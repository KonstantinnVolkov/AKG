package com.bsuir.lab.obj_renderer.model;

import lombok.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.RealMatrix;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Vector4D {
    private float x;
    private float y;
    private float z;
    private float w;

    public Vector4D applyMatrix(RealMatrix matrix) {
        return new Vector4D(
                (float) (matrix.getEntry(0, 0) * this.x + matrix.getEntry(0, 1) * this.y + matrix.getEntry(0, 2) * this.z + matrix.getEntry(0, 3) * this.w),
                (float) (matrix.getEntry(1, 0) * this.x + matrix.getEntry(1, 1) * this.y + matrix.getEntry(1, 2) * this.z + matrix.getEntry(1, 3) * this.w),
                (float) (matrix.getEntry(2, 0) * this.x + matrix.getEntry(2, 1) * this.y + matrix.getEntry(2, 2) * this.z + matrix.getEntry(2, 3) * this.w),
                (float) (matrix.getEntry(3, 0) * this.x + matrix.getEntry(3, 1) * this.y + matrix.getEntry(3, 2) * this.z + matrix.getEntry(3, 3) * this.w)
        );
    }

    public static Vector4D add(Vector4D v1, Vector4D v2) {
        return Vector4D.builder()
                .x(v1.getX() + v2.getX())
                .y(v1.getY() + v2.getY())
                .z(v1.getZ() + v2.getZ())
                .w(v1.getW() + v2.getW())
                .build();
    }

    public static Vector4D subtract(Vector4D v1, Vector4D v2) {
        return Vector4D.builder()
                .x(v1.getX() - v2.getX())
                .y(v1.getY() - v2.getY())
                .z(v1.getZ() - v2.getZ())
                .w(v1.getW() - v2.getW())
                .build();
    }

    public static Vector4D multiplyOnScalar(Vector4D v, float k) {
        return Vector4D.builder()
                .x(v.getX() * k)
                .y(v.getY() * k)
                .z(v.getZ() * k)
                .w(v.getW() * k)
                .build();
    }

    public Vector4D divideOnScalar(float w) {
        return new Vector4D(this.getX() / w, this.getY() / w, this.getZ() / w, this.getW() / w);
    }

    public Vector3D toVector3D() {
        return new Vector3D(x, y, z);
    }
}
