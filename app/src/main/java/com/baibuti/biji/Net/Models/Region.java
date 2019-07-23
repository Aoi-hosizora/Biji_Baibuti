package com.baibuti.biji.Net.Models;

import java.io.Serializable;

public class Region implements Serializable {

    private Point size;
    private int cnt;
    private Frame[] frames;

    public Region(Point size, int cnt, Frame[] frames) {
        this.size = size;
        this.cnt = cnt;
        this.frames = frames;
    }

    public Region(int sizeX, int sizeY, int cnt, Frame[] frames) {
        this.size = new Point(sizeX, sizeY);
        this.cnt = cnt;
        this.frames = frames;
    }

    public Point getSize() {
        return size;
    }

    public void setSize(Point size) {
        this.size = size;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public Frame[] getFrames() {
        return frames;
    }

    public void setFrames(Frame[] frames) {
        this.frames = frames;
    }

    public static class Point implements Serializable {

        private int x;
        private int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    public static class Frame implements Serializable {

        private Point[] points;
        private double scores;
        private String ocr;

        public Frame(Point[] points, double scores, String ocr) {
            this.points = points;
            this.scores = scores;
            this.ocr = ocr;
        }

        public Frame(String ocr, double scores, Point[] points) {
            this.points = points;
            this.scores = scores;
            this.ocr = ocr;
        }

        public Point[] getPoints() {
            return points;
        }

        public void setPoints(Point[] points) {
            this.points = points;
        }

        public double getScores() {
            return scores;
        }

        public void setScores(double scores) {
            this.scores = scores;
        }

        public String getOcr() {
            return ocr;
        }

        public void setOcr(String ocr) {
            this.ocr = ocr;
        }
    }
}
