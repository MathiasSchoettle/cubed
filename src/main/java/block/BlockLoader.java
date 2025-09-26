package block;

import block.meta.BlockMeta;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.filesystem.FileLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BlockLoader {

    private final FileLoader fileLoader;

    private final ObjectMapper mapper = new ObjectMapper();

    // TODO make this work when deployed also
    private static final String META_DIR = "src/main/resources/blocks/";

    public BlockLoader(FileLoader fileLoader) {
        this.fileLoader = fileLoader;
    }

    public List<BlockMeta> loadBlocks() {
        var metaFileNames = fileLoader.listFiles("blocks");
        List<BlockMeta> list = new ArrayList<>();

        for (var name : metaFileNames) {
            var content = fileLoader.string(META_DIR + name);

            if (content.isEmpty()) {
                continue;
            }

            try {
                var meta = mapper.readValue(content.get(), BlockMeta.class);
                list.add(meta);
            } catch (IOException ignore) {
                System.err.println("Failed reading meta of block: " + name + " - " + ignore.getLocalizedMessage());
            }
        }

        return list;
    }
}
