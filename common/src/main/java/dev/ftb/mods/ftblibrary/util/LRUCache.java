package dev.ftb.mods.ftblibrary.util;

import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/// Least Recently Used Cache implementation that will remove the least recently used value when the capacity is exceeded.
/// This implementation is thread-safe.
public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, V> map;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LRUCache.this.capacity;
            }
        };
    }

    @Nullable
    public V get(K key) {
        return writeLockedAction(() -> map.get(key));
    }

    @Nullable
    public V put(K key, V value) {
        return writeLockedAction(() -> map.put(key, value));
    }

    public boolean containsKey(K key) {
        return readLockedAction(() -> map.containsKey(key));
    }

    public int size() {
        return readLockedAction(map::size);
    }

    public void clear() {
        writeLockedAction(() -> {
            map.clear();
            return null;
        });
    }

    @Override
    public String toString() {
        return readLockedAction(map::toString);
    }

    private <T> T readLockedAction(Supplier<T> action) {
        lock.readLock().lock();
        try {
            return action.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Nullable
    private <T> T writeLockedAction(Supplier<@Nullable T> action) {
        lock.writeLock().lock();
        try {
            return action.get();
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}
