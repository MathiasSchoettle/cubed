package utils.filesystem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FileLoader {

    public FileLoader() {}

    public Optional<List<String>> lines(String filename) {
        var path = Paths.get(filename);
        return wrapInTry(() -> Files.readAllLines(path));
    }

    public Optional<String> string(String filename) {
        var path = Paths.get(filename);
        return wrapInTry(() -> Files.readString(path));
    }

    public Optional<BufferedImage> image(String filename) {
        return wrapInTry(() -> ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream(filename))));
    }

    private <T> Optional<T> wrapInTry(FileSupplier<T> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (Exception _ignore) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface FileSupplier<T> {
        T get() throws Exception;
    }
}
