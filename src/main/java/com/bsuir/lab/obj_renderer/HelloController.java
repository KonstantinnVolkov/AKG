package com.bsuir.lab.obj_renderer;

import com.bsuir.lab.obj_renderer.model.ObjectData;
import com.bsuir.lab.obj_renderer.service.drawing.Drawer;
import com.bsuir.lab.obj_renderer.util.MatrixRotations;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class HelloController {

    private long lastTime = System.nanoTime();
    private int frameCount = 0;
    private double fps = 0d;

    @FXML
    private Canvas canvas;

    private boolean isMousePressed;

    private GraphicsContext gContext;

    private Double lastX = 0.0;
    private Double lastY = 0.0;

    private static final ObjectData objectData = ObjectData.getInstance();
    private static final Drawer DRAWER = new Drawer(
            objectData.getVertexes(),
            objectData.getNormals(),
            objectData.getFaces()
    );

    private static final float ROTATION_INDEX = 0.1F;

//    @FXML
//    private Label fpsLabel;

    @FXML
    void mousePressed(MouseEvent event) {
        lastX = event.getX();
        lastY = event.getY();
        isMousePressed = true;

        handle();
        DRAWER.setupCamera(gContext);
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        isMousePressed = false;
    }

    @FXML
    void mouseDragged(MouseEvent e) {
        if (isMousePressed) {
            handle();

            double xOffset = e.getX() - lastX;
            double yOffset = e.getY() - lastY;

            lastX = e.getX();
            lastY = e.getY();

            if (e.isControlDown()) {
                DRAWER.changeTranslationMatrix((float) xOffset / 10, (float) yOffset / 10, 0);
            } else {
                Point2D path = new Point2D(xOffset, yOffset);
                MatrixRotations.rotate(path);
            }
            DRAWER.draw(gContext);
            calculateFps();
        }
    }

    @FXML
    void mouseScrolled(ScrollEvent e) {
        handle();
        if (e.getDeltaY() > 0) {
            DRAWER.zoomIn();
        } else {
            DRAWER.zoomOut();
        }
        DRAWER.draw(gContext);
        calculateFps();
    }

    private void handle() {
        gContext = canvas.getGraphicsContext2D();
        gContext.clearRect(0.0, 0.0, 1280, 720);
    }

    private void calculateFps() {
        long now = System.nanoTime();
        double elapsedTime = (now - lastTime) / 1e9;
        frameCount++;

        if (elapsedTime >= 1d) {
            fps = frameCount / elapsedTime;
            frameCount = 0;
            lastTime = now;
            System.out.printf("FPS: %.2f%n", fps);
        }
    }
}