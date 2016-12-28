package br.ufscar.dc.cg.jogo;

/**
 *
 * @author petri
 */
public class Game {
    
    private int level;
    private Polygon polygon;
    
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
