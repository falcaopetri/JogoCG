package br.ufscar.dc.cg.jogo;

import java.util.ArrayList;
import java.util.List;

public class Polygon {

    static public class ConvexHull2D {
        // Source: http://www.java-gaming.org/index.php?topic=522.0
        // Points is filled with points to test, then stripped down to minimal set when hull calcualted

        private ArrayList<Point> points;

        private ArrayList<Point> testedPoints;

        public ConvexHull2D() {
            points = new ArrayList<>();
            testedPoints = new ArrayList<>();
        }

        public void addPoint(float x, float y) {
            Point newPoint = new Point(x, y);

            points.add(newPoint);
        }

        public void clear() {
            points.clear();
        }

        public void calculateHull() {
            if (points.size() > 0) {
                // Holds the points of the calculated hull
                ArrayList hullPoints = new ArrayList();

                // First find an extreme point guranteed to be on the hull
                // Start from the first point and compare all others for minimal y coord
                Point startPoint = points.get(0);

                for (int i = 0; i < points.size(); i++) {
                    Point testPoint = (Point) points.get(i);

                    // Find lowest y, and lowest x if equal y values.
                    if (testPoint.getY() < startPoint.getY()) {
                        startPoint = testPoint;
                    } else if (testPoint.getY() == startPoint.getY()) {
                        if (testPoint.getX() < startPoint.getX()) {
                            startPoint = testPoint;
                        }
                    }
                }

                // Add the start point
                hullPoints.add(startPoint);

                Point currentPoint = startPoint;
                Point currentDirection = new Point(1.0f, 0.0f);
                Point nextPoint = null;

                int debug = 0;

                // March around the edge. Finish when we get back to where we started
                while (true) {
                    // Find next point with largest right turn relative to current
                    float currentAngle = 181f;
                    for (int i = 0; i < points.size(); i++) {
                        Point testPoint = (Point) points.get(i);

                        // Find angle between test and current points
                        Point testDirection = new Point(testPoint);
                        testDirection.subtract(currentPoint);
                        testDirection.normalise();

                        float testAngle = currentDirection.angle(testDirection);

                        // Update next point with test if smaller angle
                        if (testAngle < currentAngle) {
                            currentAngle = testAngle;
                            nextPoint = testPoint;
                        } else if (testAngle == currentAngle) {
                            // take point furthest away from current
                            if (currentPoint.distanceTo(testPoint) > currentPoint.distanceTo(nextPoint)) {
                                nextPoint = testPoint;
                            }
                        }
                    }

                    // Exit?
                    if (nextPoint == hullPoints.get(0) || debug > 1000) {
                        break;
                    }

                    // Add and advance
                    hullPoints.add(nextPoint);

                    currentDirection.set(nextPoint);
                    currentDirection.subtract(currentPoint);

                    currentPoint = nextPoint;
                    nextPoint = null;

                    debug++;
                }

                points = hullPoints;

            } // fi points>0
        }
    }
    //java.awt.Polygon _poly;
    List<Point> _poly;
    List<Boolean> _edges_states;
    Point _gravity_center;

    private static Polygon generateFromRandomPoints(int n) {
        Polygon poly = new Polygon();
        for (int i = 0; i < n; ++i) {
            Point p;
            do {
                p = Point.random(1.0f, 1.0f);
            } while (poly.is_inside(p));
            poly.add(p);

            poly.ccw_sort();
        }

        ConvexHull2D ch = new ConvexHull2D();
        for (Point tmp : poly._poly) {
            ch.addPoint(tmp.getX(), tmp.getY());
        }
        ch.calculateHull();

        poly = new Polygon();
        for (Point p : ch.points) {
            poly.add(p);
        }
        return poly;
    }

    public Polygon() {
        //_poly = new java.awt.Polygon();
        _poly = new ArrayList<Point>();
        _edges_states = new ArrayList<>();
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
        /*float xs[] = new float[n];
        float ys[] = new float[n];*/
        ArrayList<Float> xs = new ArrayList<>();
        ArrayList<Float> ys = new ArrayList<>();

        double x, y;
        double x0 = 0.0f, y0 = 0.0f, r = 0.5f;  // circle params
        double angles[] = new double[n];
        for (int i = 0; i < n; ++i) {
            angles[i] = Math.random() * 2 * Math.PI;
        }
        float xs2[] = new float[n];
        float ys2[] = new float[n];
        for (int i = 0; i < n; i++) {
            x = x0 + r * Math.cos(angles[i]);
            y = y0 + r * Math.sin(angles[i]);
            xs2[i] = (float) x;
            ys2[i] = (float) y;
        }
        return Polygon.create(xs2, ys2);
    }

    private static Polygon generateLikeACircle(int n) {
        /*float xs[] = new float[n];
        float ys[] = new float[n];*/
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

        float xs2[] = new float[xs.size()];
        float ys2[] = new float[xs.size()];

        for (int i = 0; i < xs.size(); ++i) {
            xs2[i] = xs.get(i);
            ys2[i] = ys.get(i);
        }
        System.out.println("xs: " + xs.size());
        return Polygon.create(xs2, ys2);
    }

    public static Polygon create(float x[], float y[]) {
        if (x.length != y.length) {
            throw new IllegalArgumentException();
        }

        Polygon p = new Polygon();

        for (int i = 0; i < x.length; ++i) {
            p.add(x[i], y[i]);
        }

        return p;
    }

    public void add(float x, float y) {
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
        ConvexHull2D ch = new ConvexHull2D();
        for (Point tmp : _poly) {
            ch.addPoint(tmp.getX(), tmp.getY());
        }
        ch.addPoint(p.getX(), p.getY());
        ch.calculateHull();
        return ch.points.size() == this.size();
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

    private void ccw_sort() {
        // TODO
        // throw new UnsupportedOperationException("Not supported yet."); 
    }

    private void calculate_gravity_center() {
        // TODO calcula o centro de massa dos pontos, e não do polígono em si
        // read: https://en.wikipedia.org/wiki/Centroid#Centroid_of_polygon
        
        float x_sum = 0;
        float y_sum = 0;

        for (Point p : _poly) {
            x_sum += p.getX();
            y_sum += p.getY();
        }

        _gravity_center = new Point(x_sum / this.size(), y_sum / this.size());
    }
}
