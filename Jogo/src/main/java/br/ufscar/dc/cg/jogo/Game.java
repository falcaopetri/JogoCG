package br.ufscar.dc.cg.jogo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author petri
 */
public class Game {

    private int level;
    private Polygon polygon;
    public List<Float> corR;

    public void attCorR() {
        corR = new ArrayList<Float>();
        for (int i = 0; i <= level + 2; i++) {
            corR.add(new Float(0.0));
            System.out.println(corR.get(i));
        }
    }

    public int getLevel() {
        return level;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public Game() {
        reset();
    }

    public void reset() {
        level = 1;
        polygon = Polygon.generate(level + 2);
    }

    public boolean do_move() {
        return false;
    }

    public void next_level() {
        level += 1;
        polygon = Polygon.generate(level + 2);
    }
}
