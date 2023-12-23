package com.bsuir.lab.obj_renderer.util;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.RealMatrix;

public class Vector3DMatrixApplier {

    public static Vector3D applyMatrix(Vector3D vector, RealMatrix matrix) {

        return new Vector3D(
                matrix.getEntry(0,0)*vector.getX() + matrix.getEntry(0,1)*vector.getY() + matrix.getEntry(0,2)*vector.getZ(),
                matrix.getEntry(1,0)*vector.getX() + matrix.getEntry(1,1)*vector.getY() + matrix.getEntry(1,2)*vector.getZ(),
                matrix.getEntry(2,0)*vector.getX() + matrix.getEntry(2,1)*vector.getY() + matrix.getEntry(2,2)*vector.getZ()
        );
    }

}
