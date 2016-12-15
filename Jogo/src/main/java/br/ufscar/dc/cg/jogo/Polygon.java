package br.ufscar.dc.cg.jogo;

import java.util.ArrayList;
import java.util.List;

class Polygon {

    //java.awt.Polygon _poly;
    List<Point> _poly;
    List<Boolean> _edges_states;

    public Polygon(int n) {
        //_poly = new java.awt.Polygon();
        _poly = new ArrayList<Point>();
        _edges_states = new ArrayList<>(n);
        for (int i = 0; i < _edges_states.size(); ++i) {
            _edges_states.set(i, false);
        }
    }

    public static Polygon generate(int n) {
        // TODO generate a random Polygon
        float x[] = new float[]{-0.3f, 0.3f, 0.3f};
        float y[] = new float[]{-0.3f, -0.3f, 0.3f};

        return Polygon.create(x, y);
    }

    public static Polygon create(float x[], float y[]) {
        if (x.length != y.length) {
            throw new IllegalArgumentException();
        }

        Polygon p = new Polygon(x.length);

        for (int i = 0; i < x.length; ++i) {
            p.add(x[i], y[i]);
        }

        return p;
    }

    public void add(float x, float y) {
        //_poly.addPoint(x, y);
        _poly.add(new Point(x, y));
    }

    public List<Boolean> getEdgesStates() {
        return _edges_states;
    }
}
