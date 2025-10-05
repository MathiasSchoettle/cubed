package input;

import math.vec.Vec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {

    private final long window;

    private final Map<Integer, List<KeyCallback>> keyCallbacks = new HashMap<>();

    private final Map<Integer, List<KeypressCallback>> keypressCallbacks = new HashMap<>();

    private final List<MouseMoveCallback> mouseMoveCallbacks = new ArrayList<>();

    private final List<ResizeCallback> resizeCallbacks = new ArrayList<>();

    private final Vec2 mousePosition;

    private boolean mouseFreed = false;

    public InputHandler(long window) {
        this.window = window;

        // get initial mouse pos
        double[] xPos = {0};
        double[] yPos = {0};
        glfwGetCursorPos(window, xPos, yPos);
        this.mousePosition = Vec2.of((float) xPos[0], (float) yPos[0]);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwSetCursorPosCallback(window, (_window, x, y) -> handleMouseMove((float) x, (float) y));

        glfwSetWindowSizeCallback(window, (_window, width, height) -> resizeCallbacks.forEach(callback -> callback.apply(width, height)));

        registerKeypressCallback(GLFW_KEY_ESCAPE, (action) -> {
                if (action != GLFW_PRESS) {
                    return;
                }
            
                mouseFreed = !mouseFreed;
                glfwSetInputMode(window, GLFW_CURSOR, mouseFreed ? GLFW_CURSOR_NORMAL : GLFW_CURSOR_DISABLED);
        });

        glfwSetKeyCallback(window, (_window, key, scancode, action, mods) -> {
            keypressCallbacks.forEach((k, callbacks) -> {
                if (key == k) {
                    callbacks.forEach(callback -> callback.apply(action));
                }
            });
        });
    }

    private void handleMouseMove(float x, float y) {

        float dx = x - mousePosition.x;
        float dy = y - mousePosition.y;

        mousePosition.x = x;
        mousePosition.y = y;

        if (mouseFreed) {
            return;
        }

        mouseMoveCallbacks.forEach(callback -> callback.apply(x, y, dx, dy));
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

    public void registerKeypressCallback(int key, KeypressCallback callback) {
        keypressCallbacks.computeIfAbsent(key, (k) -> new ArrayList<>()).add(callback);
    }

    public void registerMouseMoveCallback(MouseMoveCallback callback) {
        mouseMoveCallbacks.add(callback);
    }

    public void registerResizeCallback(ResizeCallback callback) {
        resizeCallbacks.add(callback);
    }
}
