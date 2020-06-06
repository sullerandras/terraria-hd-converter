package com.github.sullerandras.terraria;

import com.blogspot.intrepidis.ImageScalerxBRZ;
import com.github.carlosascari.ImageScalerxBR;

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

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        toolbar.add(new JLabel("Zoom level:"));
        JSlider zoomLevelSlider = new JSlider(JSlider.HORIZONTAL, 1, 15, 4);
        zoomLevelSlider.setMajorTickSpacing(7);
        zoomLevelSlider.setMinorTickSpacing(1);
        zoomLevelSlider.setPaintTicks(true);
        zoomLevelSlider.setPaintLabels(true);
        toolbar.add(zoomLevelSlider);

        JLabel outputFolderLabel = new JLabel("Output folder:");
        toolbar.add(outputFolderLabel);
        JTextField outputFolder = new JTextField("temp2");
        toolbar.add(outputFolder);

        JButton xBRZConvertSelectedButton = new JButton("Convert Selected with xBRZ");
        xBRZConvertSelectedButton.setToolTipText("Converts all selected images with xBRZ filter and saves them to the \"Output folder\"");
        toolbar.add(xBRZConvertSelectedButton);
        JButton xBRZConvertAll = new JButton("Convert All with xBRZ");
        xBRZConvertAll.setToolTipText("Converts all files recursively in the input folder with xBRZ filter and saves them to the \"Output folder\" with the correct relative path");
        toolbar.add(xBRZConvertAll);

        JButton xBRConvertSelectedButton = new JButton("Convert Selected with xBR");
        xBRConvertSelectedButton.setToolTipText("Converts all selected images with xBR filter and saves them to the \"Output folder\"");
        toolbar.add(xBRConvertSelectedButton);
        JButton xBRConvertAll = new JButton("Convert All with xBR");
        xBRConvertAll.setToolTipText("Converts all files recursively in the input folder with xBR filter and saves them to the \"Output folder\" with the correct relative path");
        toolbar.add(xBRConvertAll);

        mainPanel.add(toolbar, UITools.constraints(0, 0, true, false, GridBagConstraints.NORTH));

        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new GridBagLayout());

        // file chooser

        FileChooser fileChooser = new FileChooser("temp1");

        mainContentPanel.add(fileChooser, UITools.constraints(0, 0, false, true, GridBagConstraints.WEST));

        // orignal image panel

        JPanel imagesPanel = new JPanel(new GridLayout(1, 3, 10, 10));

        ZoomableImage inputImageView = new ZoomableImage(zoomLevelSlider.getValue(), "Input image");
        imagesPanel.add(inputImageView);

        // filtered image panels

        ZoomableImage xBRZoutputImageView = new ZoomableImage(zoomLevelSlider.getValue(), "xBRZ filtered image");
        imagesPanel.add(xBRZoutputImageView);

        ZoomableImage xBRoutputImageView = new ZoomableImage(zoomLevelSlider.getValue(), "xBR filtered image");
        imagesPanel.add(xBRoutputImageView);

        mainContentPanel.add(imagesPanel, UITools.constraints(1, 0, true, true, GridBagConstraints.EAST));

        mainPanel.add(mainContentPanel, UITools.constraints(0, 1, true, true, GridBagConstraints.SOUTHWEST));

        statusBar = new StatusBar();
        mainPanel.add(statusBar, UITools.constraints(0, 2, true, false, GridBagConstraints.SOUTHWEST));

        zoomLevelSlider.addChangeListener(e -> {
            clearError();
            inputImageView.setZoomLevel(zoomLevelSlider.getValue());
            xBRZoutputImageView.setZoomLevel(zoomLevelSlider.getValue());
            xBRoutputImageView.setZoomLevel(zoomLevelSlider.getValue());
        });

        fileChooser.addFileSelectionListener(file -> {
            loadFile(file, inputImageView, xBRZoutputImageView, xBRoutputImageView);
            bindScrollbars(inputImageView, xBRZoutputImageView);
            bindScrollbars(xBRoutputImageView, xBRZoutputImageView);
        });

        xBRZConvertSelectedButton.addActionListener(e -> {
            convertFiles(fileChooser.getSelectedFiles(), fileChooser.getFolder(), outputFolder.getText(), new ImageScalerxBRZ());
        });

        xBRZConvertAll.addActionListener(e -> {
            try {
                convertFiles(fileChooser.getAllFilesRecursively(), fileChooser.getFolder(), outputFolder.getText(), new ImageScalerxBRZ());
            } catch (IOException ex) {
                showError("Error converting files: "+ex, ex);
            }
        });

        xBRConvertSelectedButton.addActionListener(e -> {
            convertFiles(fileChooser.getSelectedFiles(), fileChooser.getFolder(), outputFolder.getText(), new ImageScalerxBR());
        });

        xBRConvertAll.addActionListener(e -> {
            try {
                convertFiles(fileChooser.getAllFilesRecursively(), fileChooser.getFolder(), outputFolder.getText(), new ImageScalerxBR());
            } catch (IOException ex) {
                showError("Error converting files: "+ex, ex);
            }
        });

