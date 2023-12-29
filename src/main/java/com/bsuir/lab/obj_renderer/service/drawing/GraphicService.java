package com.bsuir.lab.obj_renderer.service.drawing;

import com.bsuir.lab.obj_renderer.model.ObjectData;
import com.bsuir.lab.obj_renderer.model.Vector4D;
import com.bsuir.lab.obj_renderer.service.drawing.rasterization.ZBuffer;
import com.bsuir.lab.obj_renderer.util.MatrixRotations;
import com.bsuir.lab.obj_renderer.util.Vector3DMatrixApplier;
import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.util.List;

import static com.bsuir.lab.obj_renderer.model.WindowConstants.*;
import static com.bsuir.lab.obj_renderer.util.Vector3DMatrixApplier.applyMatrix;

public class GraphicService {

    private static final Vector3D VECTOR_3D_ONE = new Vector3D(1d, 1d, 1d);
    private static final Vector3D EYE = new Vector3D(0, 0, 5);
    private static final Vector3D LIGHT_DIRECTION = new Vector3D(-1, 0, 0).normalize();
    private static final Vector3D INVERSE_LIGHT_DIRECTION = LIGHT_DIRECTION.scalarMultiply(-1);

    private static final float LIGHT_INTENSITY = 1f;
    private static final float AMBIENT_LIGHT_INTENSITY = 1f;
    private static final float DIFFUSE_LIGHT_INTENSITY = 2f;
    private static final float SPECULAR_LIGHT_INTENSITY = 1f;
    private static final float SPECULAR_FACTOR = 10f;
    private static final float GLOSS_FACTOR = 16f;

    private static Vector4D a;
    private static Vector4D b;
    private static Vector4D c;

    private static Vector3D aw;
    private static Vector3D bw;
    private static Vector3D cw;

    private static Vector2D textureA;
    private static Vector2D textureB;
    private static Vector2D textureC;

    private static final float[] ambientValues = new float[3];
    private static final float[] diffuseValues = new float[3];
    private static final float[] specularValues = new float[3];


    record Vector3dSwap(Vector3D first, Vector3D second) {
    }

    record Vector4dSwap(Vector4D first, Vector4D second) {
    }

    record Vector2dSwap(Vector2D first, Vector2D second) {
    }

    public static void drawLineDDA(
            Point3D startPoint, Point3D endPoint,
            Color lineColor, GraphicsContext graphicsContext,
            ZBuffer zBuffer
    ) {
        int startX = (int) Math.round(startPoint.getX());
        int startY = (int) Math.round(startPoint.getY());
        int startZ = (int) Math.round(startPoint.getZ());

        int endX = (int) Math.round(endPoint.getX());
        int endY = (int) Math.round(endPoint.getY());
        int endZ = (int) Math.round(endPoint.getZ());

        int rasterizationSteps = Math.max(
                Math.abs(endX - startX),
                Math.abs(endY - startY)
        );

        double deltaX = (double) (endX - startX) / rasterizationSteps;
        double deltaY = (double) (endY - startY) / rasterizationSteps;
        double deltaZ = (double) (endZ - startZ) / rasterizationSteps;

        double x = startPoint.getX();
        double y = startPoint.getY();
        double z = startPoint.getZ();

        if ((x < WINDOW_WIDTH) && (x > 0) && (y < WINDOW_HEIGHT) && (y > 0)) {
            int index = (int) (y * WINDOW_WIDTH + x);
            if (z > zBuffer.get(index)) {
                zBuffer.setPoint(index, z);

                graphicsContext.getPixelWriter().setColor((int) x, (int) y, lineColor);
            }
        }

        for (int i = 0; i < rasterizationSteps; i++) {
            x += deltaX;
            y += deltaY;
            z += deltaZ;

            if ((x < WINDOW_WIDTH) && (x > 0) && (y < WINDOW_HEIGHT) && (y > 0)) {
                int index = (int) (y * WINDOW_WIDTH + x);
                if (z > zBuffer.get(index)) {
                    zBuffer.setPoint(index, z);

                    graphicsContext.getPixelWriter().setColor((int) x, (int) y, lineColor);
                }
            }
        }
    }

