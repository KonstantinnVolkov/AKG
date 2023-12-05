package com.bsuir.lab.obj_renderer.service.drawing;

import com.bsuir.lab.obj_renderer.model.Vector4D;
import com.bsuir.lab.obj_renderer.service.drawing.rasterization.ZBuffer;
import com.bsuir.lab.obj_renderer.util.MatrixRotations;
import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;

import static com.bsuir.lab.obj_renderer.model.WindowConstants.*;

public class Drawer {

    private final Color LIGHT_COLOR = Color.GOLD;

    private ZBuffer zBuffer;

    private float x = 0, y = 0, z = 0;
    private final int SCALE = 1;
    private final double ZOOM_STEP = 0.1;
    private final double Z_FAR = 1000000, Z_NEAR = 0.1;

    private Vector3D eye = new Vector3D(0, 0, 5);
    private Vector3D up = new Vector3D(0, 1, 0);
    private Vector3D target = new Vector3D(0, 0, 0);
    private Vector3D lightDirection = new Vector3D(1, 0, -1);

    private List<Vector4D> vertexesChangeable = new ArrayList<>();
    private List<Vector4D> vertexesStart = new ArrayList<>();
    private List<Vector4D> vertexesView = new ArrayList<>();

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

    List<double[]> vertexes;
    List<List<List<Integer>>> faces;

    public Drawer(List<double[]> vertexes, List<List<List<Integer>>> faces) {
        this.vertexes = vertexes;
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
        x += dx;
        y += dy;
        z += dz;
        TRANSLATION_MATRIX.setEntry(0, 3, TRANSLATION_MATRIX.getEntry(0, 3) + dx);
        TRANSLATION_MATRIX.setEntry(1, 3, TRANSLATION_MATRIX.getEntry(1, 3) + dy);
        TRANSLATION_MATRIX.setEntry(2, 3, TRANSLATION_MATRIX.getEntry(2, 3) + dz);
    }

    private void changeVertexes() {
        vertexesChangeable = new ArrayList<>();
        vertexesView = new ArrayList<>();

        for (int i = 0; i < vertexesStart.size(); i++) {
            vertexesChangeable.add(vertexesStart.get(i));

            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(SCALE_MATRIX));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(TRANSLATION_MATRIX));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(MatrixRotations.rotationMatrixX));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(MatrixRotations.rotationMatrixY));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(MatrixRotations.rotationMatrixZ));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(worldToViewMatrix));

            vertexesView.add(vertexesChangeable.get(i));

            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(viewToProjectionMatrix));
            vertexesChangeable.set(i, vertexesChangeable.get(i).divideOnScalar(vertexesChangeable.get(i).getW()));
            vertexesChangeable.set(i, vertexesChangeable.get(i).applyMatrix(projectionToViewMatrix));
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

        int x1, x2, y1, y2, z1, z2;
        for (int j = 0; j < faces.size(); j++) {
            List<List<Integer>> face = faces.get(j);

            if (!isBackFace(face)) {
                Vector3D normal = calculateNormal(face);
                double lightIntensity = getLightIntensity(normal);
                Color color = calculateColor(lightIntensity);

                for (int i = 0; i < face.size(); i++) {
                    List<Integer> temp = face.get(i);

                    if (temp.get(0) - 1 < 0) continue;

                    x1 = (int) vertexesChangeable.get(temp.get(0) - 1).getX();
                    y1 = (int) vertexesChangeable.get(temp.get(0) - 1).getY();
                    z1 = (int) vertexesChangeable.get(temp.get(0) - 1).getZ();

                    if (i == face.size() - 1) {
                        x2 = (int) vertexesChangeable.get(face.get(0).get(0) - 1).getX();
                        y2 = (int) vertexesChangeable.get(face.get(0).get(0) - 1).getY();
                        z2 = (int) vertexesChangeable.get(face.get(0).get(0) - 1).getZ();
                    } else {
                        if (face.get(i + 1).get(0) - 1 < 0) continue;

                        x2 = (int) vertexesChangeable.get(face.get(i + 1).get(0) - 1).getX();
                        y2 = (int) vertexesChangeable.get(face.get(i + 1).get(0) - 1).getY();
                        z2 = (int) vertexesChangeable.get(face.get(i + 1).get(0) - 1).getZ();
                    }
                    Point3D pStart = new Point3D(x1, y1, z1);
                    Point3D pEnd = new Point3D(x2, y2, z2);

                    DdaGraphicService.drawLine(pStart, pEnd, color, gContext, zBuffer);
                }
                DdaGraphicService.fillTriangle(gContext, face, vertexesChangeable, zBuffer, color);
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

    private double getLightIntensity(Vector3D normal) {
        double scalar = Vector3D.dotProduct(normal.scalarMultiply(-1), lightDirection.scalarMultiply(-1));
        return scalar - 1 > 0 ? 1 : Math.max(scalar, 0);
    }

    private Color calculateColor(double lightIntensity) {
        return Color.rgb((int) (lightIntensity * LIGHT_COLOR.getRed() * 255), (int) (lightIntensity * LIGHT_COLOR.getGreen() * 255), (int) (lightIntensity * LIGHT_COLOR.getBlue() * 255));
    }
}
