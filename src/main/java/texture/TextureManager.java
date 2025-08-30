package texture;

import utils.filesystem.FileLoader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;

public class TextureManager {

    private final TextureHandler textureHandler;
    private final FileLoader fileLoader;
    private final String pathPrefix;

    public TextureManager(TextureHandler textureHandler, FileLoader fileLoader, String pathPrefix) {
        this.textureHandler = textureHandler;
        this.fileLoader = fileLoader;
        this.pathPrefix = pathPrefix;
    }

    public void loadTextureArray(String name, int width, int height, List<String> textureFiles) {

        if (textureFiles.isEmpty()) {
            System.err.println("Unable to load texture array: " + name + ", no textures provided");
            return;
        }

        List<BufferedImage> images = new ArrayList<>();

        for (var textureFile : textureFiles) {
            var image = fileLoader.image(pathPrefix + "/" + textureFile);

            if (image.isEmpty()) {
                System.err.println("Unable to load texture array: " + name + ", texture: " + textureFile + " could not be loaded");
                return;
            }

            images.add(image.get());
        }

        var incorrectImages = images.stream().filter(image -> image.getWidth() != width || image.getHeight() != height).toList();
        if (!incorrectImages.isEmpty()) {
            System.err.println("Not all textures had the correct size for texture array: " + name);
            incorrectImages.forEach(image -> System.err.println(image.getWidth() + " - " + image.getHeight()));
            return;
        }

        var textureData = images.stream()
                .map(image -> image.getRGB(0, 0, width, height, null, 0, width))
                .toList();

        textureHandler.loadTextureArray(name, width, height, textureData);
    }

    // FIXME this is just temporary for testing
    public void bind(String name) {
        textureHandler.getId(name).ifPresent((id) -> glBindTexture(GL_TEXTURE_2D_ARRAY, id));
    }
}
