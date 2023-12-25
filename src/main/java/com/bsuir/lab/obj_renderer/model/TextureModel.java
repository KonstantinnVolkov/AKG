package com.bsuir.lab.obj_renderer.model;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

@Getter
@NoArgsConstructor
public class TextureModel {

    private static final int RED_PIXEL_PART_SHIFT = 16;
    private static final int GREEN_PIXEL_PART_SHIFT = 8;
    private static final int AND_PIXEL_PART_MULTIPLIER = 0xFF;

    private Image image;
    private double width;
    private double height;

    public TextureModel(Image image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public Color getPixelColor(int x, int y){
        int pixel = image.getPixelReader().getArgb(x, y);

        var r = pixel >> RED_PIXEL_PART_SHIFT & AND_PIXEL_PART_MULTIPLIER;
        var g = pixel >> GREEN_PIXEL_PART_SHIFT & AND_PIXEL_PART_MULTIPLIER;
        var b = pixel & AND_PIXEL_PART_MULTIPLIER;

        return Color.rgb(r, g, b);
    }
}
