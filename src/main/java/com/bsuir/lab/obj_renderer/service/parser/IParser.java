package com.bsuir.lab.obj_renderer.service.parser;

import com.bsuir.lab.obj_renderer.model.ObjectData;

import java.io.IOException;

public interface IParser {

    ObjectData parseFile(
            String objPath,
            String diffusePath,
            String normalPath,
            String specularPath
    ) throws IOException;
}
