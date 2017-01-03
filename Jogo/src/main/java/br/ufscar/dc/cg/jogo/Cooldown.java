package br.ufscar.dc.cg.jogo;

import java.util.Timer;
import java.util.TimerTask;

public class Cooldown {

    double max;
    double refresh_rate;
    double decrease_rate;
    double curr_value;
    final Timer timer;
    boolean running;

    boolean event_has_reset;

    public Cooldown(double max, double refresh_rate) {
        this.max = max;
        this.refresh_rate = refresh_rate;
        this.decrease_rate = this.refresh_rate * 2;
        this.event_has_reset = false;

        timer = new Timer();
        timer.schedule(new CooldownTask(), 0, 100);
        reset();
    }

    public synchronized boolean hasReset() {
        boolean prev = event_has_reset;
        event_has_reset = false;
        return prev;
    }

    void stop() {
        event_has_reset = false;
        running = false;
    }

    class CooldownTask extends TimerTask {

        public void run() {
            if (running) {
                curr_value = Math.max(0, curr_value - decrease_rate);
                if (curr_value <= 0) {
                    running = false;
                }
            } else {
                double prev_value = curr_value;
                curr_value = Math.min(max, curr_value + refresh_rate);
                if (prev_value != curr_value && curr_value == max) {
                    event_has_reset = true;
                }
            }
        }
    }

    void reset() {
        this.curr_value = this.max;
        running = false;
    }

    void start() {
        running = true;
    }

}
