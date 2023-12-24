package com.bsuir.lab.obj_renderer.service.drawing;

import com.bsuir.lab.obj_renderer.model.Vector4D;
import com.bsuir.lab.obj_renderer.service.drawing.rasterization.ZBuffer;
import com.bsuir.lab.obj_renderer.util.MatrixRotations;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;

import static com.bsuir.lab.obj_renderer.model.WindowConstants.*;
import static com.bsuir.lab.obj_renderer.util.Vector3DMatrixApplier.applyMatrix;

public class Drawer {

    List<double[]> vertexes;
    List<List<List<Integer>>> faces;
    List<Vector3D> normals;
    private ZBuffer zBuffer;

    private final int SCALE = 1;
    private final double ZOOM_STEP = 0.1, Z_FAR = 1000000, Z_NEAR = 0.1;

    private Vector3D eye = new Vector3D(0, 0, 5);
    private Vector3D up = new Vector3D(0, 1, 0);
    private Vector3D target = new Vector3D(0, 0, 0);

    private List<Vector4D> vertexesChangeable = new ArrayList<>();
    private List<Vector4D> vertexesStart = new ArrayList<>();
    private List<Vector4D> vertexesView = new ArrayList<>();
    private List<Vector4D> vertexesWorld = new ArrayList<>();
    private List<Vector3D> normalsChangeable = new ArrayList<>();

