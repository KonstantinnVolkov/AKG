package com.bsuir.lab.obj_renderer.service.drawing;

import com.bsuir.lab.obj_renderer.model.Vector4D;
import com.bsuir.lab.obj_renderer.service.drawing.rasterization.ZBuffer;
import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

import static com.bsuir.lab.obj_renderer.model.WindowConstants.*;

public class GraphicService {

    private static final Color PIXEL_BASE_COLOR = Color.ALICEBLUE;
    private static final Color LIGHT_BASE_COLOR = Color.WHITE;

    private static final Vector3D EYE = new Vector3D(0, 0, 5);
    private static final Vector3D LIGHT_DIRECTION = new Vector3D(-50, 1, 0).normalize();
    private static final Vector3D INVERSE_LIGHT_DIRECTION = new Vector3D(-50, 1, 0).normalize().scalarMultiply(-1);

    private static final float LIGHT_INTENSITY = 5000f;
    private static final float AMBIENT_LIGHT_INTENSITY = 1 / 5f;
    private static final float DIFFUSE_LIGHT_INTENSITY = 2f;
    private static final float SPECULAR_FACTOR = 10f;
    private static final float GLOSS_FACTOR = 50f;

    record Vector3dSwap(Vector3D first, Vector3D second) {
    }

    record Vector4dSwap(Vector4D first, Vector4D second) {
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

