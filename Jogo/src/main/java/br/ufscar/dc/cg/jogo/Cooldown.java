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

    public Cooldown(double max, double refresh_rate) {
        this.max = max;
        this.refresh_rate = refresh_rate;
        this.decrease_rate = this.refresh_rate * 2;
        timer = new Timer();
        timer.schedule(new CooldownTask(), 0, 500);
        reset();
    }

    void stop() {
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
                curr_value = Math.min(max, curr_value + refresh_rate);
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
