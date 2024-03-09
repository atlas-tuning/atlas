package com.github.manevolent.atlas.model;

public class Color {
    private int red, green, blue;

    public Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public Color() {

    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public static Color fromAwtColor(java.awt.Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue());
    }

    public java.awt.Color toAwtColor() {
        return new java.awt.Color(red, green, blue);
    }
}