    private final RealMatrix TRANSLATION_MATRIX = MatrixUtils.createRealMatrix(new double[][] {
            {1, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
    });

    private final RealMatrix SCALE_MATRIX = MatrixUtils.createRealMatrix(new double[][] {
            {SCALE, 0, 0, 0},
            {0, SCALE, 0, 0},
            {0, 0, SCALE, 0},
            {0, 0, 0, 1}
    });

    private RealMatrix worldToViewMatrix;
    private final RealMatrix viewToProjectionMatrix = MatrixUtils.createRealMatrix(new double[][] {
            {(float) (1 / (WINDOW_ASPECT * Math.tan(Math.PI / 8))), 0, 0, 0},
            {0, (float) (1 / (Math.tan(Math.PI / 8))), 0, 0},
            {0, 0, (float) (Z_FAR / (Z_NEAR - Z_FAR)), (float) (Z_NEAR * Z_FAR / (Z_NEAR - Z_FAR)),},
            {0, 0, -1, 0}
    });
    private final RealMatrix projectionToViewMatrix = MatrixUtils.createRealMatrix(new double[][] {
            {(WINDOW_WIDTH / 2), 0, 0, (WINDOW_WIDTH / 2)},
            {0, -(float) (WINDOW_HEIGHT / 2), 0, (WINDOW_HEIGHT / 2)},
            {0, 0, 1, 0},
            {0, 0, 0, 1}}
    );



    public Drawer(List<double[]> vertexes, List<Vector3D> normals, List<List<List<Integer>>> faces) {
        this.vertexes = vertexes;
        this.normals = normals;
        this.faces = faces;
        this.zBuffer = new ZBuffer();

        Vector4D temp;
        for (double[] vertex : vertexes) {
            if (vertex.length == 3) {
                temp = new Vector4D((float) vertex[0], (float) vertex[1], (float) vertex[2], 1F);
            } else {
                temp = new Vector4D((float) vertex[0], (float) vertex[1], (float) vertex[2], (float) vertex[3]);
            }
            vertexesStart.add(temp);
        }
    }

    public void changeTranslationMatrix(float dx, float dy, float dz) {
        TRANSLATION_MATRIX.setEntry(0, 3, TRANSLATION_MATRIX.getEntry(0, 3) + dx);
        TRANSLATION_MATRIX.setEntry(1, 3, TRANSLATION_MATRIX.getEntry(1, 3) + dy);
        TRANSLATION_MATRIX.setEntry(2, 3, TRANSLATION_MATRIX.getEntry(2, 3) + dz);
    }

    private void changeVertexes() {
        vertexesChangeable = new ArrayList<>();
        vertexesView = new ArrayList<>();
        vertexesWorld = new ArrayList<>();
        normalsChangeable = new ArrayList<>();

        for (int i = 0; i < vertexesStart.size(); i++) {
            //model to world
            vertexesChangeable.add(vertexesStart.get(i));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(SCALE_MATRIX));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(TRANSLATION_MATRIX));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(MatrixRotations.rotationMatrixX));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(MatrixRotations.rotationMatrixY));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(MatrixRotations.rotationMatrixZ));
            vertexesWorld.add(vertexesChangeable.get(i));

            //to observer
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(worldToViewMatrix));
            vertexesView.add(vertexesChangeable.get(i));

            //to projection
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(viewToProjectionMatrix));
            vertexesChangeable.set(i, vertexesChangeable.get(i).divideOnScalar(vertexesChangeable.get(i).getW()));

            //to screen
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(projectionToViewMatrix));
        }
        Vector3D normal = null;
        for (int i = 0; i < normals.size(); i++) {
            normal = normals.get(i);
            normal = applyMatrix(normal, TRANSLATION_MATRIX);
            normal = applyMatrix(normal, MatrixRotations.rotationMatrixX);
            normal = applyMatrix(normal, MatrixRotations.rotationMatrixY);
            normal = applyMatrix(normal, MatrixRotations.rotationMatrixZ);
            normal = applyMatrix(normal, worldToViewMatrix);
            normalsChangeable.add(normal);
        }
    }

    public void setWorldToViewMatrix() {
        Vector3D axisZ = eye.subtract(target).normalize();
        Vector3D axisX = Vector3D.crossProduct(up, axisZ).normalize();
        Vector3D axisY = Vector3D.crossProduct(axisZ, axisX).normalize();

        worldToViewMatrix = MatrixUtils.createRealMatrix(new double[][] {
                {axisX.getX(), axisX.getY(), axisX.getZ(), -Vector3D.dotProduct(axisX, eye)},
                {axisY.getX(), axisY.getY(), axisY.getZ(), -Vector3D.dotProduct(axisY, eye)},
                {axisZ.getX(), axisZ.getY(), axisZ.getZ(), -Vector3D.dotProduct(axisZ, eye)},
                {0, 0, 0, 1}
        });
    }

    public void zoomIn() {
        eye = new Vector3D(eye.getX(), eye.getY(), eye.getZ() - ZOOM_STEP);
        setWorldToViewMatrix();
    }

    public void zoomOut() {
        eye = new Vector3D(eye.getX(), eye.getY(), eye.getZ() + ZOOM_STEP);
        setWorldToViewMatrix();
    }

    public void draw(GraphicsContext gContext) {
        changeVertexes();
        zBuffer.cleanZBuffer();

        for (int i = 0; i < faces.size(); i++) {
            List<List<Integer>> face = faces.get(i);

            if (!isBackFace(face)) {
                GraphicService.fillTriangle(
                        gContext,
                        face,
                        vertexesWorld,
                        vertexesChangeable,
                        normalsChangeable,
                        zBuffer
                );
            }
        }

    }

    public void setupCamera(GraphicsContext gContext) {
        setWorldToViewMatrix();

        changeVertexes();
        draw(gContext);
    }

    private boolean isBackFace(List<List<Integer>> face) {
        Vector3D viewVector = vertexesView.get(face.get(0).get(0) - 1).toVector3D().subtract(eye);
        return Vector3D.dotProduct(calculateNormal(face), viewVector) <= 0;
    }

    private Vector3D calculateNormal(List<List<Integer>> face) {
        Vector3D v1 = vertexesView.get(face.get(1).get(0) - 1).toVector3D().subtract(vertexesView.get(face.get(0).get(0) - 1).toVector3D()).normalize();

        Vector3D v2 = vertexesView.get(face.get(2).get(0) - 1).toVector3D().subtract(vertexesView.get(face.get(0).get(0) - 1).toVector3D()).normalize();
        return Vector3D.crossProduct(v2, v1).normalize();
    }
}
