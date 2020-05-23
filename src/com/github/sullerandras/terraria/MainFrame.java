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
    private JSlider zoomLevelSlider;

    public void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Terraria HD Converter");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // toolbar

        JPanel toolbar = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel leftToolbar = new JPanel();
        leftToolbar.add(new JLabel("Zoom level:"));
        zoomLevelSlider = new JSlider(JSlider.HORIZONTAL, 1, 15, 8);
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
        JLabel inputImageLabel = new JLabel();
        leftPanel.add(inputImageLabel);

//        inputImagePath = "Item_1.png";
//        inputImagePath = "temp1/NPC_4.png";
        String inputImagePath = "temp1/Tiles_21.png";

        JLabel inputImageView = new JLabel();
        JScrollPane inputScrollPane = new JScrollPane(inputImageView);
        inputScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        inputScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        leftPanel.add(inputScrollPane);

        imagesPanel.add(leftPanel);

        // right panel

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        JLabel outputImageLabel = new JLabel();
        rightPanel.add(outputImageLabel);

        JLabel outputImageView = new JLabel();
        JScrollPane outputScrollPane = new JScrollPane(outputImageView);
        outputScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        outputScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        rightPanel.add(outputScrollPane);

        actiontarget = new JLabel();
        rightPanel.add(actiontarget);

        imagesPanel.add(rightPanel);

        mainPanel.add(imagesPanel);

        // bind scrollbars, so if i scroll one image it scrolls the other one as well
        inputScrollPane.getVerticalScrollBar().setModel(outputScrollPane.getVerticalScrollBar().getModel());
        inputScrollPane.getHorizontalScrollBar().setModel(outputScrollPane.getHorizontalScrollBar().getModel());

        Function<Boolean, Boolean> refreshInputImage = (unused) -> {
            showImage(inputImagePath, inputImageView, inputImageLabel, "Input image");
            return true;
        };
        Function<Boolean, Boolean> refreshOutputImage = (unused) -> {
            try {
                showImage(new ImageConverter().convertImage(inputImagePath), outputImageView, outputImageLabel, "Smoothed image");
            } catch (ImageConverterException ex) {
                showError(ex.getMessage(), ex);
            }
            return true;
        };
        convertButton.addActionListener(e -> {
            clearError();
            refreshOutputImage.apply(true);
        });
        zoomLevelSlider.addChangeListener(e -> {
            clearError();
            refreshInputImage.apply(true);
            refreshOutputImage.apply(true);
        });

        refreshInputImage.apply(true);
        refreshOutputImage.apply(true);

        this.setContentPane(mainPanel);
        this.setSize(800, 480);

        this.pack();

        this.setVisible(true);
    }

    private void showImage(String imagePath, JLabel imageView, JLabel imageLabel, String labelPrefix) {
        if (imagePath == null) {
            imageView.setIcon(null);
            imageLabel.setText(labelPrefix);
            return;
        }

        BufferedImage img;
        try {
            img = ImageIO.read(new java.io.File(imagePath));
        } catch (IOException e) {
            showError("Error reading input image: "+e, e);
            imageLabel.setText(labelPrefix);
            return;
        }
        imageLabel.setText(labelPrefix+" "+img.getWidth()+"x"+img.getHeight());

        Image zoomedImage = img.getScaledInstance(img.getWidth() * zoomLevelSlider.getValue(), img.getHeight() * zoomLevelSlider.getValue(), Image.SCALE_AREA_AVERAGING);
        imageView.setIcon(new ImageIcon(ImageTools.toBufferedImage(zoomedImage)));
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