    private static void sortVerticesByY() {
        if (a.getY() > c.getY()) {
            Vector4dSwap vector4dSwap = new Vector4dSwap(c, a);
            a = vector4dSwap.first;
            c = vector4dSwap.second;

            Vector3dSwap vector3dSwap = new Vector3dSwap(cw, aw);
            aw = vector3dSwap.first;
            cw = vector3dSwap.second;

            Vector2dSwap vector2dSwap = new Vector2dSwap(textureC, textureA);
            textureA = vector2dSwap.first;
            textureC = vector2dSwap.second;
        }

        if (a.getY() > b.getY()) {
            Vector4dSwap vector4dSwap = new Vector4dSwap(b, a);
            a = vector4dSwap.first;
            b = vector4dSwap.second;

            Vector3dSwap vector3dSwap = new Vector3dSwap(bw, aw);
            aw = vector3dSwap.first;
            bw = vector3dSwap.second;

            Vector2dSwap vector2dSwap = new Vector2dSwap(textureB, textureA);
            textureA = vector2dSwap.first;
            textureB = vector2dSwap.second;
        }

        if (b.getY() > c.getY()) {
            Vector4dSwap vector4dSwap = new Vector4dSwap(c, b);
            b = vector4dSwap.first;
            c = vector4dSwap.second;

            Vector3dSwap vector3dSwap = new Vector3dSwap(cw, bw);
            bw = vector3dSwap.first;
            cw = vector3dSwap.second;

            Vector2dSwap vector2dSwap = new Vector2dSwap(textureC, textureB);
            textureB = vector2dSwap.first;
            textureC = vector2dSwap.second;
        }
    }

