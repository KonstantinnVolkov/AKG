package com.bsuir.lab.obj_renderer.service.parser;

import com.bsuir.lab.obj_renderer.model.ObjectData;
import javafx.geometry.Point3D;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ObjFileParserImpl implements IParser{

    private static final String V = "v";
    private static final String VT = "vt";
    private static final String VN = "vn";
    private static final String F = "f";
    private static final String SPACE_REGEXP = "\\s+";
    private static final String SLASH_REGEXP = "/";

    List<double[]> vertexes = new ArrayList<>();
    List<double[]> textures = new ArrayList<>();
    List<Point3D> normals = new ArrayList<>();
    List<List<List<Integer>>> faces = new ArrayList<>();


    @Override
    public ObjectData parseFile(String path) throws IOException {

        List<String> lines = Files.readAllLines(Path.of(path));

        lines.forEach(line -> {
            String[] literals = line.split(SPACE_REGEXP);
            switch (literals[0]) {
                case V -> {
                    if (literals.length == 4) {
                        vertexes.add(new double[] {
                                Double.parseDouble(literals[1]), Double.parseDouble(literals[2]),
                                Double.parseDouble(literals[3]), 1D
                        });
                    } else {
                        vertexes.add(new double[] {
                                Double.parseDouble(literals[1]), Double.parseDouble(literals[2]),
                                Double.parseDouble(literals[3]), Double.parseDouble(literals[4]),
                        });
                    }
                }
                case F -> {
                    if (line.contains(SLASH_REGEXP)) {
                        List<List<Integer>> topLevelList = new ArrayList<>();
                        for (int i = 1; i < literals.length; i++) {
                            String[] numbers = literals[i].split(SLASH_REGEXP);
                            List<Integer> tempList = new ArrayList<>();

                            for (String num: numbers) {
                                if (!num.isEmpty()) {
                                    tempList.add(Integer.parseInt(num));
                                } else {
                                    tempList.add(0);
                                }
                            }
                            topLevelList.add(tempList);
                        }
                        faces.add(topLevelList);
                    } else {
                        List<List<Integer>> topLevelList = new ArrayList<>();
                        for (int i = 1; i < literals.length; i++) {
                            List<Integer> tempList = new ArrayList<>();
                            tempList.add(Integer.parseInt(literals[i]));
                            topLevelList.add(tempList);
                        }
                        faces.add(topLevelList);
                    }

                }
                case VN -> {

                }
                case VT -> {

                }
            }
        });

        return ObjectData.builder()
                .vertexes(vertexes)
                .textures(textures)
                .faces(faces)
                .build();
    }
}
