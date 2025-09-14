package texture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL20.glGenTextures;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.opengl.GL42.glTexStorage3D;

public class TextureHandler {

    private final Map<String, Integer> textureIds = new HashMap<>();

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

        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);

        glGenerateMipmap(GL_TEXTURE_2D_ARRAY);

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

        System.out.println("Created texture array " + name + " with id " + textureArray + " containing " + textureData.size() + " textures");

        textureIds.put(name, textureArray);
    }

    public void unload(String name) {
        var texture = textureIds.remove(name);
        if (texture != null) {
            glDeleteTextures(texture);
        }
    }

    public Optional<Integer> getId(String name) {
        return Optional.of(textureIds.get(name));
    }

    public void bind(String name) {
        getId(name).ifPresent((id) -> glBindTexture(GL_TEXTURE_2D_ARRAY, id));
    }
}