    public static void fillTriangle(
            GraphicsContext gContext,
            List<List<Integer>> face,
            List<Vector4D> vertexesWorld,
            List<Vector4D> vertexesChangeable,
            List<Vector3D> normalsChangeable,
            List<Vector2D> textures,
            ZBuffer zBuffer
    ) {

        //Polygon vertexes
        a = vertexesChangeable.get(face.get(0).get(0) - 1);
        b = vertexesChangeable.get(face.get(1).get(0) - 1);
        c = vertexesChangeable.get(face.get(2).get(0) - 1);

        //Polygon world vertexes
        aw = vertexesWorld.get(face.get(0).get(0) - 1).toVector3D();
        bw = vertexesWorld.get(face.get(1).get(0) - 1).toVector3D();
        cw = vertexesWorld.get(face.get(2).get(0) - 1).toVector3D();

        textureA = textures.get(face.get(0).get(1) - 1).scalarMultiply(a.getW());
        textureB = textures.get(face.get(1).get(1) - 1).scalarMultiply(b.getW());
        textureC = textures.get(face.get(2).get(1) - 1).scalarMultiply(c.getW());
        textureA = textureA.scalarMultiply(a.getW());
        textureB = textureB.scalarMultiply(b.getW());
        textureC = textureC.scalarMultiply(c.getW());

        sortVerticesByY();

        Vector4D vertexInterpolationCoefficient1 = calculateVertexInterpolationCoefficient(a, c);
        Vector3D worldKoeff01 = calculateVertexWorldCoordsInterpolationCoefficient(aw, cw, a, c);
        Vector2D textureKoeff01 = textureC.subtract(textureA).scalarMultiply(1 / (c.getY() - a.getY()));

        Vector4D vertexInterpolationCoefficient2 = calculateVertexInterpolationCoefficient(a, b);
        Vector3D worldKoeff02 = calculateVertexWorldCoordsInterpolationCoefficient(aw, bw, a, b);
        Vector2D textureKoeff02 = textureB.subtract(textureA).scalarMultiply(1 / (b.getY() - a.getY()));

        Vector4D vertexInterpolationCoefficient3 = calculateVertexInterpolationCoefficient(b, c);
        Vector3D worldKoeff03 = calculateVertexWorldCoordsInterpolationCoefficient(bw, cw, b, c);
        Vector2D textureKoeff03 = textureC.subtract(textureB).scalarMultiply(1 / (c.getY() - b.getY()));

        int top = Math.max(0, (int) Math.ceil(a.getY()));
        int bottom = Math.min((int) WINDOW_HEIGHT, (int) Math.ceil(c.getY()));

        for (int y = top; y < bottom; y++) {
            Vector4D l = Vector4D.add(a, Vector4D.multiplyOnScalar(vertexInterpolationCoefficient1, y - a.getY()));
            Vector4D r = (y < b.getY())
                    ? Vector4D.add(a, Vector4D.multiplyOnScalar(vertexInterpolationCoefficient2, y - a.getY()))
                    : Vector4D.add(b, Vector4D.multiplyOnScalar(vertexInterpolationCoefficient3, y - b.getY()));

            Vector3D worldL = aw.add(worldKoeff01.scalarMultiply(y - a.getY()));
            Vector3D worldR = (y < b.getY())
                    ? aw.add(worldKoeff02.scalarMultiply(y - a.getY()))
                    : bw.add(worldKoeff03.scalarMultiply(y - b.getY()));

            Vector2D textureL = textureA.add(
                    textureKoeff01.scalarMultiply(y - a.getY())
            );
            Vector2D textureR = (y < b.getY())
                    ? textureA.add(textureKoeff02.scalarMultiply(y - a.getY()))
                    : textureB.add(textureKoeff03.scalarMultiply(y - b.getY()));


            if (l.getX() > r.getX()) {
                Vector4dSwap vector4dSwap = new Vector4dSwap(r, l);
                l = vector4dSwap.first;
                r = vector4dSwap.second;

                Vector3dSwap vector3dSwap = new Vector3dSwap(worldR, worldL);
                worldL = vector3dSwap.first;
                worldR = vector3dSwap.second;

                Vector2dSwap vector2dSwap = new Vector2dSwap(textureR, textureL);
                textureL = vector2dSwap.first;
                textureR = vector2dSwap.second;
            }

            Vector4D k = Vector4D.subtract(r, l).divideOnScalar(r.getX() - l.getX());
            Vector3D worldKoeff = worldR.subtract(worldL).scalarMultiply(1 / (r.getX() - l.getX()));
            Vector2D textureKoeff = textureR.subtract(textureL).scalarMultiply(1 / (r.getX() - l.getX()));

            int left = Math.max(0, (int) Math.ceil(l.getX()));
            int right = Math.min((int) WINDOW_WIDTH, (int) Math.ceil(r.getX()));

            for (int x = left; x < right; x++) {
                Vector4D p = Vector4D.add(
                        l,
                        Vector4D.multiplyOnScalar(k, x - l.getX())
                );
                Vector3D pWorld = worldL.add(
                        worldKoeff.scalarMultiply(x - l.getX())
                );

                int index = (int) (y * WINDOW_WIDTH + x);

                if (p.getZ() < zBuffer.get(index)) {
                    Vector2D texture = textureL.add(
                            textureKoeff.scalarMultiply(x - l.getX())
                    ).scalarMultiply(1 / p.getW());

                    Color pixelColor = null;
                    ObjectData objectData = ObjectData.getInstance();

                    //Цвет объекта
                    Vector3D color = null;
                    if (ObjectData.getInstance().getDiffuseTexture().getImage() != null) {
                        pixelColor = objectData.getDiffuseTexture().getPixelColor(
                                (int) (texture.getX() * (objectData.getDiffuseTexture().getWidth() - 1)),
                                (int) ((1 - texture.getY()) * (objectData.getDiffuseTexture().getHeight() - 1))
                        );
                        color = new Vector3D(pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue());
                    }

                    //Цвет отражения
                    Vector3D specularColor = null;
                    if (ObjectData.getInstance().getSpecularTexture().getImage() != null) {
                        pixelColor = objectData.getSpecularTexture().getPixelColor(
                                (int) (texture.getX() * (objectData.getSpecularTexture().getWidth() - 1)),
                                (int) ((1 - texture.getY()) * (objectData.getSpecularTexture().getHeight() - 1))
                        );
                        specularColor = new Vector3D(pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue());
                    }

                    //Нормали
                    Vector3D normal;
                    pixelColor = objectData.getNormalTexture().getPixelColor(
                            (int) (texture.getX() * (objectData.getNormalTexture().getWidth() - 1)),
                            (int) ((1 - texture.getY()) * (objectData.getNormalTexture().getHeight() - 1))
                    );
                    normal = new Vector3D(pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue());
                    normal = normal.scalarMultiply(2).subtract(VECTOR_3D_ONE).normalize();
                    normal = applyMatrix(normal, MatrixRotations.rotationMatrixX);
                    normal = applyMatrix(normal, MatrixRotations.rotationMatrixY);
                    normal = applyMatrix(normal, MatrixRotations.rotationMatrixZ);

                    ambientLightning(color, ambientValues);
                    diffuseLightning(color, diffuseValues, normal);
                    specularLightning(specularColor, EYE.subtract(pWorld).normalize(), normal, specularValues);

                    zBuffer.setPoint(index, p.getZ());

                    int colorR = (int) (Math.min(
                            (ambientValues[0] + diffuseValues[0] + specularValues[0]) * 255,
                            255
                    ));

                    int colorG = (int) (Math.min(
                            (ambientValues[1] + diffuseValues[1] + specularValues[1]) * 255,
                            255
                    ));
                    int colorB = (int) (Math.min(
                            (ambientValues[2] + diffuseValues[2] + specularValues[2]) * 255,
                            255
                    ));

                    Color finalColor = Color.rgb(colorR, colorG, colorB);
                    gContext.getPixelWriter().setColor(x, y, finalColor);
                }
            }
        }
    }

