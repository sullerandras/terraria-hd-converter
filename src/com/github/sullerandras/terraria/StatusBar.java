package com.github.sullerandras.terraria;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {
    private final JLabel label;
    private final JProgressBar progressBar;

    public StatusBar() {
        super();
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel(" ");
        progressBar = new JProgressBar(0, 100);
        progressBar.setVisible(false);
        add(label);
        add(progressBar);
    }

    public void setText(String message) {
        label.setText(message);
        progressBar.setVisible(false);
    }

    public void setProgress(double progress) {
        setText("Work in progress");
        progressBar.setVisible(true);
        progressBar.setValue((int) (100 * progress));
    }
}
