package com.bsuir.lab.obj_renderer.service.drawing.rasterization;

import java.util.Arrays;

import static com.bsuir.lab.obj_renderer.model.WindowConstants.WINDOW_HEIGHT;
import static com.bsuir.lab.obj_renderer.model.WindowConstants.WINDOW_WIDTH;

public class ZBuffer {

//    private List<Float> zBuffer = Arrays.asList(new Float[(int) (WINDOW_WIDTH * WINDOW_HEIGHT)]);
    private Float[] zBuffer = new Float[(int) (WINDOW_WIDTH * WINDOW_HEIGHT)];

    public void cleanZBuffer() {
        Arrays.fill(zBuffer, Float.POSITIVE_INFINITY);
//       this.zBuffer = this.zBuffer
//               .stream()
//               .map(aFloat -> Float.POSITIVE_INFINITY)
//               .toList();
    }

//    public void setPoint(float point) {
//        zBuffer.add(point);
//    }

    public void setPoint(int index, double point) {
        zBuffer[index] = (float) point;
//        zBuffer.set(index, (float) point);
    }

    public float get(int index) {
        return zBuffer[index];
//        return zBuffer.get(index);
    }
}
