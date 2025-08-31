import block.BlockLoader;
import block.BlockProvider;
import camera.Camera;
import camera.CameraController;
import chunk.*;
import chunk.data.ChunkKey;
import input.InputHandler;
import math.vec.Vec3;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import shader.ProgramHandler;
import shader.ShaderManager;
import shader.uniform.Uniforms;
import texture.TextureHandler;
import utils.Delta;
import utils.filesystem.FileLoader;

import java.util.List;

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

    private CameraController cameraController;

    private ChunkManager chunkManager;

    private TextureHandler textureHandler;

    private final FileLoader fileLoader = new FileLoader();

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

        // delta time calculator
        delta = new Delta();

        // setup input handler
        inputHandler = new InputHandler(window);
        inputHandler.registerResizeCallback((width, height) -> glViewport(0, 0, width, height));


        // setup camera controller
        cameraController = new CameraController(new Camera(Vec3.of(0, 0, 5), Vec3.of(0, 0, -1)), inputHandler, delta);

        // init shader
        shaderManager = new ShaderManager(new ProgramHandler(), fileLoader);
        shaderManager.register("simple", "simple.vs", "simple.fs");

        // setup uniform binding
        Uniforms uniforms = new Uniforms();
        uniforms.mat4("projection", cameraController.camera().getProjectionMatrix());
        uniforms.mat4("view", cameraController.camera().getViewMatrix());
        uniforms.integer("textures", () -> 0);

        // setup block provider
        var blockLoader = new BlockLoader(fileLoader);
        var blockProvider = new BlockProvider(blockLoader.loadBlocks());

        // setup textures
        textureHandler = new TextureHandler();
        List<int[]> pixels = blockProvider.getTextures().stream().flatMap(name -> fileLoader.pixels("/textures/" + name).stream()).toList();
        textureHandler.loadTextureArray("blocks", 16, 16, pixels);

        // setup chunk manager
        var chunkStorage = new ChunkStorage();
        var chunkGenerator = new ChunkGenerator(1, blockProvider);
        var chunkMesher = new ChunkMesher(blockProvider);
        chunkManager = new ChunkManager(chunkStorage, chunkGenerator, chunkMesher, shaderManager, uniforms);

        // create these chunks
        for (int x = 0; x < 2; ++x) for (int y = 0; y < 2; ++y) for (int z = 0; z < 2; ++z) {
            chunkManager.load(new ChunkKey(x, y, z));
        }
    }

    private void loop() {
        glClearColor(0f, 0f, 0f, 1f);

        glActiveTexture(GL_TEXTURE0);
        textureHandler.bind("blocks");

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            delta.update();
            shaderManager.update();
            cameraController.update();
            inputHandler.update();
            chunkManager.update();

            chunkManager.draw();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
