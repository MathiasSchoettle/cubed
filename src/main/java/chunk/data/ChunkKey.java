package chunk.data;

// TODO: think about this. When accessing a hashmap where this is a key, we need to construct this object
//  maybe having a hashed primitive key (i.e. long value) would be better?
public record ChunkKey(int x, int y, int z) {}
