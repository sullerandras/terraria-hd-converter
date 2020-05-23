package com.github.sullerandras.terraria;

public class Main {
    private String inputImagePath;

    public static void main(String[] args) {
        Main main = new Main();
        main.init();
    }

    private void init() {
        new MainFrame().init();
    }
}
