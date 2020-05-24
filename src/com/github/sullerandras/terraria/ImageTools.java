package com.github.sullerandras.terraria;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageTools {
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        BufferedImage buffered = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        buffered.getGraphics().drawImage(image, 0, 0 , null);
        return buffered;
    }

    public static void saveImage(Image image, File file) throws IOException {
        if (image == null) {
            return;
        }
        BufferedImage buffered = ImageTools.toBufferedImage(image);
        ImageIO.write(buffered, "PNG", file);
    }
}
