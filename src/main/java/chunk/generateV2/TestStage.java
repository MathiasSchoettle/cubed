package chunk.generateV2;

import block.meta.BlockInfo;

public class TestStage implements GenerationStage {

    private final BlockInfo log;

    public TestStage(BlockInfo log) {
        this.log = log;
    }

    @Override
    public void generate(Domain domain) {

        var setTree = Math.random() > 0.5;

        for (int x = domain.x(); x < domain.spanX(); ++x) for (int y = domain.y(); y < domain.spanY(); ++y) for (int z = domain.z(); z < domain.spanZ(); ++z) {
            if (setTree) {
                domain.set(x, y, z, log);
            }
        }
    }
}
