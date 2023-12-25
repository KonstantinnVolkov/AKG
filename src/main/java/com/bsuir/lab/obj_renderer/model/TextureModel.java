package com.bsuir.lab.obj_renderer.model;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class TextureModel {

    private static final int RED_PIXEL_PART_SHIFT = 16;
    private static final int GREEN_PIXEL_PART_SHIFT = 8;
    private static final int AND_PIXEL_PART_MULTIPLIER = 0xFF;

    private Image image;
    private double width;
    private double height;
    private PixelReader pixelReader;
    private int[] pixelData;

    public TextureModel(Image image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.pixelReader = image.getPixelReader();
        this.pixelData = new int[(int) (width * height)];
        image.getPixelReader().getPixels(0, 0, (int) width, (int) height, PixelFormat.getIntArgbInstance(), pixelData, 0, (int) width);
    }

    public Color getPixelColor(int x, int y){
        int pixel = pixelData[(int) (y * width + x)];

        int r = (pixel >>> RED_PIXEL_PART_SHIFT) & AND_PIXEL_PART_MULTIPLIER;
        int g = (pixel >>> GREEN_PIXEL_PART_SHIFT) & AND_PIXEL_PART_MULTIPLIER;
        int b = pixel & AND_PIXEL_PART_MULTIPLIER;

        return Color.rgb(r, g, b);
    }
}
