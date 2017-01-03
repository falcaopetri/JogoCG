package br.ufscar.dc.cg.jogo;

import java.util.ArrayList;
import java.util.List;

public class Polygon {

    int intersectAfterRotation(double angle) {
        int p = -1;
        for (int i = 0; i < _poly.size(); i++) {
            Point curr = this._poly.get(i).rotate(angle);
            Point next = this._poly.get((i + 1) % _poly.size()).rotate(angle);

            if (next.getX() < 0 && 0 <= curr.getX()) {

                double m = (next.getY() - curr.getY()) / (next.getX() - curr.getX());
                //y  = m (x – x1) + y1.
                colide = m * (0.0 - curr.getX()) + curr.getY();
                p = i;
                break;
            }
        }

        return p;
    }

    private Pair<Double, Double> min_max_angle(Point p) {
        double angle_min = Double.MAX_VALUE;
        double angle_max = Double.MIN_VALUE;

        if (_poly.size() < 2) {
            return new Pair<Double, Double>(angle_min, angle_max);
        }

        _poly.add(p);

        for (int i = 0; i < _poly.size() + 1; ++i) {
            Point p1 = _poly.get((i) % _poly.size());
            Point p2 = _poly.get((i + 1) % _poly.size());
            Point p3 = _poly.get((i + 2) % _poly.size());

            Point v1 = new Point(p2);
            v1.subtract(p1);
            Point v2 = new Point(p3);
            v2.subtract(p2);

            angle_min = Math.min(angle_min, v1.angle(v2));
            angle_max = Math.max(angle_max, v1.angle(v2));
        }

        _poly.remove(p);
        return new Pair<Double, Double>(angle_min, angle_max);
    }

    //java.awt.Polygon _poly;
    List<Point> _poly;
    List<Boolean> _edges_states;
    Point _gravity_center;
    double colide;

    private static Polygon generateFromRandomPoints(int n) {
        int tries_left = 100000;
        Polygon poly = new Polygon();
        Pair<Double, Double> angle_pair = null;

        for (int i = 0; i < n; ++i) {
            Point p = null;

            do {
                if (tries_left == 0) {
                    //    throw new InstantiationError("too many tries");
                    break;
                }
                p = Point.random(1.25, 1.25);
                angle_pair = poly.min_max_angle(p);

                tries_left--;

            } while (p.min_distance(poly._poly) < 0.1 || angle_pair.getFirst() < 0.35 || poly.is_inside(p)/* || angle_pair.getSecond() > 2.7*/);
            System.out.println("angle: " + angle_pair);
            poly.add(p);
        }
        System.out.println();

        Polygon new_poly = new Polygon();
        for (Point p : ConvexHull2D.calculateHull(poly._poly)) {
            new_poly.add(p);
            Point p_dup = new Point(p);
            new_poly.add(p_dup);
        }

        new_poly.bringCenterToOrigin();

        return new_poly;
    }

    public Polygon() {
        //_poly = new java.awt.Polygon();
        _poly = new ArrayList<>();
        _edges_states = new ArrayList<>();
    }

    public void bringCenterToOrigin() {
        List<Point> new_poly = new ArrayList<>();
        for (Point p : _poly) {
            new_poly.add(new Point(p.getX() - _gravity_center.getX(), p.getY() - _gravity_center.getY()));
        }
        _poly = new_poly;
        calculate_gravity_center();

        // TODO era pra esse assert valer? (porque ele não está valendo)
        /*if (_gravity_center.getX() != _gravity_center.getY() || _gravity_center.getX() != 0) {
            throw new AssertionError();
        }*/
    }

    public static Polygon generate(int n) {
        if (n < 3) {
            throw new IllegalArgumentException("não existe polígono com menos que 3 vértices");
        }
        // TODO generate a random Polygon
        //return generateFromCircle(n);
        //return generateLikeACircle(n);
        return generateFromRandomPoints(n);
    }

    private static Polygon generateFromCircle(int n) {
        /*double xs[] = new double[n];
        double ys[] = new double[n];*/
        ArrayList<Float> xs = new ArrayList<>();
        ArrayList<Float> ys = new ArrayList<>();

        double x, y;
        double x0 = 0.0f, y0 = 0.0f, r = 0.5f;  // circle params
        double angles[] = new double[n];
        for (int i = 0; i < n; ++i) {
            angles[i] = Math.random() * 2 * Math.PI;
        }
        double xs2[] = new double[n];
        double ys2[] = new double[n];
        for (int i = 0; i < n; i++) {
            x = x0 + r * Math.cos(angles[i]);
            y = y0 + r * Math.sin(angles[i]);
            xs2[i] = (double) x;
            ys2[i] = (double) y;
        }
        return Polygon.create(xs2, ys2);
    }

