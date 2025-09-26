package utils.filesystem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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

    public Optional<int[]> pixels(String filename) {
        return image(filename).map(image -> image.getRGB(
                0, 0,
                image.getWidth(), image.getHeight(),
                null,
                0,
                image.getWidth())
        );
    }

    public List<String> listFiles(String filename) {
        return wrapInTry(() -> getClass().getClassLoader().getResource(filename))
                .flatMap(url -> wrapInTry(url::toURI))
                .map(Paths::get)
                .flatMap(path -> wrapInTry(() -> Files.list(path)))
                .map(paths -> paths.map(Path::toFile))
                .map(files -> files.map(File::getName))
                .map(Stream::toList).orElse(List.of());
    }

    private <T> Optional<T> wrapInTry(ExceptionalSupplier<T> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (Exception _ignore) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface ExceptionalSupplier<T> {
        T get() throws Exception;
    }
}
