package com.bsuir.lab.obj_renderer.model;

import com.bsuir.lab.obj_renderer.service.parser.IParser;
import com.bsuir.lab.obj_renderer.service.parser.ObjFileParserImpl;
import lombok.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

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

        private static final String MODEL_FOLDER = "/diablo";

        private static final String OBJ_PATH = "src/main/resources/obj"+MODEL_FOLDER+"/model.obj";
        private static final String DIFFUSE_TEXTURE_PATH = "/obj"+MODEL_FOLDER+"/diffuse.png";
        private static final String NORMAL_TEXTURE_PATH = "/obj"+MODEL_FOLDER+"/normal.png";
        private static final String SPECULAR_TEXTURE_PATH = "/obj"+MODEL_FOLDER+"/specular.png";

        private static final IParser OBJ_FILE_PARSER = new ObjFileParserImpl();
        private static final ObjectData instance;

        static {
            try {
                instance = OBJ_FILE_PARSER.parseFile(
                        OBJ_PATH,
                        DIFFUSE_TEXTURE_PATH,
                        NORMAL_TEXTURE_PATH,
                        SPECULAR_TEXTURE_PATH
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static ObjectData getInstance() {
        return InstanceHolder.instance;
    }

    private List<double[]> vertexes = new ArrayList<>();
    private List<Vector2D> textures = new ArrayList<>();
    private List<Vector3D> normals = new ArrayList<>();
    private List<List<List<Integer>>> faces = new ArrayList<>();

    private TextureModel diffuseTexture;
    private TextureModel normalTexture;
    private TextureModel specularTexture;

}
