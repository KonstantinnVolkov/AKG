package com.bsuir.lab.obj_renderer.service.drawing;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DdaGraphicService {

	public static void drawLine(Point2D startPoint, Point2D endPoint, GraphicsContext graphicsContext) {
		int roundedStartX = (int) Math.round(startPoint.getX());
		int roundedStartY = (int) Math.round(startPoint.getY());
		int roundedEndX = (int) Math.round(endPoint.getX());
		int roundedEndY = (int) Math.round(endPoint.getY());

		double deltaX = Math.abs(roundedStartX - roundedEndX);
		double deltaY = Math.abs(roundedStartY - roundedEndY);

		double length = Math.max(deltaX, deltaY);
		if (length == 0) {
			graphicsContext.getPixelWriter().setColor(roundedStartX + 650, roundedStartY + 350, Color.BLACK);
			return;
		}

		double dX = (endPoint.getX() - startPoint.getX()) / length;
		double dY = (endPoint.getY() - startPoint.getY()) / length;

		double x = startPoint.getX();
		double y = startPoint.getY();
		for (int i = 0; i <= length; i++) {
			graphicsContext.getPixelWriter().setColor((int) Math.round(x), (int) Math.round(y), Color.BLACK);
//			graphicsContext.getPixelWriter().setColor((int) Math.round(x + 650), (int) Math.round(y + 350), Color.BLACK);
			x += dX;
			y += dY;
		}
	}
}
