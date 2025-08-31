package block;

public enum BlockSide {
    NEGATIVE_X((short) 0),
    POSITIVE_X((short) 1),
    NEGATIVE_Y((short) 2),
    POSITIVE_Y((short) 3),
    NEGATIVE_Z((short) 4),
    POSITIVE_Z((short) 5);

    public final short index;

    BlockSide(short index) {
        this.index = index;
    }
}
