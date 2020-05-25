package com.github.sullerandras.terraria;

import com.blogspot.intrepidis.xBRZ;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

public class ImageConverter {
    public Image convertImage(String fileName, BufferedImage img) throws ImageConverterException {
        final int w = Math.max(img.getWidth() / 2, 1);
        final int h = Math.max(img.getHeight() / 2, 1);

        img = clipImage(img);
        Image scaledDownImage = img.getScaledInstance(w, h, Image.SCALE_AREA_AVERAGING);
        return scaleUpImage(fileName, scaledDownImage);
    }

    /**
     * If the image width or height is odd then it cuts down the redundant column or row. If the image size is
     * even then it simply returns the `img` param.
     * @param img
     * @throws ImageConverterException
     */
    private BufferedImage clipImage(BufferedImage img) throws ImageConverterException {
        if (img.getWidth() == 1 || img.getHeight() == 1 || img.getWidth() % 2 == 0) {
            return img;
        }
        BufferedImage clippedImg = img.getSubimage(0, 0, img.getWidth() - 1, img.getHeight() & 0xfffffffe);
        try {
            checkIfAlreadySmoothed(clippedImg);
            return clippedImg;
        } catch (ImageConverterException e) {
            clippedImg = img.getSubimage(1, 0, img.getWidth() - 1, img.getHeight());
            checkIfAlreadySmoothed(clippedImg);
            return clippedImg;
        }
    }

    private void checkIfAlreadySmoothed(BufferedImage img) throws ImageConverterException {
        final int[] pixels = getPixelsAsInt(img);
        int nonEqualPixels = 0;
        for (int i = 0; i + 1 < pixels.length; i += 2) {
            if (pixels[i] != pixels[i + 1]) {
                nonEqualPixels++;
                pixels[i] = 0xff000000;
                pixels[i + 1] = 0xff000000;
            }
        }
        if (nonEqualPixels > 10) { // if not all pixels are "doubled"
            try {
                ImageTools.saveImage(img, new File("wrong_image_original.png"));
                ImageTools.saveImage(getImageFromArray(pixels, img.getWidth(), img.getHeight()), new File("wrong_image.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw new ImageConverterException("Image is probably already converted, found " + nonEqualPixels + " wrong pixels", null);
        }
    }

    private Image scaleUpImage(String fileName, Image image) throws ImageConverterException {
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

    private static int[] getPixelsAsInt(BufferedImage image) throws ImageConverterException {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
//        DataBuffer buffer = image.getRaster().getDataBuffer();
//        if (buffer instanceof DataBufferInt) {
//            return ((DataBufferInt) buffer).getData();
//        } else {
//            throw new ImageConverterException("Unsupported DataBuffer: " + buffer.getClass().getName(), null);
//        }
    }

    public static BufferedImage getImageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    private static BufferedImage toFramedBufferedImage(Image image) {
        BufferedImage buffered = new BufferedImage(image.getWidth(null) + 2, image.getHeight(null) + 2, BufferedImage.TYPE_INT_ARGB);
        buffered.getGraphics().drawImage(image, 1, 1, null);
        return buffered;
    }
}
