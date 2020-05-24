package com.github.sullerandras.terraria;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainFrame extends JFrame {
    private StatusBar statusBar;

    public void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Terraria HD Converter");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        // toolbar

        JPanel toolbar = new JPanel(new FlowLayout());

        toolbar.add(new JLabel("Zoom level:"));
        JSlider zoomLevelSlider = new JSlider(JSlider.HORIZONTAL, 1, 15, 8);
        zoomLevelSlider.setMajorTickSpacing(7);
        zoomLevelSlider.setMinorTickSpacing(1);
        zoomLevelSlider.setPaintTicks(true);
        zoomLevelSlider.setPaintLabels(true);
        toolbar.add(zoomLevelSlider);

        JLabel outputFolderLabel = new JLabel("Output folder:");
        toolbar.add(outputFolderLabel);
        JTextField outputFolder = new JTextField("temp2");
        toolbar.add(outputFolder);
        JButton convertSelectedButton = new JButton("Convert Selected");
        convertSelectedButton.setToolTipText("Converts all selected images and saves them to the \"Output folder\"");
        toolbar.add(convertSelectedButton);

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

        imagesPanel.add(rightPanel);

        mainContentPanel.add(imagesPanel, constraints(1, 0, true, true, GridBagConstraints.EAST));

        mainPanel.add(mainContentPanel, constraints(0, 1, true, true, GridBagConstraints.SOUTHWEST));

        statusBar = new StatusBar();
        mainPanel.add(statusBar, constraints(0, 2, true, false, GridBagConstraints.SOUTHWEST));

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

        convertSelectedButton.addActionListener(e -> {
            convertFiles(fileChooser.getSelectedFiles(), outputFolder.getText());
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

    private void convertFiles(List<File> files, String outputFolder) {
        clearError();
        new File(outputFolder).mkdirs();
        SwingWorker<String, Integer> task = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                int success = 0;
                int failure = 0;
                int lastProgress = 0;
                final long startTime = System.currentTimeMillis();

                for (int i = 0; i < files.size(); i++) {
                    File file = files.get(i);
                    BufferedImage inputImage = null;
                    try {
                        inputImage = ImageIO.read(file);
                    } catch (IOException e) {
                        System.out.println("Error loading image " + file.getName() + ": " + e);
                        failure++;
                    }

                    try {
                        Image outputImage = new ImageConverter().convertImage(file.getName(), inputImage);
                        try {
                            ImageTools.saveImage(outputImage, new File(outputFolder, file.getName()));
                            success++;
                        } catch (IOException e) {
                            System.out.println("Error saving image " + file.getName() + ": " + e);
                            failure++;
                        }
                    } catch (ImageConverterException e) {
                        System.out.println("Error converting image " + file.getName() + ": " + e);
                        failure++;
                    }
                    int progress = i * 100 / files.size();
                    if (progress != lastProgress) {
                        setProgress(progress);
                        lastProgress = progress;
                    }
                }
                String message = "Processed " + (success + failure) + " images in " + ((System.currentTimeMillis() - startTime) / 1000.0) + " sec";
                if (failure != 0) {
                    message += " (" + success + " was successful, " + failure + " has failed)";
                }
                return message;
            }

            @Override
            protected void done() {
                try {
                    showInfo(get());
                } catch (InterruptedException | ExecutionException e) {
                    showError("Error in done: "+e, e);
                }
            }
        };

        task.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                 showProgress(((Integer)evt.getNewValue()) / 100.0);
             }
            }
        });

        task.execute();
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        if (statusBar == null) {
            return;
        }
        statusBar.setForeground(Color.RED);
        statusBar.setText(message);
    }

    private void showInfo(String message) {
        if (statusBar == null) {
            return;
        }
        statusBar.setForeground(Color.BLACK);
        statusBar.setText(message);
    }

    private void showProgress(double progress) {
        if (statusBar == null) {
            return;
        }
        statusBar.setProgress(progress);
    }

    private void clearError() {
        statusBar.setText("");
    }
}
