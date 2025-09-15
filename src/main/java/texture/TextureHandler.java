package texture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL20.glGenTextures;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL42.glTexStorage3D;

public class TextureHandler {

    private final Map<String, TextureEntry> textureIds = new HashMap<>();

    public void loadTextureArray(String name, int width, int height, List<int[]> textureData) {

        unload(name);

        int textureArray = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureArray);

        glTexStorage3D(GL_TEXTURE_2D_ARRAY, 1, GL_RGBA8, width, height, textureData.size());

        for (int layer = 0; layer < textureData.size(); ++layer) {
            var image = textureData.get(layer);
            glTexSubImage3D(
                    GL_TEXTURE_2D_ARRAY,
                    0,
                    0,
                    0,
                    layer,
                    width,
                    height,
                    1,
                    GL_BGRA,
                    GL_UNSIGNED_BYTE,
                    image
            );
        }

        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

        System.out.println("Created texture array " + name + " with id " + textureArray + " containing " + textureData.size() + " textures");

        textureIds.put(name, new TextureEntry(GL_TEXTURE_2D_ARRAY, textureArray));
    }

    // 6 images, +X, -X, +Y, -Y, +Z, -Z
    public void loadCubemap(String name, int size, List<int[]> textureData) {

        unload(name);

        int cubemap = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemap);

        // cubemap always contains six images
        for (int i = 0; i < 6; ++i) {
            glTexImage2D(
                    GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, // this will iterate through the correct targets
                    0,
                    GL_RGB,
                    size, size,
                    0,
                    GL_BGRA,
                    GL_UNSIGNED_BYTE,
                    textureData.get(i));
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        System.out.println("Created cubemap " + name + " with id " + cubemap);

        textureIds.put(name, new TextureEntry(GL_TEXTURE_CUBE_MAP, cubemap));
    }

    public void unload(String name) {
        var texture = textureIds.remove(name);
        if (texture != null) {
            glDeleteTextures(texture.id());
        }
    }

    public void bind(String name) {
        getId(name).ifPresent((entry) -> glBindTexture(entry.target(), entry.id()));
    }

    private Optional<TextureEntry> getId(String name) {
        return Optional.of(textureIds.get(name));
    }

    private record TextureEntry(int target, int id){}
}
