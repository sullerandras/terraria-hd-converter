package com.github.sullerandras.terraria;

import com.blogspot.intrepidis.xBRZ;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

public class Main extends Application {
    private String inputImagePath;
    private Text actiontarget;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Terraria HD Converter");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        Label userName = new Label("Select input image:");
        grid.add(userName, 0, 0);

//        inputImagePath = "Item_1.png";
//        inputImagePath = "temp1/NPC_4.png";
        inputImagePath = "temp1/Tiles_21.png";

        ImageView inputImageView = new ImageView();
        ScrollPane inputScrollPane = new ScrollPane();
        inputScrollPane.setContent(inputImageView);
        grid.add(inputScrollPane, 0, 1);

        Button convertButton = new Button("Convert");
        grid.add(convertButton, 1, 0);

        Label pw = new Label("Smoothed image:");
        grid.add(pw, 2, 0);

        ImageView outputImageView = new ImageView();
        ScrollPane outputScrollPane = new ScrollPane();
        outputScrollPane.setContent(outputImageView);
        grid.add(outputScrollPane, 2, 1);

        actiontarget = new Text();
        grid.add(actiontarget, 0, 2);

        // bind scrollbars, so if i scroll one image it scrolls the other one as well
        inputScrollPane.hvalueProperty().bindBidirectional(outputScrollPane.hvalueProperty());
        inputScrollPane.vvalueProperty().bindBidirectional(outputScrollPane.vvalueProperty());

        convertButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent e) {
                showImage(convertImage(inputImagePath), outputImageView);
            }
        });

        showImage(inputImagePath, inputImageView);
        showImage(null, outputImageView);

        Scene scene = new Scene(grid, 800, 480);
        primaryStage.setScene(scene);
        primaryStage.show();

        convertButton.fire(); // click the convert button by default
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

    private void showImage(String imagePath, ImageView imageView) {
        if (imagePath == null) {
            imageView.setImage(null);
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

        Image zoomedImage = img.getScaledInstance(img.getWidth() * 8, img.getHeight() * 8, Image.SCALE_AREA_AVERAGING);
        imageView.setImage(SwingFXUtils.toFXImage(toBufferedImage(zoomedImage), null));
    }

    private void showError(String message, Exception e) {
        e.printStackTrace();
        if (actiontarget == null) {
            return;
        }
        actiontarget.setFill(Color.FIREBRICK);
        actiontarget.setText(message);
    }

    private void clearError() {
        actiontarget.setText("");
    }
}
