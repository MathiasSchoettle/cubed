import java.time.Instant;

public class Delta {

    private float deltaTime = 0;

    private Instant lastUpdate = Instant.now();

    private static final float MAX_UPDATE_LENGTH = 0.05f;

    public void update() {
        var now = Instant.now();
        long millis = now.toEpochMilli() - lastUpdate.toEpochMilli();
        deltaTime = Math.min((float) millis / 1_000f, MAX_UPDATE_LENGTH);
        lastUpdate = now;
    }

    public float delta() {
        return deltaTime;
    }
}
