import math.vec.Vec3;

public class Sun {

    private static final float DAY_DURATION = 30;

    private float time = 0;

    private final Vec3 nightColor;
    private final Vec3 dayColor;

    public final Vec3 color = Vec3.of(0);

    public Sun(Vec3 nightColor, Vec3 dayColor) {
        this.nightColor = nightColor;
        this.dayColor = dayColor;
        updateColor();
    }

    private void updateColor() {
        float t = time / DAY_DURATION;
        float factor = 0.5f * (float)(Math.cos(2 * Math.PI * t) + 1.0);
        color.x = dayColor.x * factor + nightColor.x * (1 - factor);
        color.y = dayColor.y * factor + nightColor.y * (1 - factor);
        color.z = dayColor.z * factor + nightColor.z * (1 - factor);
    }

    public void update(float delta) {
        time = (delta + time) % DAY_DURATION;
        updateColor();
    }
}
