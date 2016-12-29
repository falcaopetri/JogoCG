package br.ufscar.dc.cg.jogo;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Point {

    private double _x, _y;
    RGBColor color;

    // TODO mover para um lugar apropriado, talvez renomear para _TRIANGULO e _MIOLO
    public static RGBColor DEFAULT_COLOR1 = new RGBColor(0, 0.8, 0.7);
    public static RGBColor DEFAULT_COLOR2 = new RGBColor(0, 0, 1);
    public static RGBColor DEFAULT_COLOR3 = new RGBColor(1, 0, 0);

    public static Point random(double max_x, double max_y) {
        double x = Math.random() * max_x;
        double y = Math.random() * max_y;
        return new Point(x, y);
    }

    public double getX() {
        return _x;
    }

    public double getY() {
        return _y;
    }

    public Point(double x, double y) {
        _x = x;
        _y = y;
        color = new RGBColor(DEFAULT_COLOR2);
    }

    public Point(Point p) {
        _x = p.getX();
        _y = p.getY();
    }

    // Source: http://www.java-gaming.org/index.php?topic=522.0
    public void set(Point point) {
        _x = point.getX();
        _y = point.getY();
    }

    // Subtracts a point from this one
    public void subtract(Point point) {
        _x -= point.getX();
        _y -= point.getY();
    }

    // Adds a point to this one
    public void add(Point point) {
        _x += point.getX();
        _y += point.getY();
    }

    public double dot(Point point) {
        return point.getX() * _x + point.getY() * _y;
    }

    public double length() {
        double dot = dot(this);
        return Math.sqrt(dot);
    }

    public void normalise() {
        double length = length();
        _x = _x / length;
        _y = _y / length;
    }

    public double angle(Point point) {
        double angle = (this.dot(point)) / (this.length() * point.length());

        return Math.acos(angle);
    }

    public double distanceTo(Point point) {
        double dx = point.getX() - _x;
        double dy = point.getY() - _y;

        // Find length of dx,dy
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("Point (%1$f, %2$f)", getX(), getY());
    }

    Point rotate(double angle) {
        double nx = rotationX(_x, _y, angle);
        double ny = rotationY(_x, _y, angle);
        return new Point(nx, ny);
    }

    public static double rotationX(double x, double y, double ang) {
        double rad = ang * Math.PI / 180;
        double nx = cos(rad) * x + sin(rad) * -1 * y;
        return nx;
    }

    public static double rotationY(double x, double y, double ang) {
        double rad = ang * Math.PI / 180;
        double ny = sin(rad) * x + cos(rad) * y;
        return ny;
    }
}
