package com.github.sullerandras.terraria;

import com.blogspot.intrepidis.xBRZ;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

public class ImageConverter {
    public String convertImage(String inputImagePath) throws ImageConverterException {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new java.io.File(inputImagePath));
        } catch (IOException e) {
            throw new ImageConverterException("Error reading input image: "+e, e);
        }
        java.awt.Image scaledDownImage = img.getScaledInstance(img.getWidth() / 2, img.getHeight() / 2, java.awt.Image.SCALE_AREA_AVERAGING);
        File scaledDownFile = null;
        try {
            scaledDownFile = saveImage(scaledDownImage, "scaled_down.png");
        } catch (IOException e) {
            throw new ImageConverterException("Error writing image to file: "+e, e);
        }

        Image scaledUpImage = scaleUpImage(scaledDownFile, scaledDownImage);
        File scaledUpFile = null;
        try {
            scaledUpFile = saveImage(scaledUpImage, "scaled_up.png");
        } catch (IOException e) {
            throw new ImageConverterException("Error writing image to file: "+e, e);
        }

        return scaledUpFile != null ? scaledUpFile.getAbsolutePath() : null;
    }

    private Image scaleUpImage(File file, java.awt.Image image) {
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

    private File saveImage(java.awt.Image image, String path) throws IOException {
        if (image == null) {
            return null;
        }
        BufferedImage buffered = ImageTools.toBufferedImage(image);
        File file = new java.io.File(path);
        ImageIO.write(buffered, "PNG", file);
        return file;
    }

    private static BufferedImage toFramedBufferedImage(Image image) {
        BufferedImage buffered = new BufferedImage(image.getWidth(null) + 2, image.getHeight(null) + 2, BufferedImage.TYPE_INT_ARGB);
        buffered.getGraphics().drawImage(image, 1, 1 , null);
        return buffered;
    }
}
