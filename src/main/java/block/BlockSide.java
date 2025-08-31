package block;

public enum BlockSide {
    POSITIVE_X((short) 0),
    NEGATIVE_X((short) 1),
    POSITIVE_Y((short) 2),
    NEGATIVE_Y((short) 3),
    POSITIVE_Z((short) 4),
    NEGATIVE_Z((short) 5);

    public final short index;

    BlockSide(short index) {
        this.index = index;
    }
}
