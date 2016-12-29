package br.ufscar.dc.cg.jogo;

/**
 *
 * @author petri
 */
public class Game {

    private int level;
    private Polygon polygon;
    private GameState state;

    public int getLevel() {
        return level;
    }

    public GameState getState() {
        return state;
    }

    public void pause() {
        this.state = GameState.PAUSED;
    }

    public void resume() {
        this.state = GameState.PLAYING;
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
        state = GameState.PLAYING;
    }

    public boolean do_move() {
        return false;
    }

    public void next_level() {
        level += 1;
        polygon = Polygon.generate(level + 2);
    }

    void reset_level() {
        polygon = Polygon.generate(level + 2);
        state = GameState.PLAYING;
    }
}
