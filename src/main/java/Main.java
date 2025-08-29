import camera.Camera;
import camera.CameraMovement;
import chunk.data.LegacyChunk;
import input.InputHandler;
import math.vec.Vec2;
import math.vec.Vec3;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import shader.ProgramHandler;
import shader.ShaderManager;
import shader.uniform.Uniforms;
import texture.TextureHandler;
import texture.TextureManager;
import utils.filesystem.FileLoader;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    private long window;

    private ShaderManager manager;

    private Uniforms uniforms;

    private InputHandler inputHandler;

    private Delta delta;

    private Camera camera;

    private Sun sun;

    private TextureManager textureManager;

    public void run() {
        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
    }

    private void init() {
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

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        textureManager = new TextureManager(new TextureHandler(), new FileLoader(), "/textures");
        textureManager.loadTextureArray(
                "blocks",
                16, 16,
                "cobblestone.png", "dirt.png", "sand.png"
            );

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
        inputHandler.registerResizeCallback((width, height) -> camera.setAspect(width, height));

        // init shader
        manager = new ShaderManager(new ProgramHandler(), new FileLoader());
        manager.register("simple", "simple.vs", "simple.fs");

        sun = new Sun(
                Vec3.of(104, 119, 173).scale(1f / 255f),
                Vec3.of(123, 32, 82).scale(1f / 255f)
        );

        // setup uniform binding
        uniforms = new Uniforms();
        uniforms.mat4("projection", camera.getProjectionMatrix());
        uniforms.mat4("view", camera.getViewMatrix());
        uniforms.vec3("color", sun.color);
        uniforms.integer("textures", () -> 0);
    }

    int vao, vbo, ibo;

    private void loop() {
        glClearColor(0f, 0f, 0f, 1f);

        var chunk = new LegacyChunk();
        chunk.remesh();

        setupChunk(chunk);

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            delta.update();
            manager.update();
            camera.update();
            inputHandler.update();
            sun.update(delta.delta());

            glActiveTexture(GL_TEXTURE0);
            textureManager.bind("blocks");

            // rendering of triangle
            manager.use("simple", uniforms);

            glBindVertexArray(vao);
            glDrawElements(GL_TRIANGLES, chunk.indices.size(), GL_UNSIGNED_SHORT, 0L);
            glBindVertexArray(0);

            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }

    public void setupChunk(LegacyChunk chunk) {

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, chunk.vertices.data, GL_STATIC_DRAW);

        ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, chunk.indices.data, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0L);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 9 * Float.BYTES, 3L * Float.BYTES);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 9 * Float.BYTES, 6L * Float.BYTES);
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 1, GL_FLOAT, false, 9 * Float.BYTES, 8L * Float.BYTES);

        glBindVertexArray(0);
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
