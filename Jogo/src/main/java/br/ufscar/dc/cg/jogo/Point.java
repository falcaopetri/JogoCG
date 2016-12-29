package br.ufscar.dc.cg.jogo;

public class Point {

    public static Point random(float max_x, float max_y) {
        float x = (float) Math.random() * max_x;
        float y = (float) Math.random() * max_y;
        return new Point(x, y);
    }

    private float _x, _y;

    public float getX() {
        return _x;
    }

    public float getY() {
        return _y;
    }

    public Point(float x, float y) {
        _x = x;
        _y = y;
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

    public float dot(Point point) {
        return point.getX() * _x + point.getY() * _y;
    }

    public float length() {
        float dot = dot(this);
        return (float) Math.sqrt(dot);
    }

    public void normalise() {
        float length = length();
        _x = _x / length;
        _y = _y / length;
    }

    public float angle(Point point) {
        float angle = (this.dot(point)) / (this.length() * point.length());

        return (float) Math.acos(angle);
    }

    public float distanceTo(Point point) {
        float dx = point.getX() - _x;
        float dy = point.getY() - _y;

        // Find length of dx,dy
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString() {
        return String.format("Point (%1$f, %2$f)", getX(), getY());
    }
}