//        inputImagePath = "Item_1.png";
//        inputImagePath = "temp1/NPC_4.png";
//        String inputImagePath = "temp1/Tiles_21.png";
        String inputImagePath = "temp1/Sun.png";

        loadFile(new File(inputImagePath), inputImageView, xBRZoutputImageView, xBRoutputImageView);

        this.setContentPane(mainPanel);
        this.pack();
        this.setSize(800, 480);

        this.setVisible(true);
    }

    private void loadFile(File inputFile, ZoomableImage inputImageView, ZoomableImage xBRZoutputImageView, ZoomableImage xBRoutputImageView) {
        System.out.println("loading file " + inputFile);
        clearError();
        BufferedImage inputImage = null;
        try {
            inputImage = ImageTools.readImage(inputFile);
        } catch (IOException e) {
            showError("Error loading image: " + e, e);
            return;
        }
        inputImageView.setImage(inputImage);
        try {
            Image outputImage = new ImageConverter().convertImage(inputFile.getName(), inputImage, new ImageScalerxBRZ());
            xBRZoutputImageView.setImage(outputImage);
        } catch (ImageConverterException e) {
            showError("Error converting image: " + e, e);
            xBRZoutputImageView.setImage(null);
        }
        try {
            Image outputImage = new ImageConverter().convertImage(inputFile.getName(), inputImage, new ImageScalerxBR());
            xBRoutputImageView.setImage(outputImage);
        } catch (ImageConverterException e) {
            showError("Error converting image: " + e, e);
            xBRoutputImageView.setImage(null);
        }
    }

    private void convertFiles(List<File> files, File inputFolder, String outputFolder, ImageScalerInterface imageScaler) {
        clearError();
        new File(outputFolder).mkdirs();
        SwingWorker<String, Integer> task = new SwingWorker<String, Integer>() {
            @Override
            protected String doInBackground() throws Exception {
                int success = 0;
                int failure = 0;
                int lastProgress = 0;
                final long startTime = System.currentTimeMillis();

                for (int i = 0; i < files.size(); i++) {
                    final File file = files.get(i);
                    final String relativePath = getRelativeFilename(file, inputFolder);

                    BufferedImage inputImage = null;
                    try {
                        inputImage = ImageTools.readImage(file);
                    } catch (IOException e) {
                        System.out.println("Error loading image " + file.getName() + ": " + e);
                        failure++;
                    }

                    try {
                        Image outputImage = new ImageConverter().convertImage(file.getName(), inputImage, imageScaler);
                        try {
                            ImageTools.saveImage(outputImage, new File(outputFolder, relativePath));
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

    private String getRelativeFilename(File file, File base) throws IOException {
        String filePath = file.getAbsolutePath();
        String basePath = base.getAbsolutePath();
        if (filePath.startsWith(basePath)) {
            String path = filePath.substring(basePath.length());
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        }
        throw new IOException(file+" is not in "+base);
    }

    private void bindScrollbars(ZoomableImage zoomableImage1, ZoomableImage zoomableImage2) {
        zoomableImage1.bindScrollBarsTo(zoomableImage2);
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
