package shader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.lwjgl.opengl.GL20.*;

/**
 * Handles GL programs. Recompiles on reload.
 */
public class ProgramHandler {

    private final Map<String, Integer> programs = new HashMap<>();

    public Optional<Integer> load(String name, String vertexSource, String fragmentSource) {

        var vertexShaderId = createShader(vertexSource, GL_VERTEX_SHADER);
        var fragmentShaderId = createShader(fragmentSource, GL_FRAGMENT_SHADER);

        if (vertexShaderId == 0 || fragmentShaderId == 0) {
            System.err.println("SHADER COMPILING WENT WRONG");
            return Optional.empty();
        }

        var programId = glCreateProgram();

        if (programId == 0) {
            return Optional.empty();
        }

        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);

        glLinkProgram(programId);

        if ( glGetProgrami(programId, GL_LINK_STATUS) == 0 ) {
            glDeleteProgram(programId);
            System.out.println("LINKING WENT WRONG");
            return Optional.empty();
        }

        glDetachShader(programId, vertexShaderId);
        glDetachShader(programId, fragmentShaderId);

        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);

        glValidateProgram(programId);
        if ( glGetProgrami(programId, GL_VALIDATE_STATUS) == 0 ) {
            System.out.println("VALIDATING WENT WRONG");
            glDeleteProgram(programId);
            return Optional.empty();
        }

        var existing = programs.put(name, programId);
        if (existing != null) {
            glDeleteProgram(existing);
        }

        System.out.println("Created shader " + name + " with id " + programId);

        return Optional.of(programId);
    }

    public void clear(String name) {
        var programId = programs.remove(name);
        if (programId != null) {
            glDeleteProgram(programId);
        }
    }

    public Optional<Integer> getId(String name) {
        return Optional.ofNullable(programs.get(name));
    }

    private int createShader(String shaderSource, int shaderType) {
        int shaderId = glCreateShader(shaderType);

        if (shaderId == 0) {
            return 0;
        }

        glShaderSource(shaderId, shaderSource);
        glCompileShader(shaderId);

        if ( glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            return 0;
        }

        return shaderId;
    }
}
