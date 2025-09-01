package utils.time;

import java.time.Duration;
import java.time.Instant;

public class Timer {

    public Instant start, stop;

    public Timer() {
        start = Instant.now();
        stop = start;
    }

    public Timer stop() {
        stop = Instant.now();
        return this;
    }

    public long millis() {
        return Duration.between(start, stop).toMillis();
    }
}
