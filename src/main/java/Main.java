import camera.Camera;
import camera.CameraMovement;
import chunk.*;
import chunk.data.ChunkKey;
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
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    private long window;

    private ShaderManager shaderManager;

    private InputHandler inputHandler;

    private Delta delta;

    private Camera camera;

    private Sun sun;

    private TextureManager textureManager;

    private ChunkManager chunkManager;

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
        shaderManager = new ShaderManager(new ProgramHandler(), new FileLoader());
        shaderManager.register("simple", "simple.vs", "simple.fs");

        sun = new Sun(
                Vec3.of(104, 119, 173).scale(1f / 255f),
                Vec3.of(123, 32, 82).scale(1f / 255f)
        );

        // setup uniform binding
        Uniforms uniforms = new Uniforms();
        uniforms.mat4("projection", camera.getProjectionMatrix());
        uniforms.mat4("view", camera.getViewMatrix());
        uniforms.vec3("color", sun.color);
        uniforms.integer("textures", () -> 0);

        // setup block provider
        var blockProvider = new BlockProvider();
        blockProvider.register("base:air");
        blockProvider.register("base:dirt");

        // setup chunk manager
        var chunkStorage = new ChunkStorage();
        var chunkGenerator = new ChunkGenerator(1, blockProvider);
        var chunkMesher = new ChunkMesher(blockProvider);
        chunkManager = new ChunkManager(chunkStorage, chunkGenerator, chunkMesher, shaderManager, uniforms);
    }

    private void loop() {
        glClearColor(0f, 0f, 0f, 1f);

        for (int x = 0; x < 4; ++x) for (int y = 0; y < 2; ++y) for (int z = 0; z < 4; ++z) {
            chunkManager.load(new ChunkKey(x, y, z));
        }

        chunkManager.mesh();

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            delta.update();
            shaderManager.update();
            camera.update();
            inputHandler.update();
            sun.update(delta.delta());

            glActiveTexture(GL_TEXTURE0);
            textureManager.bind("blocks");

            chunkManager.draw();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