    public static void fillTriangle(
            GraphicsContext gContext,
            List<List<Integer>> face,
            List<Vector4D> vertexesWorld,
            List<Vector4D> vertexesChangeable,
            List<Vector3D> normalsChangeable,
            ZBuffer zBuffer
    ) {

        Vector4D a = vertexesChangeable.get(face.get(0).get(0) - 1);
        Vector4D b = vertexesChangeable.get(face.get(1).get(0) - 1);
        Vector4D c = vertexesChangeable.get(face.get(2).get(0) - 1);

        Vector3D aw = vertexesWorld.get(face.get(0).get(0) - 1).toVector3D();
        Vector3D bw = vertexesWorld.get(face.get(1).get(0) - 1).toVector3D();
        Vector3D cw = vertexesWorld.get(face.get(2).get(0) - 1).toVector3D();

        Vector3D vertexNormalA = normalsChangeable.get(face.get(0).get(0) - 1).normalize();
        Vector3D vertexNormalB = normalsChangeable.get(face.get(1).get(0) - 1).normalize();
        Vector3D vertexNormalC = normalsChangeable.get(face.get(2).get(0) - 1).normalize();

        //Сортировка по Y
        if (a.getY() > c.getY()) {
            Vector4dSwap vector4dSwap = new Vector4dSwap(c, a);
            a = vector4dSwap.first;
            c = vector4dSwap.second;

            Vector3dSwap vector3dSwap = new Vector3dSwap(cw, aw);
            aw = vector3dSwap.first;
            cw = vector3dSwap.second;

            vector3dSwap = new Vector3dSwap(vertexNormalC, vertexNormalA);
            vertexNormalA = vector3dSwap.first;
            vertexNormalC = vector3dSwap.second;
        }

        if (a.getY() > b.getY()) {
            Vector4dSwap vector4dSwap = new Vector4dSwap(b, a);
            a = vector4dSwap.first;
            b = vector4dSwap.second;

            Vector3dSwap vector3dSwap = new Vector3dSwap(bw, aw);
            aw = vector3dSwap.first;
            bw = vector3dSwap.second;

            vector3dSwap = new Vector3dSwap(vertexNormalB, vertexNormalA);
            vertexNormalA = vector3dSwap.first;
            vertexNormalB = vector3dSwap.second;
        }

        if (b.getY() > c.getY()) {
            Vector4dSwap vector4dSwap = new Vector4dSwap(c, b);
            b = vector4dSwap.first;
            c = vector4dSwap.second;

            Vector3dSwap vector3dSwap = new Vector3dSwap(cw, bw);
            bw = vector3dSwap.first;
            cw = vector3dSwap.second;

            vector3dSwap = new Vector3dSwap(vertexNormalC, vertexNormalB);
            vertexNormalB = vector3dSwap.first;
            vertexNormalC = vector3dSwap.second;
        }

        Vector4D vertexInterpolationCoefficient1 = calculateVertexInterpolationCoefficient(a, c);
        Vector3D vertexNormalKoeff01 = calculateVertexNormalsInterpolationCoefficient(vertexNormalA, vertexNormalC, a, c);
        Vector3D worldKoeff01 = calculateVertexWorldCoordsInterpolationCoefficient(aw, cw, a, c);

        Vector4D vertexInterpolationCoefficient2 = calculateVertexInterpolationCoefficient(a, b);
        Vector3D vertexNormalKoeff02 = calculateVertexNormalsInterpolationCoefficient(vertexNormalA, vertexNormalB, a, b);
        Vector3D worldKoeff02 = calculateVertexWorldCoordsInterpolationCoefficient(aw, bw, a, b);

        Vector4D vertexInterpolationCoefficient3 = calculateVertexInterpolationCoefficient(b, c);
        Vector3D vertexNormalKoeff03 = calculateVertexNormalsInterpolationCoefficient(vertexNormalB, vertexNormalC, b, c);
        Vector3D worldKoeff03 = calculateVertexWorldCoordsInterpolationCoefficient(bw, cw, b, c);


        int top = Math.max(0, (int) Math.ceil(a.getY()));
        int bottom = Math.min((int) WINDOW_HEIGHT, (int) Math.ceil(c.getY()));

        float[] ambientValues = new float[3];
        float[] diffuseValues = new float[3];
        float[] specularValues = new float[3];
        for (int y = top; y < bottom; y++) {
            Vector4D l = Vector4D.add(a, Vector4D.multiplyOnScalar(vertexInterpolationCoefficient1, y - a.getY()));
            Vector4D r = (y < b.getY())
                    ? Vector4D.add(a, Vector4D.multiplyOnScalar(vertexInterpolationCoefficient2, y - a.getY()))
                    : Vector4D.add(b, Vector4D.multiplyOnScalar(vertexInterpolationCoefficient3, y - b.getY()));

            Vector3D worldL = aw.add(worldKoeff01.scalarMultiply(y - a.getY()));
            Vector3D worldR = (y < b.getY())
                    ? aw.add(worldKoeff02.scalarMultiply(y - a.getY()))
                    : bw.add(worldKoeff03.scalarMultiply(y - b.getY()));

            Vector3D normalL = vertexNormalA.add(
                    vertexNormalKoeff01.scalarMultiply(y - a.getY())
            );
            Vector3D normalR = (y < b.getY())
                    ? vertexNormalA.add(vertexNormalKoeff02.scalarMultiply(y - a.getY()))
                    : vertexNormalB.add(vertexNormalKoeff03.scalarMultiply(y - b.getY()));


            if (l.getX() > r.getX()) {
                Vector4dSwap vector4dSwap = new Vector4dSwap(r, l);
                l = vector4dSwap.first;
                r = vector4dSwap.second;

                Vector3dSwap vector3dSwap = new Vector3dSwap(normalR, normalL);
                normalL = vector3dSwap.first;
                normalR = vector3dSwap.second;

                vector3dSwap = new Vector3dSwap(worldR, worldL);
                worldL = vector3dSwap.first;
                worldR = vector3dSwap.second;
            }

            Vector4D k = Vector4D.subtract(r, l)
                    .divideOnScalar(r.getX() - l.getX());
            Vector3D normalKoeff = normalR.subtract(normalL).scalarMultiply(1 / (r.getX() - l.getX()));
            Vector3D worldKoeff = worldR.subtract(worldL).scalarMultiply(1 / (r.getX() - l.getX()));

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
                    Vector3D normal = normalL
                            .add(
                                    normalKoeff.scalarMultiply(x - l.getX())
                            ).normalize();

                    ambientLightning(ambientValues);
                    diffuseLightning(diffuseValues, normal);
                    specularLightning(EYE.subtract(pWorld).normalize(), normal, specularValues);

                    zBuffer.setPoint(index, p.getZ());

                    int colorR = (int) (Math.min(
                            PIXEL_BASE_COLOR.getRed() * (ambientValues[0] + diffuseValues[0] + specularValues[0]) * 255,
                            255
                    ));

                    int colorG = (int) (Math.min(
                            PIXEL_BASE_COLOR.getGreen() * (ambientValues[1] + diffuseValues[1] + specularValues[1]) * 255,
                            255
                    ));
                    int colorB = (int) (Math.min(
                            PIXEL_BASE_COLOR.getBlue() * (ambientValues[2] + diffuseValues[2] + specularValues[2]) * 255,
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

    private static Vector3D calculateVertexNormalsInterpolationCoefficient(
            Vector3D normalA, Vector3D normalB,
            Vector4D a, Vector4D b
    ) {
        return normalB.subtract(normalA).scalarMultiply(1 / (b.getY() - a.getY()));
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

    private static void ambientLightning(float[] ambientValues) {
        ambientValues[0] = (float) (LIGHT_BASE_COLOR.getRed() * AMBIENT_LIGHT_INTENSITY);
        ambientValues[1] = (float) (LIGHT_BASE_COLOR.getGreen() * AMBIENT_LIGHT_INTENSITY);
        ambientValues[2] = (float) (LIGHT_BASE_COLOR.getBlue() * AMBIENT_LIGHT_INTENSITY);
    }

    private static void diffuseLightning(float[] diffuseValues, Vector3D normal) {
        float scalar = (float) Math.max(getLightIntensity(normal), 0) * DIFFUSE_LIGHT_INTENSITY;

        diffuseValues[0] = (float) (LIGHT_BASE_COLOR.getRed() * scalar);
        diffuseValues[1] = (float) (LIGHT_BASE_COLOR.getGreen() * scalar);
        diffuseValues[2] = (float) (LIGHT_BASE_COLOR.getBlue() * scalar);
    }

    private static void specularLightning(Vector3D view, Vector3D normal, float[] specularValues) {
        Vector3D reflection = reflect(LIGHT_DIRECTION.scalarMultiply(-1), normal).normalize();

        float RV = (float) Math.max(reflection.dotProduct(view), 0);
        float temp = (float) Math.pow(RV, GLOSS_FACTOR) * SPECULAR_FACTOR;

        specularValues[0] = temp;
        specularValues[1] = temp;
        specularValues[2] = temp;
    }

    private static Vector3D reflect(Vector3D vector, Vector3D normal) {
        double num = vector.dotProduct(normal);
        Vector3D vector3D = normal.scalarMultiply(num).scalarMultiply(2f);
        return vector.subtract(vector3D);
    }
}