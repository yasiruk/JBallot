package me.yasiru.ballot;

import org.opencv.core.Mat;

/**
 * Created by wik2kassa on 12/2/2016.
 */
public class Party {
    private Mat symbol;
    private String name;

    public Party(Mat symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public Party(Mat symbol) {
        this.symbol = symbol;
    }

    public Party() {
    }

    public Mat getSymbol() {
        return symbol;
    }

    public void setSymbol(Mat symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
