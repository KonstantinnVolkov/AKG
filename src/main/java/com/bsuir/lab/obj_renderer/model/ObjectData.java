package com.bsuir.lab.obj_renderer.model;

import com.bsuir.lab.obj_renderer.service.parser.IParser;
import com.bsuir.lab.obj_renderer.service.parser.ObjFileParserImpl;
import lombok.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ObjectData {

    private static final class InstanceHolder {

        private static final IParser OBJ_FILE_PARSER = new ObjFileParserImpl();
        private static final ObjectData instance;

        static {
            try {
                instance = OBJ_FILE_PARSER.parseFile("src/main/resources/com/bsuir/lab/obj_renderer/obj/african_head.obj");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static ObjectData getInstance() {
        return InstanceHolder.instance;
    }

    private List<double[]> vertexes = new ArrayList<>();
    private List<double[]> textures = new ArrayList<>();
    private List<double[]> normals = new ArrayList<>();
    List<List<List<Integer>>> faces = new ArrayList<>();

}
