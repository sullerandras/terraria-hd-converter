package com.blogspot.intrepidis;

import com.github.sullerandras.terraria.ImageScalerInterface;

public class ImageScalerxBRZ implements ImageScalerInterface {
    @Override
    public void scaleImage(final int[] source, final int[] target, final int width, final int height) {
        new xBRZ().scaleImage(
                xBRZ.ScaleSize.Times2,
                source,
                target,
                width,
                height,
                new xBRZ.ScalerCfg(),
                0,
                height);
    }
}
