package com.bsuir.lab.obj_renderer.service.drawing;

import com.bsuir.lab.obj_renderer.model.Vector4D;
import com.bsuir.lab.obj_renderer.service.drawing.rasterization.ZBuffer;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

import static com.bsuir.lab.obj_renderer.model.WindowConstants.*;

public class DdaGraphicService {

	public static final Color LINE_COLOR = Color.GOLD;

	public static void drawLine(Point2D startPoint, Point2D endPoint, GraphicsContext graphicsContext) {
		int roundedStartX = (int) Math.round(startPoint.getX());
		int roundedStartY = (int) Math.round(startPoint.getY());
		int roundedEndX = (int) Math.round(endPoint.getX());
		int roundedEndY = (int) Math.round(endPoint.getY());

		double deltaX = Math.abs(roundedStartX - roundedEndX);
		double deltaY = Math.abs(roundedStartY - roundedEndY);

		double length = Math.max(deltaX, deltaY);
		if (length == 0) {
			graphicsContext.getPixelWriter().setColor(roundedStartX, roundedStartY, LINE_COLOR);
			return;
		}

		double dX = (endPoint.getX() - startPoint.getX()) / length;
		double dY = (endPoint.getY() - startPoint.getY()) / length;

		double x = startPoint.getX();
		double y = startPoint.getY();
		for (int i = 0; i <= length; i++) {
			graphicsContext.getPixelWriter().setColor((int) Math.round(x), (int) Math.round(y), LINE_COLOR);
			x += dX;
			y += dY;
		}
	}

	public static void drawLine(
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

//			graphicsContext.getPixelWriter().setColor((int) x, (int) y, lineColor);
		}
/*		double length = Math.max(deltaX, deltaY);
		if (length == 0) {
			graphicsContext.getPixelWriter().setColor(startX, startY, lineColor);
			return;
		}

		double dX = (endPoint.getX() - startPoint.getX()) / length;
		double dY = (endPoint.getY() - startPoint.getY()) / length;

		double x = startPoint.getX();
		double y = startPoint.getY();
		for (int i = 0; i <= length; i++) {
			graphicsContext.getPixelWriter().setColor((int) Math.round(x), (int) Math.round(y), lineColor);
			x += dX;
			y += dY;
		}*/
	}

	public static void fillTriangle(
			GraphicsContext gContext,
			List<List<Integer>> face,
			List<Vector4D> vertexesChangeable,
			ZBuffer zBuffer,
			Color color
	) {
		Vector4D a = vertexesChangeable.get(face.get(0).get(0) - 1);
		Vector4D b = vertexesChangeable.get(face.get(1).get(0) - 1);
		Vector4D c = vertexesChangeable.get(face.get(2).get(0) - 1);

		Vector4D tmp;

		if (a.getY() > c.getY()) {
			tmp = a;
			a = c;
			c = tmp;
		}

		if (a.getY() > b.getY()) {
			tmp = a;
			a = b;
			b = tmp;
		}

		if (b.getY() > c.getY()) {
			tmp = b;
			b = c;
			c = tmp;
		}

		Vector4D k1 = (Vector4D.subtract(c, a)).divideOnScalar(c.getY() - a.getY());
		Vector4D k2 = (Vector4D.subtract(b, a)).divideOnScalar(b.getY() - a.getY());
		Vector4D k3 = (Vector4D.subtract(c, b)).divideOnScalar(c.getY() - b.getY());

		int top = Math.max(0, (int) Math.ceil(a.getY()));
		int bottom = Math.min((int) WINDOW_HEIGHT, (int) Math.ceil(c.getY()));

		for (int y = top; y < bottom; y++) {
			Vector4D l = Vector4D.add(
					a,
					Vector4D.multiplyOnScalar(k1, y - a.getY())
			);
			Vector4D r = (y < b.getY()) ?
					Vector4D.add(
							a,
							Vector4D.multiplyOnScalar(k2, y - a.getY())
					)
					:
					Vector4D.add(
							b,
							Vector4D.multiplyOnScalar(k3, y - b.getY())
					);

			if (l.getX() > r.getX()) {
				tmp = l;
				l = r;
				r = tmp;
			}
			Vector4D k = Vector4D.subtract(r, l)
					.divideOnScalar(r.getX() - l.getX());

			int left = Math.max(0, (int) Math.ceil(l.getX()));
			int right = Math.min((int) WINDOW_WIDTH, (int) Math.ceil(r.getX()));

			for (int x = left; x < right; x++) {
				Vector4D p = Vector4D.add(
						l,
						Vector4D.multiplyOnScalar(k, x - l.getX())
				);

				int index = (int) (y * WINDOW_WIDTH + x);
				if (p.getZ() < zBuffer.get(index)) {
					zBuffer.setPoint(index, p.getZ());
					gContext.getPixelWriter().setColor(x, y, color);
				}
			}

		}


	}
}
