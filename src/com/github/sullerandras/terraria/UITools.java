package com.github.sullerandras.terraria;

import java.awt.*;

public class UITools {
    public static GridBagConstraints constraints(int x, int y, boolean fillHorizontal, boolean fillVertical, int anchor) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        if (fillHorizontal) {
            if (fillVertical) {
                c.fill = GridBagConstraints.BOTH;
            } else {
                c.fill = GridBagConstraints.HORIZONTAL;
            }
        } else {
            if (fillVertical) {
                c.fill = GridBagConstraints.VERTICAL;
            } else {
                c.fill = GridBagConstraints.NONE;
            }
        }
        c.anchor = anchor;
        c.weightx = fillHorizontal ? 1 : 0;
        c.weighty = fillVertical ? 1 : 0;
        return c;
    }
}
