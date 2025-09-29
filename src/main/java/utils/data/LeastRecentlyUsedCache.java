package utils.data;

import java.util.LinkedHashMap;
import java.util.Map;

public class LeastRecentlyUsedCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public LeastRecentlyUsedCache(int maxSize) {
        // accessOrder = true → maintains order by access, not insertion
        super(maxSize, 0.75f, true);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
}