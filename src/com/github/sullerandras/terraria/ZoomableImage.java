package com.github.sullerandras.terraria;

import javax.swing.*;
import java.awt.*;

public class ZoomableImage extends JPanel {
    private final JLabel imageView;
    private final JScrollPane scrollPane;

    private int zoomLevel;
    private Image originalImage;
    private int originalImageWidth;
    private int originalImageHeight;
    private Image zoomedImage;

    public ZoomableImage(int zoomLevel) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.zoomLevel = zoomLevel;
        imageView = new JLabel();
        scrollPane = new JScrollPane(imageView);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane);
    }

    public JScrollBar getHorizontalScrollBar() {
        return scrollPane.getHorizontalScrollBar();
    }

    public JScrollBar getVerticalScrollBar() {
        return scrollPane.getVerticalScrollBar();
    }

    public void setImage(Image image) {
        this.originalImage = image;
        this.originalImageWidth = image.getWidth(null);
        this.originalImageHeight = image.getHeight(null);
        redrawImage();
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
        redrawImage();
    }

    private void redrawImage() {
        zoomedImage = originalImage.getScaledInstance(originalImageWidth * zoomLevel, originalImageHeight * zoomLevel, Image.SCALE_AREA_AVERAGING);
        imageView.setIcon(new ImageIcon(ImageTools.toBufferedImage(zoomedImage)));
    }
}
