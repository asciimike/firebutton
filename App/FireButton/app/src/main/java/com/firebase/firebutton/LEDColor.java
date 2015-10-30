package com.firebase.firebutton;

/**
 * Created by mpmcdonald on 10/28/15.
 */
public class LEDColor {

    private int r;
    private int g;
    private int b;

    public LEDColor() {}

    public LEDColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }
}
