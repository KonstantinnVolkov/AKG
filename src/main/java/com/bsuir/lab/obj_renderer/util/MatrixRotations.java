package com.bsuir.lab.obj_renderer.util;

import javafx.geometry.Point2D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class MatrixRotations {

    private static final double DELTA_ANGLE = 0.01;
    static double angleY, angleX, angleZ;
    public static RealMatrix rotationMatrixX = MatrixUtils.createRealMatrix(new double[][]{
                    {1, 0, 0, 0},
                    {0, 1, 0, 0},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1}
            }

    );

    public static RealMatrix rotationMatrixY = MatrixUtils.createRealMatrix(new double[][]{
                    {1, 0, 0, 0},
                    {0, 1, 0, 0},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1}
            }

    );
    public static RealMatrix rotationMatrixZ = MatrixUtils.createRealMatrix(new double[][]{
                    {1, 0, 0, 0},
                    {0, 1, 0, 0},
                    {0, 0, 1, 0},
                    {0, 0, 0, 1}
            }

    );

    public static void rotate(Point2D path) {
        angleX += path.getY() * DELTA_ANGLE;
        angleY += path.getX() * DELTA_ANGLE;

        rotationMatrixX.setEntry(1, 1, Math.cos(angleX));
        rotationMatrixX.setEntry(1, 2, -Math.sin(angleX));
        rotationMatrixX.setEntry(2, 1, Math.sin(angleX));
        rotationMatrixX.setEntry(2, 2, Math.cos(angleX));

        rotationMatrixY.setEntry(0, 0, Math.cos(angleY));
        rotationMatrixY.setEntry(0, 2, Math.sin(angleY));
        rotationMatrixY.setEntry(2, 0, -Math.sin(angleY));
        rotationMatrixY.setEntry(2, 2, Math.cos(angleY));
    }
}
