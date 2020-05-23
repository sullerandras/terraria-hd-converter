package com.github.sullerandras.terraria;

import com.blogspot.intrepidis.xBRZ;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main extends JFrame {
    private String inputImagePath;
    private JLabel actiontarget;
    private JSlider zoomLevelSlider;

    public static void main(String[] args) {
        Main main = new Main();
        main.init();
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setVisible(true);
    }

    public void init() {
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
        JLabel userName = new JLabel("Select input image:");
        leftPanel.add(userName);

//        inputImagePath = "Item_1.png";
//        inputImagePath = "temp1/NPC_4.png";
        inputImagePath = "temp1/Tiles_21.png";

        JLabel inputImageView = new JLabel();
        JScrollPane inputScrollPane = new JScrollPane(inputImageView);
        inputScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        inputScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        leftPanel.add(inputScrollPane);

        imagesPanel.add(leftPanel);

        // right panel

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        JLabel pw = new JLabel("Smoothed image:");
        rightPanel.add(pw);

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

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showImage(convertImage(inputImagePath), outputImageView);
            }
        });
        zoomLevelSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                showImage(inputImagePath, inputImageView);
                showImage(convertImage(inputImagePath), outputImageView);
            }
        });

        showImage(inputImagePath, inputImageView);
        showImage(null, outputImageView);

        this.setContentPane(mainPanel);
        this.setSize(800, 480);

        convertButton.doClick(); // click the convert button by default

        this.pack();
    }

    private String convertImage(String inputImagePath) {
        clearError();

        BufferedImage img = null;
        try {
            img = ImageIO.read(new java.io.File(inputImagePath));
        } catch (IOException e) {
            showError("Error reading input image: "+e, e);
            return null;
        }
        java.awt.Image scaledDownImage = img.getScaledInstance(img.getWidth() / 2, img.getHeight() / 2, java.awt.Image.SCALE_AREA_AVERAGING);
        File scaledDownFile = null;
        try {
            scaledDownFile = saveImage(scaledDownImage, "scaled_down.png");
        } catch (IOException e) {
            showError("Error writing image to file: "+e, e);
            return null;
        }

        java.awt.Image scaledUpImage = scaleUpImage(scaledDownFile, scaledDownImage);
        File scaledUpFile = null;
        try {
            scaledUpFile = saveImage(scaledUpImage, "scaled_up.png");
        } catch (IOException e) {
            showError("Error writing image to file: "+e, e);
            return null;
        }

        return scaledUpFile != null ? scaledUpFile.getAbsolutePath() : null;
    }

    private Image scaleUpImage(File file, java.awt.Image image) {
        final boolean frame = true;
        final BufferedImage b;
        if (frame) {
            b = toFramedBufferedImage(image);
        } else {
            b = toBufferedImage(image);
        }
        final int width = b.getWidth();
        final int height = b.getHeight();
        final int[] inputPixels = getPixelsAsInt(b);
        final int[] targetPixels = new int[width * height * 4];
        new xBRZ().scaleImage(
                xBRZ.ScaleSize.Times2,
                inputPixels,
                targetPixels,
                width,
                height,
                new xBRZ.ScalerCfg(),
                0,
                height);
        final BufferedImage scaled = getImageFromArray(targetPixels, width * 2, height * 2);
        if (frame) {
            return scaled.getSubimage(2, 2, scaled.getWidth() - 4, scaled.getHeight() - 4);
        }
        return scaled;
    }

    private static int[] getPixelsAsInt(BufferedImage image) {
        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public static BufferedImage getImageFromArray(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }

    private File saveImage(java.awt.Image image, String path) throws IOException {
        if (image == null) {
            return null;
        }
        BufferedImage buffered = toBufferedImage(image);
        File file = new java.io.File(path);
        ImageIO.write(buffered, "PNG", file);
        return file;
    }

    private static BufferedImage toBufferedImage(Image image) {
        BufferedImage buffered = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        buffered.getGraphics().drawImage(image, 0, 0 , null);
        return buffered;
    }

    private static BufferedImage toFramedBufferedImage(Image image) {
        BufferedImage buffered = new BufferedImage(image.getWidth(null) + 2, image.getHeight(null) + 2, BufferedImage.TYPE_INT_ARGB);
        buffered.getGraphics().drawImage(image, 1, 1 , null);
        return buffered;
    }

    private void showImage(String imagePath, JLabel imageView) {
        if (imagePath == null) {
            imageView.setIcon(null);
            return;
        }

        System.out.println("showImage: "+imagePath);
        BufferedImage img = null;
        try {
            img = ImageIO.read(new java.io.File(imagePath));
        } catch (IOException e) {
            showError("Error reading input image: "+e, e);
            return;
        }
        System.out.println("image size: "+img.getWidth()+"x"+img.getHeight());

        Image zoomedImage = img.getScaledInstance(img.getWidth() * zoomLevelSlider.getValue(), img.getHeight() * zoomLevelSlider.getValue(), Image.SCALE_AREA_AVERAGING);
        imageView.setIcon(new ImageIcon(toBufferedImage(zoomedImage)));
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
