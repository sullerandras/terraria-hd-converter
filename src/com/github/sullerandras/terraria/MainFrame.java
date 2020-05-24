package com.github.sullerandras.terraria;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Function;

public class MainFrame extends JFrame {
    private JLabel actiontarget;

    public void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Terraria HD Converter");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // toolbar

        JPanel toolbar = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel leftToolbar = new JPanel();
        leftToolbar.add(new JLabel("Zoom level:"));
        JSlider zoomLevelSlider = new JSlider(JSlider.HORIZONTAL, 1, 15, 8);
        zoomLevelSlider.setMajorTickSpacing(7);
        zoomLevelSlider.setMinorTickSpacing(1);
        zoomLevelSlider.setPaintTicks(true);
        zoomLevelSlider.setPaintLabels(true);
        leftToolbar.add(zoomLevelSlider);
        toolbar.add(leftToolbar);

        JPanel rightToolbar = new JPanel();

        JButton convertButton = new JButton("Convert");
        rightToolbar.add(convertButton);

        toolbar.add(rightToolbar);

        mainPanel.add(toolbar);

        // left panel

        JPanel imagesPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

//        inputImagePath = "Item_1.png";
//        inputImagePath = "temp1/NPC_4.png";
        String inputImagePath = "temp1/Tiles_21.png";

        ZoomableImage inputImageView = new ZoomableImage(zoomLevelSlider.getValue(), "Input image");
        leftPanel.add(inputImageView);

        imagesPanel.add(leftPanel);

        // right panel

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        ZoomableImage outputImageView = new ZoomableImage(zoomLevelSlider.getValue(), "xBRZ smoothed image");
        rightPanel.add(outputImageView);

        actiontarget = new JLabel();
        rightPanel.add(actiontarget);

        imagesPanel.add(rightPanel);

        mainPanel.add(imagesPanel);

        // bind scrollbars, so if i scroll one image it scrolls the other one as well
        inputImageView.getVerticalScrollBar().setModel(outputImageView.getVerticalScrollBar().getModel());
        inputImageView.getHorizontalScrollBar().setModel(outputImageView.getHorizontalScrollBar().getModel());

        Runnable refreshInputImage = () -> {
            showImage(inputImagePath, inputImageView);
        };
        Runnable refreshOutputImage = () -> {
            try {
                showImage(new ImageConverter().convertImage(inputImagePath), outputImageView);
            } catch (ImageConverterException ex) {
                showError(ex.getMessage(), ex);
            }
        };
        convertButton.addActionListener(e -> {
            clearError();
            refreshOutputImage.run();
        });
        zoomLevelSlider.addChangeListener(e -> {
            clearError();
            inputImageView.setZoomLevel(zoomLevelSlider.getValue());
            outputImageView.setZoomLevel(zoomLevelSlider.getValue());
        });

        refreshInputImage.run();
        refreshOutputImage.run();

        this.setContentPane(mainPanel);
        this.setSize(800, 480);

        this.pack();

        this.setVisible(true);
    }

    private void showImage(String imagePath, ZoomableImage imageView) {
        if (imagePath == null) {
            imageView.setImage(null);
            return;
        }

        BufferedImage img;
        try {
            img = ImageIO.read(new java.io.File(imagePath));
        } catch (IOException e) {
            showError("Error reading input image: "+e, e);
            return;
        }

        imageView.setImage(img);
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        if (actiontarget == null) {
            return;
        }
        actiontarget.setForeground(Color.RED);
        actiontarget.setText(message);
    }

    private void clearError() {
        actiontarget.setText("");
    }
}
