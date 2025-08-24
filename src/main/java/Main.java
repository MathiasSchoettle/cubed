import camera.Camera;
import camera.CameraMovement;
import input.InputHandler;
import math.vec.Vec2;
import math.vec.Vec3;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;
import shader.ProgramHandler;
import shader.ShaderManager;
import shader.uniform.Uniforms;

import java.io.IOException;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    private long window;

    private ShaderManager manager;

    private Uniforms uniforms;

    private InputHandler inputHandler;

    private Delta delta;

    private Camera camera;

    private final Vec3 color = Vec3.of(0.3f, 0.6f, 0.1f);

    public void run() throws IOException {
        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
    }

    private void init() throws IOException {
        GLFWErrorCallback.createPrint(System.err).set();

        if ( !glfwInit() ) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(1920, 1080, "cubed", NULL, NULL);
        if ( window == NULL ) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glfwSwapInterval(1);
        glfwShowWindow(window);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED); // TODO move to input manager?

        // delta time calculator
        delta = new Delta();

        camera = new Camera(Vec3.of(0, 0, 5), Vec3.of(0, 0, -1));

        // get initial mouse pos
        double[] xPos = {0};
        double[] yPos = {0};
        glfwGetCursorPos(window, xPos, yPos);
        inputHandler = new InputHandler(window, Vec2.of((float) xPos[0], (float) yPos[0]));

        // setup camera movement
        inputHandler.registerKeyCallback(GLFW_KEY_W, () -> camera.move(CameraMovement.FORWARD, delta.delta()));
        inputHandler.registerKeyCallback(GLFW_KEY_S, () -> camera.move(CameraMovement.BACKWARD, delta.delta()));
        inputHandler.registerKeyCallback(GLFW_KEY_A, () -> camera.move(CameraMovement.LEFT, delta.delta()));
        inputHandler.registerKeyCallback(GLFW_KEY_D, () -> camera.move(CameraMovement.RIGHT, delta.delta()));
        inputHandler.registerKeyCallback(GLFW_KEY_SPACE, () -> camera.move(CameraMovement.UP, delta.delta()));
        inputHandler.registerKeyCallback(GLFW_KEY_LEFT_CONTROL, () -> camera.move(CameraMovement.DOWN, delta.delta()));
        inputHandler.registerMouseMoveCallback((x, y, dx, dy) -> {
            float sensitivity = 0.002f;
            float pitch = -dy * sensitivity;
            float yaw = -dx * sensitivity;
            camera.look(yaw, pitch);
        });

        // init shader
        manager = new ShaderManager(new ProgramHandler());
        manager.register("simple", "simple.vs", "simple.fs");

        // setup uniform binding
        uniforms = new Uniforms();
        uniforms.mat4("projection", camera.getProjectionMatrix());
        uniforms.mat4("view", camera.getViewMatrix());
        uniforms.vec3("color", color);
    }

    private void loop() {
        glClearColor(0f, 0f, 0f, 1f);

        float[] vertices = new float[]{
                0.0f,  0.5f, -0.5f,
                -0.5f, -0.5f, -0.5f,
                0.5f, -0.5f, -.5f
        };

        var buffer = MemoryUtil.memAllocFloat(vertices.length);
        buffer.put(vertices).flip();

        var vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        var vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        memFree(buffer);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            delta.update();
            manager.update();
            camera.update();
            inputHandler.update();

            // rendering of triangle
            manager.use("simple", uniforms);

            glBindVertexArray(vaoId);
            glEnableVertexAttribArray(0);
            glDrawArrays(GL_TRIANGLE_FAN, 0, 3);
            glDisableVertexAttribArray(0);
            glBindVertexArray(0);

            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }

    public static void main(String[] args) throws IOException {
        new Main().run();
    }
}
