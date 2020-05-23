package com.github.sullerandras.terraria;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageTools {
    public static BufferedImage toBufferedImage(Image image) {
        BufferedImage buffered = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        buffered.getGraphics().drawImage(image, 0, 0 , null);
        return buffered;
    }
}
