package input;

import math.vec.Vec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

public class InputHandler {

    private final long window;

    private final Map<Integer, List<KeyCallback>> keyCallbacks = new HashMap<>();

    private final List<MouseMoveCallback> mouseMoveCallbacks = new ArrayList<>();

    private final List<ResizeCallback> resizeCallbacks = new ArrayList<>();

    private final Vec2 mousePosition;

    public InputHandler(long window, Vec2 mousePosition) {
        this.window = window;
        this.mousePosition = mousePosition;
        glfwSetCursorPosCallback(window, (_window, x, y) -> handleMouseMove((float) x, (float) y));
        glfwSetWindowSizeCallback(window, (_window, width, height) -> {
            resizeCallbacks.forEach(callback -> callback.apply(width, height));
            glViewport(0, 0, width, height);
        });
    }

    private void handleMouseMove(float x, float y) {
        float dx = x - mousePosition.x;
        float dy = y - mousePosition.y;

        mouseMoveCallbacks.forEach(callback -> callback.apply(x, y, dx, dy));

        mousePosition.x = x;
        mousePosition.y = y;
    }

    public void update() {
        keyCallbacks.forEach((key, value) -> {
            if (glfwGetKey(window, key) == GLFW_PRESS) {
                value.forEach(KeyCallback::apply);
            }
        });
    }

    public void registerKeyCallback(int key, KeyCallback callback) {
        keyCallbacks.computeIfAbsent(key, (k) -> new ArrayList<>()).add(callback);
    }

    public void registerMouseMoveCallback(MouseMoveCallback callback) {
        mouseMoveCallbacks.add(callback);
    }

    public void registerResizeCallback(ResizeCallback callback) {
        resizeCallbacks.add(callback);
    }
}
