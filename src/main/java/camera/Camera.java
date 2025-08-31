package camera;

import math.mat.Mat4;
import math.vec.Vec3;

public class Camera {
    public final Vec3 position;

    public final Vec3 direction;
    public final Vec3 up = Vec3.of(0);
    public final Vec3 right = Vec3.of(0);

    public final Vec3 globalUp = Vec3.of(0, 1, 0);

    private final Mat4 projectionMatrix = Mat4.of(0);
    private final Mat4 viewMatrix = Mat4.of(0);

    private final float FOV = (float) Math.toRadians(90);
    private final float NEAR = 0.01f;
    private final float FAR = 1000;
    private float aspectRatio = 16f / 9f;

    public Camera(Vec3 position, Vec3 direction) {
        this.position = position;
        this.direction = direction;

        updateViewMatrix();
        updatePerspectiveMatrix();
    }

    public void move(CameraMovement type, float speed) {

        var movement = switch (type) {
            case FORWARD -> direction.getScaled(speed);
            case BACKWARD -> direction.getScaled(speed).invert();
            case RIGHT -> right.getScaled(speed);
            case LEFT -> right.getScaled(speed).invert();
            case UP -> up.getScaled(speed);
            case DOWN -> up.getScaled(speed).invert();
        };

        position.add(movement);
    }

    public void setAspect(float width, float height) {
        aspectRatio = width / height;
        updatePerspectiveMatrix();
    }

    private void updatePerspectiveMatrix() {
        projectionMatrix.perspective(FOV, aspectRatio, NEAR, FAR);
    }

    public void look(float yaw, float pitch) {
        direction.rotate(up, yaw)
            .rotate(right, pitch)
            .normalize();
    }

    public void update() {
        updateViewMatrix();
    }

    private void updateViewMatrix() {
        direction.normalize();
        right.cross(direction, globalUp).normalize();
        up.cross(right, direction);

        viewMatrix.values[0] = right.x;
        viewMatrix.values[1] = up.x;
        viewMatrix.values[2] = -direction.x;
        viewMatrix.values[3] = 0;

        viewMatrix.values[4] = right.y;
        viewMatrix.values[5] = up.y;
        viewMatrix.values[6] = -direction.y;
        viewMatrix.values[7] = 0;

        viewMatrix.values[8] = right.z;
        viewMatrix.values[9] = up.z;
        viewMatrix.values[10] = -direction.z;
        viewMatrix.values[11] = 0;

        viewMatrix.values[12] = -right.dot(position);
        viewMatrix.values[13] = -up.dot(position);
        viewMatrix.values[14] = direction.dot(position);
        viewMatrix.values[15] = 1;
    }

    public Mat4 getViewMatrix() {
        return viewMatrix;
    }

    public Mat4 getProjectionMatrix() {
        return projectionMatrix;
    }
}