    private static Vector4D calculateVertexInterpolationCoefficient(Vector4D a, Vector4D b) {
        return Vector4D.subtract(b, a).divideOnScalar(b.getY() - a.getY());
    }

    private static Vector3D calculateVertexWorldCoordsInterpolationCoefficient(
            Vector3D worldA, Vector3D worldB,
            Vector4D a, Vector4D b
    ) {
        return worldB.subtract(worldA).scalarMultiply(1 / (b.getY() - a.getY()));
    }

    private static double getLightIntensity(Vector3D normal) {
        double scalar = Vector3D.dotProduct(normal.scalarMultiply(-1), INVERSE_LIGHT_DIRECTION);
        return scalar - 1 > 0 ? 1 : Math.max(scalar, 0);
    }

    private static void ambientLightning(Vector3D color, float[] ambientValues) {
        ambientValues[0] = (float) (color.getX() * AMBIENT_LIGHT_INTENSITY);
        ambientValues[1] = (float) (color.getY() * AMBIENT_LIGHT_INTENSITY);
        ambientValues[2] = (float) (color.getZ() * AMBIENT_LIGHT_INTENSITY);
    }

    private static void diffuseLightning(Vector3D color, float[] diffuseValues, Vector3D normal) {
        float scalar = (float) Math.max(getLightIntensity(normal), 0) * DIFFUSE_LIGHT_INTENSITY;

        diffuseValues[0] = (float) (color.getX() * scalar);
        diffuseValues[1] = (float) (color.getY() * scalar);
        diffuseValues[2] = (float) (color.getZ() * scalar);
    }

    private static void specularLightning(Vector3D color, Vector3D view, Vector3D normal, float[] specularValues) {
        Vector3D reflection = reflect(INVERSE_LIGHT_DIRECTION, normal).normalize();

        float RV = (float) Math.max(reflection.dotProduct(view), 0);
        float temp = (float) Math.pow(RV, GLOSS_FACTOR);

        specularValues[0] = (float) (temp * SPECULAR_LIGHT_INTENSITY * color.getX());
        specularValues[1] = (float) (temp * SPECULAR_LIGHT_INTENSITY * color.getY());
        specularValues[2] = (float) (temp * SPECULAR_LIGHT_INTENSITY * color.getZ());
    }

    private static Vector3D reflect(Vector3D vector, Vector3D normal) {
        double num = vector.dotProduct(normal);
        Vector3D vector3D = normal.scalarMultiply(num).scalarMultiply(2f);
        return vector.subtract(vector3D);
    }
}
