package com.bsuir.lab.obj_renderer.service.drawing.rasterization;

import java.util.ArrayList;
import java.util.List;

public class ZBuffer {

    private List<Float> zBuffer = new ArrayList<>();

    public void cleanZBuffer() {
       this.zBuffer = this.zBuffer
               .stream()
               .map(aFloat -> Float.POSITIVE_INFINITY)
               .toList();
    }
}
