package com.bsuir.lab.obj_renderer.service.drawing.rasterization;

import java.util.Arrays;

import static com.bsuir.lab.obj_renderer.model.WindowConstants.WINDOW_HEIGHT;
import static com.bsuir.lab.obj_renderer.model.WindowConstants.WINDOW_WIDTH;

public class ZBuffer {
    private final float[] zBuffer = new float[(int) (WINDOW_WIDTH * WINDOW_HEIGHT)];

    public void cleanZBuffer() {
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);
    }

    public void setPoint(int index, double point) {
        zBuffer[index] = (float) point;
    }

    public float get(int index) {
        return zBuffer[index];
    }
}
