package com.bsuir.lab.obj_renderer.model;

import lombok.*;
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

    public Vector4D divide(float w) {
        return new Vector4D(this.getX() / w, this.getY() / w, this.getZ() / w, this.getW() / w);
    }

    public Vector4D apply(RealMatrix matrix) {
        return new Vector4D(
                (float) (matrix.getEntry(0, 0) * this.x + matrix.getEntry(0, 1) * this.y + matrix.getEntry(0, 2) * this.z + matrix.getEntry(0, 3) * this.w),
                (float) (matrix.getEntry(1, 0) * this.x + matrix.getEntry(1, 1) * this.y + matrix.getEntry(1, 2) * this.z + matrix.getEntry(1, 3) * this.w),
                (float) (matrix.getEntry(2, 0) * this.x + matrix.getEntry(2, 1) * this.y + matrix.getEntry(2, 2) * this.z + matrix.getEntry(2, 3) * this.w),
                (float) (matrix.getEntry(3, 0) * this.x + matrix.getEntry(3, 1) * this.y + matrix.getEntry(3, 2) * this.z + matrix.getEntry(3, 3) * this.w)
        );
    }
}
