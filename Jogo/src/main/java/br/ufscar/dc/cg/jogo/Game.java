package br.ufscar.dc.cg.jogo;

public class Game {

    private int level;
    private Polygon polygon;
    private int count_edges;
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
        reset_level();
    }

    public void do_move() {
        count_edges++;
        if (count_edges == polygon.size()+1) {
            state = GameState.NEXT_LEVEL;
        }
    }

    public void next_level() {
        level += 1;
        reset_level();
    }

    void reset_level() {
        polygon = Polygon.generate(level + 2);
        state = GameState.PLAYING;
        count_edges = 0;
    }
}
