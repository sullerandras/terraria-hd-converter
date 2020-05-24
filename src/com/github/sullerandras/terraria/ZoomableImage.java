package com.github.sullerandras.terraria;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ZoomableImage extends JPanel {
    private final JLabel imageView;
    private final JScrollPane scrollPane;
    private final JLabel label;

    private int zoomLevel;
    private final String imageLabel;

    private Image originalImage;
    private int originalImageWidth;
    private int originalImageHeight;
    private Image zoomedImage;

    public ZoomableImage(int zoomLevel, String imageLabel) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.zoomLevel = zoomLevel;
        this.imageLabel = imageLabel;

        label = new JLabel(imageLabel);
        this.add(label);

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
        if (image == null) {
            image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        originalImage = image;
        originalImageWidth = image.getWidth(null);
        originalImageHeight = image.getHeight(null);
        label.setText(imageLabel + " " + originalImageWidth + "x" + originalImageHeight);
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
