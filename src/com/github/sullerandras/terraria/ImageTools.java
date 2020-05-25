package com.github.sullerandras.terraria;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

public class ImageTools {
    /**
     * Reads image as TYPE_INT_ARGB
     * @param file The image we want to read
     * @return The image in TYPE_INT_ARGB
     */
    public static BufferedImage readImage(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            return image;
        }
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        ColorConvertOp xformOp = new ColorConvertOp(null);
        xformOp.filter(image, newImage);
        return newImage;
    }

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
        file.getAbsoluteFile().getParentFile().mkdirs();
        BufferedImage buffered = ImageTools.toBufferedImage(image);
        ImageIO.write(buffered, "PNG", file);
    }
}
