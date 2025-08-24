package shader;

import shader.uniform.Uniforms;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

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

    private final Map<String, ShaderEntry> mappings = new HashMap<>();

    // caches uniform locations per shader. Cache must be cleared when a shader is reloaded.
    private final Map<String, Map<String, Integer>> locations = new HashMap<>();

    // TODO try and get rid of the exception here
    public ShaderManager(ProgramHandler programHandler) throws IOException {
        this.programHandler = programHandler;

        this.watchService = FileSystems.getDefault().newWatchService();
        Paths.get(SHADER_DIR).register(watchService, ENTRY_MODIFY);
    }

    public void register(String name, String vertexFile, String fragmentFile) {
        mappings.put(name, new ShaderEntry(vertexFile, fragmentFile));

        try {
            String vertexSource = loadSource(vertexFile);
            String fragmentSource = loadSource(fragmentFile);

            loadProgram(name, vertexSource, fragmentSource);
        } catch (IOException e) {
            System.err.println("Error while loading shader sources for shader " + name);
        }
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

        var loc = locations.computeIfAbsent(name, (_key) -> new HashMap<>());

        uniforms.uniforms.forEach((locationName, uniform) -> {
            int locationId = loc.computeIfAbsent(locationName, (location) -> glGetUniformLocation(program, location));
            uniform.bind(locationId);
        });
    }

    private void loadProgram(String name, String vertexSource, String fragmentSource) {
        locations.remove(name);
        programHandler.load(name, vertexSource, fragmentSource);
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

        mappingsToReload.forEach((shaderName, value) -> {
            var vertexFile = value.vertexFile;
            var fragmentFile = value.fragmentFile;

            try {
                var vertexSource = loadSource(vertexFile);
                var fragmentSource = loadSource(fragmentFile);

                loadProgram(shaderName, vertexSource, fragmentSource);
            } catch (IOException e) {
                System.err.println("Error while reloading shader " + shaderName);
            }
        });
    }

    private String loadSource(String file) throws IOException {
        var path = Paths.get(SHADER_DIR + "/" + file);
        return Files.readString(path);
    }

    private record ShaderEntry(
            String vertexFile,
            String fragmentFile
    ){}
}
