package br.ufscar.dc.cg.jogo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author petri
 */
class Polygon {

    java.awt.Polygon _poly;
    List<Boolean> _edges_state;

    public Polygon(int n) {
        _poly = new java.awt.Polygon();
        _edges_state = new ArrayList<>(n);
        for (int i = 0; i < _edges_state.size(); ++i) {
            _edges_state.set(i, false);
        }
    }

    public static Polygon generate(int n) {
        Polygon p = new Polygon(n);
        // TODO add n vertex

        return p;
    }
}
