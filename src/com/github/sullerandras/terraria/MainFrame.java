package com.github.sullerandras.terraria;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame {
    private JLabel actiontarget;

    public void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Terraria HD Converter");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

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

        mainPanel.add(toolbar, constraints(0, 0, true, false, GridBagConstraints.NORTH));

        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new GridBagLayout());

        // file chooser

        FileChooser fileChooser = new FileChooser("temp1");

        mainContentPanel.add(fileChooser, constraints(0, 0, false, true, GridBagConstraints.WEST));

        // orignal image panel

        JPanel imagesPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        ZoomableImage inputImageView = new ZoomableImage(zoomLevelSlider.getValue(), "Input image");
        leftPanel.add(inputImageView);

        imagesPanel.add(leftPanel);

        // xBRZ image panel

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        ZoomableImage outputImageView = new ZoomableImage(zoomLevelSlider.getValue(), "xBRZ smoothed image");
        rightPanel.add(outputImageView);

        actiontarget = new JLabel();
        rightPanel.add(actiontarget);

        imagesPanel.add(rightPanel);

        mainContentPanel.add(imagesPanel, constraints(1, 0, true, true, GridBagConstraints.EAST));

        mainPanel.add(mainContentPanel, constraints(0, 1, true, true, GridBagConstraints.SOUTHWEST));

        // bind scrollbars, so if i scroll one image it scrolls the other one as well
        inputImageView.getVerticalScrollBar().setModel(outputImageView.getVerticalScrollBar().getModel());
        inputImageView.getHorizontalScrollBar().setModel(outputImageView.getHorizontalScrollBar().getModel());

        zoomLevelSlider.addChangeListener(e -> {
            clearError();
            inputImageView.setZoomLevel(zoomLevelSlider.getValue());
            outputImageView.setZoomLevel(zoomLevelSlider.getValue());
        });

        fileChooser.addFileSelectionListener(file -> {
            loadFile(file, inputImageView, outputImageView);
        });

//        inputImagePath = "Item_1.png";
//        inputImagePath = "temp1/NPC_4.png";
        String inputImagePath = "temp1/Tiles_21.png";

        loadFile(new File("temp1/Tiles_21.png"), inputImageView, outputImageView);

        this.setContentPane(mainPanel);
        this.setSize(800, 480);

        this.pack();

        this.setVisible(true);
    }

    private GridBagConstraints constraints(int x, int y, boolean fillHorizontal, boolean fillVertical, int anchor) {
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

    private void loadFile(File inputFile, ZoomableImage inputImageView, ZoomableImage outputImageView) {
        System.out.println("loading file " + inputFile);
        clearError();
        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(inputFile);
        } catch (IOException e) {
            showError("Error loading image: " + e, e);
            return;
        }
        inputImageView.setImage(inputImage);
        Image outputImage = null;
        try {
            outputImage = new ImageConverter().convertImage(inputFile.getName(), inputImage);
        } catch (ImageConverterException e) {
            showError("Error converting image: " + e, e);
        }
        outputImageView.setImage(outputImage);
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
            showError("Error reading input image: " + e, e);
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
