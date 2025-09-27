package shader;

import shader.uniform.Uniforms;
import utils.filesystem.FileLoader;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUseProgram;

// TODO split this up into general shader logic required for using shaders
//  and the auto reload of shaders which only really makes sense in dev mode

/**
 * Provides functionality to register shaders and use them.
 */
public class ShaderManager {

    private static final String SHADER_DIR = "src/main/resources/shaders/";

    private final WatchService watchService;

    private final ProgramHandler programHandler;

    private final FileLoader fileLoader;

    private final Map<String, ShaderEntry> mappings = new HashMap<>();

    // caches uniform locations per shader. Cache must be cleared when a shader is reloaded.
    private final Map<String, Map<String, Integer>> locations = new HashMap<>();

    public ShaderManager(ProgramHandler programHandler, FileLoader fileLoader) {
        this.programHandler = programHandler;
        this.fileLoader = fileLoader;

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            Paths.get(SHADER_DIR).register(watchService, ENTRY_MODIFY);
        } catch (IOException e) {
            // we only use hot reloading in dev, so this is fine
            throw new RuntimeException(e);
        }
    }

    public void register(String name, String vertexFile, String fragmentFile) {
        mappings.put(name, new ShaderEntry(vertexFile, fragmentFile));
        loadShaders(name, vertexFile, fragmentFile);
    }

    /**
     * @param name the name of the program to bind
     * @param uniforms the uniform values which will be bound to the program
     */
    public void use(String name, Uniforms uniforms) {
        var optional = programHandler.getId(name);

        if (optional.isEmpty()) {
            System.err.println("No program " + name + " present");
            return;
        }

        int program = optional.get();
        glUseProgram(program);

        var loc = locations.computeIfAbsent(name, (_) -> new HashMap<>());

        // While profiling, a lot of lambdas where created here when using computeIfAbsent for loc
        // don't really know why
        for (var entry : uniforms.uniforms.entrySet()) {
            if (!loc.containsKey(entry.getKey())) {
                loc.put(entry.getKey(), glGetUniformLocation(program, entry.getKey()));
            }

            int locationId = loc.get(entry.getKey());

            entry.getValue().bind(locationId);
        }
    }

    public void update() {

        var watchKey = watchService.poll();

        if (watchKey == null) {
            return;
        }

        Map<String, ShaderEntry> mappingsToReload = new HashMap<>();

        watchKey.pollEvents().forEach(event -> {
            var fileName = ((Path) event.context()).toString();

            for (var entry : mappings.entrySet()) {
                var vertexFile = entry.getValue().vertexFile;
                var fragmentFile = entry.getValue().fragmentFile;

                if (vertexFile.equals(fileName) || fragmentFile.equals(fileName)) {
                    mappingsToReload.put(entry.getKey(), new ShaderEntry(vertexFile, fragmentFile));
                }
            }
        });

        watchKey.reset();

        mappingsToReload.forEach((shaderName, value) -> loadShaders(
                shaderName,
                value.vertexFile,
                value.fragmentFile
        ));
    }

    private void loadProgram(String name, String vertexSource, String fragmentSource) {
        locations.remove(name);
        programHandler.load(name, vertexSource, fragmentSource);
    }

    private void loadShaders(String name, String vertexFile, String fragmentFile) {
        var vertexSource = loadSource(vertexFile);
        var fragmentSource = loadSource(fragmentFile);

        if (vertexSource.isPresent() && fragmentSource.isPresent()) {
            loadProgram(name, vertexSource.get(), fragmentSource.get());
        } else {
            System.err.println("Could not load shader: " + name);
        }
    }

    private Optional<String> loadSource(String file) {
        return fileLoader.string(SHADER_DIR + "/" + file);
    }

    private record ShaderEntry(
            String vertexFile,
            String fragmentFile
    ){}
}
