package com.github.carlosascari;

/**
 * Container used to abstract a 32bit Integer into a RGBA Pixel.
 */
public class Pixel {
    private final int value;

    public Pixel(int value) {
        this.value = value;
    }

    public Pixel(int red, int green, int blue, int alpha) {
        this.value = red | green << 8 | blue << 16 | alpha << 24;
    }

    public int getValue() {
        return value;
    }

    public int red() {
        return this.value & 0xFF;
    }

    public int green() {
        return (this.value >> 8) & 0xFF;
    }

    public int blue() {
        return (this.value >> 16) & 0xFF;
    }

    public int alpha() {
        return (this.value >> 24) & 0xFF;
    }
}
