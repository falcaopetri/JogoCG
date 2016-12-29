package br.ufscar.dc.cg.jogo;

public class Point {

    private double _x, _y;
    RGBColor color;

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
        color = new RGBColor(0f, 0f, 1f);
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
}
