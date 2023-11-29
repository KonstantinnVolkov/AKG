package com.bsuir.lab.obj_renderer.service.drawing;

import com.bsuir.lab.obj_renderer.model.Vector4D;
import com.bsuir.lab.obj_renderer.util.MatrixRotations;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;

import static com.bsuir.lab.obj_renderer.model.WindowConstants.*;

public class Drawer {

    private float x = 0, y = 0, z = 0;
    private final int SCALE = 1;
    private final double ZOOM_NUMBER = 0.5;
    private final double Z_FAR = 1000000, Z_NEAR = 0.1;

    private Vector3D eye = new Vector3D(0, 0, 5);
    private Vector3D up = new Vector3D(0, 1, 0);
    private Vector3D target = new Vector3D(0, 0, 0);
    private List<Vector4D> vertexesChangeable = new ArrayList<>();
    private List<Vector4D> vertexesView = new ArrayList<>();

    private final RealMatrix TRANSLATION_MATRIX = MatrixUtils.createRealMatrix(
            new double[][]{
                    {1, 0, 0, 0},
                    {0, 1, 0, 0},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1}
            }
    );

    private final RealMatrix SCALE_MATRIX = MatrixUtils.createRealMatrix(
            new double[][]{
                    {SCALE, 0, 0, 0},
                    {0, SCALE, 0, 0},
                    {0, 0, SCALE, 0},
                    {0, 0, 0, 1}
            }
    );

    private RealMatrix worldToViewMatrix;
    private final RealMatrix viewToProjectionMatrix = MatrixUtils.createRealMatrix(new double[][]{
            {(float) (1 / (WINDOW_ASPECT * Math.tan(Math.PI / 8))), 0, 0, 0},
            {0, (float) (1 / (Math.tan(Math.PI / 8))), 0, 0},
            {0, 0, (float) (Z_FAR / (Z_NEAR - Z_FAR)), (float) (Z_NEAR * Z_FAR / (Z_NEAR - Z_FAR)),},
            {0, 0, -1, 0}
    });
    private final RealMatrix projectionToViewMatrix = MatrixUtils.createRealMatrix(new double[][]{
            {(float) (WINDOW_WIDTH / 2), 0, 0, (float) (WINDOW_WIDTH / 2)},
            {0, -(float) (WINDOW_HEIGHT / 2), 0, (float) (WINDOW_HEIGHT / 2)},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
    });

    List<double[]> vertexes;
    List<List<List<Integer>>> faces;

    public Drawer(List<double[]> vertexes, List<List<List<Integer>>> faces) {
        this.vertexes = vertexes;
        this.faces = faces;
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
        Vector4D temp;
        vertexesChangeable = new ArrayList<>();

        for (double[] vertex : vertexes) {
            if (vertex.length == 3) {
                temp = new Vector4D((float) vertex[0], (float) vertex[1], (float) vertex[2], 1F);
            } else {
                temp = new Vector4D((float) vertex[0], (float) vertex[1], (float) vertex[2], (float) vertex[3]);
            }
            vertexesChangeable.add(temp);
        }

        for (int i = 0; i < vertexesChangeable.size(); i++) {
            vertexesChangeable.set(i, vertexesChangeable.get(i).apply(SCALE_MATRIX));

            vertexesChangeable.set(i, vertexesChangeable.get(i).apply(MatrixRotations.rotationMatrixX));
            vertexesChangeable.set(i, vertexesChangeable.get(i).apply(MatrixRotations.rotationMatrixY));
            vertexesChangeable.set(i, vertexesChangeable.get(i).apply(MatrixRotations.rotationMatrixZ));
            vertexesChangeable.set(i, vertexesChangeable.get(i).apply(TRANSLATION_MATRIX));
            vertexesChangeable.set(i, vertexesChangeable.get(i).apply(worldToViewMatrix));
            vertexesChangeable.set(i, vertexesChangeable.get(i).apply(viewToProjectionMatrix));
            vertexesChangeable.set(i, vertexesChangeable.get(i).divide(vertexesChangeable.get(i).getW()));
            vertexesChangeable.set(i, vertexesChangeable.get(i).apply(projectionToViewMatrix));
        }
    }

    public void setWorldToViewMatrix() {
        Vector3D axisZ = eye.subtract(target).normalize();
        Vector3D axisX = Vector3D.crossProduct(up, axisZ).normalize();
        Vector3D axisY = Vector3D.crossProduct(axisZ, axisX).normalize();

        worldToViewMatrix = MatrixUtils.createRealMatrix(new double[][]{
                {axisX.getX(), axisX.getY(), axisX.getZ(), -Vector3D.dotProduct(axisX, eye)},
                {axisY.getX(), axisY.getY(), axisY.getZ(), -Vector3D.dotProduct(axisY, eye)},
                {axisZ.getX(), axisZ.getY(), axisZ.getZ(), -Vector3D.dotProduct(axisZ, eye)},
                {0, 0, 0, 1},
        });
//        System.out.println(worldToViewMatrix);
    }

    public void zoomIn() {
        eye = new Vector3D(eye.getX(), eye.getY(), eye.getZ() - ZOOM_NUMBER);
        setWorldToViewMatrix();
    }

    public void zoomOut() {
        eye = new Vector3D(eye.getX(), eye.getY(), eye.getZ() + ZOOM_NUMBER);
        setWorldToViewMatrix();
    }

    public void draw(GraphicsContext gContext) {
        changeVertexes();

        int x1, x2, y1, y2;
        for (int j = 0; j < faces.size(); j++) {
            List<List<Integer>> array = faces.get(j);

            for (int i = 0; i < array.size(); i++) {
                List<Integer> temp = array.get(i);

                x1 = (int) vertexesChangeable.get(temp.get(0) - 1).getX();
                y1 = (int) vertexesChangeable.get(temp.get(0) - 1).getY();

                if (i == array.size() - 1) {
                    x2 = (int) vertexesChangeable.get(array.get(0).get(0) - 1).getX();
                    y2 = (int) vertexesChangeable.get(array.get(0).get(0) - 1).getY();
                } else {
                    x2 = (int) vertexesChangeable.get(array.get(i + 1).get(0) - 1).getX();
                    y2 = (int) vertexesChangeable.get(array.get(i + 1).get(0) - 1).getY();
                }
                Point2D pointStart = new Point2D(x1, y1);
                Point2D pointEnd = new Point2D(x2, y2);
                DdaGraphicService.drawLine(pointStart, pointEnd, gContext);
            }
        }

    }

    public void setupCamera(GraphicsContext gContext) {
        setWorldToViewMatrix();

        changeVertexes();
        draw(gContext);
    }
}
