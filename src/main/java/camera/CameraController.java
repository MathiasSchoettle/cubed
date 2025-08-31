package camera;

import input.InputHandler;
import utils.Delta;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

public class CameraController {

    private final Camera camera;

    private final Delta delta;

    private boolean isFast = false;

    public CameraController(Camera camera, InputHandler inputHandler, Delta delta) {
        this.camera = camera;
        this.delta = delta;

        // setup camera movement
        inputHandler.registerKeyCallback(GLFW_KEY_W, () -> camera.move(CameraMovement.FORWARD, speed()));
        inputHandler.registerKeyCallback(GLFW_KEY_S, () -> camera.move(CameraMovement.BACKWARD, speed()));
        inputHandler.registerKeyCallback(GLFW_KEY_A, () -> camera.move(CameraMovement.LEFT, speed()));
        inputHandler.registerKeyCallback(GLFW_KEY_D, () -> camera.move(CameraMovement.RIGHT, speed()));

        inputHandler.registerKeyCallback(GLFW_KEY_SPACE, () -> camera.move(CameraMovement.UP, speed()));
        inputHandler.registerKeyCallback(GLFW_KEY_LEFT_CONTROL, () -> camera.move(CameraMovement.DOWN, speed()));

        inputHandler.registerKeypressCallback(GLFW_KEY_LEFT_SHIFT, action -> isFast = action != GLFW_RELEASE);

        inputHandler.registerMouseMoveCallback((x, y, dx, dy) -> {
            float sensitivity = 0.002f;
            float pitch = -dy * sensitivity;
            float yaw = -dx * sensitivity;
            camera.look(yaw, pitch);
        });

        inputHandler.registerResizeCallback(camera::setAspect);
    }

    private float speed() {
        return (isFast ? 40 : 10) * delta.delta();
    }

    public void update() {
        camera.update();
    }

    public Camera camera() {
        return camera;
    }
}
