import block.BlockLoader;
import block.BlockProvider;
import camera.Camera;
import camera.CameraController;
import chunk.*;
import chunk.generate.ChunkProvider;
import chunk.generate.stage.impl.GrassStage;
import chunk.generate.stage.impl.TerrainStage;
import environment.Cubemap;
import input.InputHandler;
import math.vec.Vec3;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import shader.ProgramHandler;
import shader.ShaderManager;
import shader.uniform.Uniforms;
import texture.TextureHandler;
import threading.TaskHandler;
import ui.UserInterfaceRenderer;
import utils.time.Delta;
import utils.filesystem.FileLoader;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

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

    private Cubemap cubemap;

    private TextureHandler textureHandler;

    private final FileLoader fileLoader = new FileLoader();

    private TaskHandler taskHandler;

    private UserInterfaceRenderer uiRenderer;

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

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glDepthFunc(GL_LEQUAL);

        // setup task handler
        taskHandler = new TaskHandler(Executors.newFixedThreadPool(4), Executors.newFixedThreadPool(12));

        // delta time calculator
        delta = new Delta();

        // setup input handler
        inputHandler = new InputHandler(window);

        uiRenderer = new UserInterfaceRenderer(window);

        inputHandler.registerResizeCallback((width, height) -> glViewport(0, 0, width, height));

        // setup camera controller
        cameraController = new CameraController(new Camera(Vec3.of(0, 64, 0), Vec3.of(0, 0, -1)), inputHandler, delta);

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

        // setup cubemap
        shaderManager.register("cubemap", "cubemap.vs", "cubemap.fs");
        var cubemapData = Stream.of("front.jpg", "back.jpg", "top.jpg", "bottom.jpg", "left.jpg", "right.jpg")
                .map(name -> "/textures/cubemap/sky/" + name)
                .flatMap(filename -> fileLoader.pixels(filename).stream()).toList();
        textureHandler.loadCubemap("cubemap", 2048, cubemapData);
        cubemap = new Cubemap();

        // setup chunk manager
        var chunkStorage = new ChunkStorage();

        var air = blockProvider.getBlockInfo("base:air");
        var stone = blockProvider.getBlockInfo("base:stone");
        var dirt = blockProvider.getBlockInfo("base:dirt");
        var grass = blockProvider.getBlockInfo("base:grass");

        var provider = ChunkProvider.builder(taskHandler)
                .addStage("terrain", new TerrainStage(1, air, stone))
                .addStage("grass", new GrassStage(air, dirt, grass))
                .build();

        var chunkMesher = new ChunkMesher(blockProvider, taskHandler);
        chunkManager = new ChunkManager(chunkStorage, provider, chunkMesher, shaderManager, uniforms);
    }

    private void loop() {
        glClearColor(0f, 0f, 0f, 1f);

        // this is probably going to break
        glActiveTexture(GL_TEXTURE0);
        textureHandler.bind("blocks");
        textureHandler.bind("cubemap");

        // somewhere else
        Uniforms cubemapUniforms = new Uniforms();
        cubemapUniforms.mat4("projection", cameraController.camera().getProjectionMatrix());
        cubemapUniforms.mat4("view", cameraController.camera().getCubemapViewMatrix());
        cubemapUniforms.integer("skybox", () -> 0);

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            delta.update();
            shaderManager.update();
            cameraController.update();
            inputHandler.update();

            chunkManager.update(cameraController.camera().position);
            chunkManager.draw();

            shaderManager.use("cubemap", cubemapUniforms);
            cubemap.render();

            uiRenderer.render(delta.delta());

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        uiRenderer.shutdown();
        taskHandler.shutdown();
    }

    public static void main(String[] args) throws InterruptedException {
        new Main().run();
    }
}
