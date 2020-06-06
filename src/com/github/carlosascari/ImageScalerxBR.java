package com.github.carlosascari;

import com.github.sullerandras.terraria.ImageScalerInterface;

public class ImageScalerxBR implements ImageScalerInterface {
    @Override
    public void scaleImage(final int[] source, final int[] target, final int width, final int height) {
        xBR.execute(source, target, width, height);
    }
}
