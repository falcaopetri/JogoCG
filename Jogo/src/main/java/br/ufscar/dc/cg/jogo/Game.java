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

    public void do_move(int i) {
        if (polygon._poly.get(i).state == PointState.USED) {
            state = GameState.GAME_OVER;
            return;
        }

        polygon._poly.get(i).color = new RGBColor(Point.DEFAULT_COLOR3);
        polygon._poly.get(i).state = PointState.USED;
        int next_vertex = (i + 1) % polygon._poly.size();
        polygon._poly.get(next_vertex).color = new RGBColor(Point.DEFAULT_COLOR3);
        polygon._poly.get(next_vertex).state = PointState.USED;
        count_edges -= 1;

        if (count_edges == 0) {
            state = GameState.NEXT_LEVEL;
        }
    }

    public int getCount_edges() {
        return count_edges;
    }

    public void next_level() {
        level += 1;
        reset_level();
    }

    void reset_level() {
        polygon = Polygon.generate(level + 2);
        state = GameState.PLAYING;
        count_edges = polygon.size() / 2;
    }
}
