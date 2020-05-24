package com.github.sullerandras.terraria;

import com.blogspot.intrepidis.xBRZ;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ImageConverter {
    public Image convertImage(String fileName, BufferedImage img) throws ImageConverterException {
        final int w = Math.max(img.getWidth() / 2, 1);
        final int h = Math.max(img.getHeight() / 2, 1);
        Image scaledDownImage = img.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
        return scaleUpImage(fileName, scaledDownImage);
    }

    private Image scaleUpImage(String fileName, Image image) {
        final boolean frame = true;
        final BufferedImage b;
        if (frame) {
            b = toFramedBufferedImage(image);
        } else {
            b = ImageTools.toBufferedImage(image);
        }
        final int width = b.getWidth();
        final int height = b.getHeight();
        final int[] inputPixels = getPixelsAsInt(b);
        final int[] targetPixels = new int[width * height * 4];
        new xBRZ().scaleImage(
                xBRZ.ScaleSize.Times2,
                inputPixels,
                targetPixels,
                width,
                height,
                new xBRZ.ScalerCfg(),
                0,
                height);
        final BufferedImage scaled = getImageFromArray(targetPixels, width * 2, height * 2);
        if (frame) {
            return scaled.getSubimage(2, 2, scaled.getWidth() - 4, scaled.getHeight() - 4);
        }
        return scaled;
    }

    private static int[] getPixelsAsInt(BufferedImage image) {
        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public static BufferedImage getImageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    private static BufferedImage toFramedBufferedImage(Image image) {
        BufferedImage buffered = new BufferedImage(image.getWidth(null) + 2, image.getHeight(null) + 2, BufferedImage.TYPE_INT_ARGB);
        buffered.getGraphics().drawImage(image, 1, 1 , null);
        return buffered;
    }
}
