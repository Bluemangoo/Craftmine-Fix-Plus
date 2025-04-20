package net.bluemangoo.craftmineFixPlus.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.*;

public class RwLockLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private final Map<K, V> map = new LinkedHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public V get(Object key) {
        lock.readLock().lock();
        try {
            return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public V put(K key, V value) {
        lock.writeLock().lock();
        try {
            map.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
        return value;
    }

    public V remove(Object key) {
        lock.writeLock().lock();
        try {
            return map.remove(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        lock.writeLock().lock();
        try {
            map.putAll(m);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            map.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public @NotNull Set<K> keySet() {
        lock.readLock().lock();
        try {
            return new HashSet<>(map.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public @NotNull Collection<V> values() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(map.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        lock.readLock().lock();
        try {
            return new HashSet<>(map.entrySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return map.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return map.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean containsKey(Object key) {
        lock.readLock().lock();
        try {
            return map.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        lock.readLock().lock();
        try {
            return map.containsValue(value);
        } finally {
            lock.readLock().unlock();
        }
    }


}