    private static Polygon generateLikeACircle(int n) {
        /*double xs[] = new double[n];
        double ys[] = new double[n];*/
        ArrayList<Float> xs = new ArrayList<>();
        ArrayList<Float> ys = new ArrayList<>();

        // Source: http://stackoverflow.com/a/21724845
        double x0 = 0.0f, y0 = 0.0f;  // circle params
        double rx = Math.random(), ry = Math.random();
        double x, y;

        for (double a = 0.0f; a < 2.0f * Math.PI;) // full circle
        {
            x = x0 + rx * Math.cos(a);
            y = y0 + ry * Math.sin(a);
            a += (60f / n + ((60f - 60f / n) * Math.random())) * Math.PI / 180.0f;              // random angle step < 20,60 > degrees

            // here add your x,y point to polygon
            xs.add(new Float(x));
            ys.add(new Float(y));
        }

        double xs2[] = new double[xs.size()];
        double ys2[] = new double[xs.size()];

        for (int i = 0; i < xs.size(); ++i) {
            xs2[i] = xs.get(i);
            ys2[i] = ys.get(i);
        }
        //System.out.println("xs: " + xs.size());
        return Polygon.create(xs2, ys2);
    }

    public static Polygon create(double x[], double y[]) {
        if (x.length != y.length) {
            throw new IllegalArgumentException();
        }

        Polygon p = new Polygon();

        for (int i = 0; i < x.length; ++i) {
            p.add(x[i], y[i]);

        }

        return p;
    }

    public void add(double x, double y) {
        add(new Point(x, y));

    }

    public void add(Point p) {
        _poly.add(p);
        _edges_states.add(false);

        // TODO recalcula o centro de massa a cada ponto novo inserido - kind of a bad idea para um polígono grande...
        calculate_gravity_center();
    }

    public List<Boolean> getEdgesStates() {
        return _edges_states;
    }

    public int size() {
        return _poly.size();
    }

    private boolean is_inside(Point p) {
        // TODO retorna false caso o polígono tenha menos que três vértices.
        // Isso é um comportamento adequado?
        if (this.size() < 3) {
            return false;
        }

        return is_inside_convex_hull(p);
        // return is_inside_maratona(p);
    }

    private boolean is_inside_convex_hull(Point p) {
        List<Point> points = new ArrayList<>(_poly);
        points.add(p);

        return ConvexHull2D.calculateHull(points).size() == this.size() + 1;
    }

    private boolean is_inside_maratona(Point p) {
        boolean c = false;
        for (int i = 0, j = this.size() - 1; i < this.size(); j = i++) {
            if (((_poly.get(i).getY() > p.getY()) != (_poly.get(j).getY() > p.getY()))
                    && (p.getX() < (_poly.get(j).getX() - _poly.get(i).getX()) * (p.getY() - _poly.get(i).getY()) / (_poly.get(j).getY() - _poly.get(i).getY())
                    + _poly.get(i).getX())) {
                c = !c;
            }
        }

        return c;
    }

    private void calculate_gravity_center() {
        // read: https://en.wikipedia.org/wiki/Centroid#Centroid_of_polygon
        // _gravity_center =  calculate_gravity_center_mean();
        _gravity_center = calculate_gravity_center_centroid();
    }

    private Point calculate_gravity_center_mean() {
        double x_sum = 0;
        double y_sum = 0;

        for (Point p : _poly) {
            x_sum += p.getX();
            y_sum += p.getY();
        }

        return new Point(x_sum / this.size(), y_sum / this.size());
    }

    private Point calculate_gravity_center_centroid() {
        // Source: http://stackoverflow.com/a/33852627

        double twicearea = 0;
        double x = 0;
        double y = 0;
        Point p1, p2;
        double f;
        Point off = _poly.get(0);
        for (int i = 0, j = _poly.size() - 1; i < _poly.size(); j = i++) {
            p1 = _poly.get(i);
            p2 = _poly.get(j);
            f = (p1.getX() - off.getX()) * (p2.getY() - off.getY()) - (p2.getX() - off.getX()) * (p1.getY() - off.getY());
            twicearea += f;
            x += (p1.getX() + p2.getX() - 2 * off.getX()) * f;
            y += (p1.getY() + p2.getY() - 2 * off.getY()) * f;
        }

        f = twicearea * 3;

        return new Point(x / f + off.getX(), y / f + off.getY());

    }
}
