package com.bgsoftware.wildstacker.utils.data.structures;

import com.bgsoftware.wildstacker.utils.chunks.ChunkPosition;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

public class Chunk2ObjectMap<V> extends AbstractMap<ChunkPosition, V> {

    @Nullable
    private KeySet keySet;
    @Nullable
    private Values values;
    @Nullable
    private EntrySet entrySet;

    private final Map<String, Map<Long, V>> backendMap = new LinkedHashMap<>();
    private int size = 0;

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return value != null && super.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return key instanceof ChunkPosition ? get((ChunkPosition) key) : null;
    }

    @Nullable
    public V get(ChunkPosition chunkPosition) {
        return get(chunkPosition.getWorld(), chunkPosition.asPair());
    }

    @Nullable
    public V get(String worldName, long chunkPair) {
        Map<Long, V> worldBackendData = this.backendMap.get(worldName);
        if (worldBackendData == null)
            return null;

        return worldBackendData.get(chunkPair);
    }

    public V computeIfAbsent(String worldName, long chunkPair, Supplier<V> newValue) {
        Map<Long, V> worldBackendData = this.backendMap.computeIfAbsent(worldName, n -> new LinkedHashMap<>());
        return worldBackendData.computeIfAbsent(chunkPair, p -> newValue.get());
    }

    @Nullable
    @Override
    public V put(ChunkPosition chunkPosition, V value) {
        return put(chunkPosition.getWorld(), chunkPosition.asPair(), value);
    }

    @Nullable
    public V put(String worldName, long chunkPair, V value) {
        Map<Long, V> worldBackendData = this.backendMap.computeIfAbsent(worldName, n -> new LinkedHashMap<>());

        V oldValue = worldBackendData.put(chunkPair, value);

        if (oldValue == null)
            ++this.size;

        return oldValue;
    }

    @Override
    public V remove(Object key) {
        return key instanceof ChunkPosition ? remove((ChunkPosition) key) : null;
    }

    @Nullable
    public V remove(ChunkPosition chunkPosition) {
        return remove(chunkPosition.getWorld(), chunkPosition.asPair());
    }

    @Nullable
    public V remove(String worldName, long chunkPair) {
        Map<Long, V> worldBackendData = this.backendMap.get(worldName);
        if (worldBackendData == null)
            return null;

        V oldValue = onRemove(worldBackendData.remove(chunkPair));
        if (oldValue == null)
            return null;

        if (worldBackendData.isEmpty())
            Chunk2ObjectMap.this.backendMap.remove(worldName);

        return oldValue;
    }

    @Nullable
    private V onRemove(@Nullable V removedValue) {
        if (removedValue != null)
            --this.size;
        return removedValue;
    }

    @Override
    public void clear() {
        this.backendMap.clear();
        this.size = 0;
    }

    @NotNull
    @Override
    public Set<ChunkPosition> keySet() {
        return this.keySet == null ? (this.keySet = new KeySet()) : this.keySet;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return this.values == null ? (this.values = new Values()) : this.values;
    }

    @NotNull
    @Override
    public Set<Entry<ChunkPosition, V>> entrySet() {
        return this.entrySet == null ? (this.entrySet = new EntrySet()) : this.entrySet;
    }

    static int getChunkXFromPair(long chunkPair) {
        return (int) (chunkPair & 0xFFFFFFFFL);
    }

    static int getChunkZFromPair(long chunkPair) {
        return (int) (chunkPair >> 32);
    }

    private class KeySet extends AbstractSet<ChunkPosition> {

        @Override
        public int size() {
            return Chunk2ObjectMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return Chunk2ObjectMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return Chunk2ObjectMap.this.containsKey(o);
        }

        @NotNull
        @Override
        public Iterator<ChunkPosition> iterator() {
            return new KeySetItr();
        }

        @Override
        public void clear() {
            Chunk2ObjectMap.this.clear();
        }

        private class KeySetItr extends Itr<ChunkPosition> {

            @Override
            protected ChunkPosition getNext() {
                return new ChunkPosition(this.currWorld, getChunkXFromPair(this.currChunk), getChunkZFromPair(this.currChunk));
            }

        }

    }

    private class Values extends AbstractCollection<V> {

        @Override
        public int size() {
            return Chunk2ObjectMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return Chunk2ObjectMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return Chunk2ObjectMap.this.containsValue(o);
        }

        @NotNull
        @Override
        public Iterator<V> iterator() {
            return new ValuesItr();
        }

        @Override
        public void clear() {
            Chunk2ObjectMap.this.clear();
        }

        private class ValuesItr extends Itr<V> {

            @Override
            protected V getNext() {
                return this.currValue;
            }

        }

    }

    private class EntrySet extends AbstractSet<Entry<ChunkPosition, V>> {

        @Override
        public int size() {
            return Chunk2ObjectMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return Chunk2ObjectMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            ChunkPosition key = null;

            if (o instanceof Entry) {
                o = ((Entry<?, ?>) o).getKey();
            }

            if (o instanceof ChunkPosition)
                key = (ChunkPosition) o;

            return key != null && Chunk2ObjectMap.this.containsKey(o);
        }

        @NotNull
        @Override
        public Iterator<Entry<ChunkPosition, V>> iterator() {
            return new EntrySetItr();
        }

        @Override
        public void clear() {
            Chunk2ObjectMap.this.clear();
        }

        private class EntrySetItr extends Itr<Entry<ChunkPosition, V>> {

            @Override
            protected Entry<ChunkPosition, V> getNext() {
                ChunkPosition chunkPosition = new ChunkPosition(this.currWorld,
                        getChunkXFromPair(this.currChunk), getChunkZFromPair(this.currChunk));

                return new Entry<ChunkPosition, V>() {
                    @Override
                    public ChunkPosition getKey() {
                        return chunkPosition;
                    }

                    @Override
                    public V getValue() {
                        return currValue;
                    }

                    @Override
                    public V setValue(V value) {
                        V oldValue = currValue;
                        currValue = value;

                        Map<Long, V> worldBackendData = Chunk2ObjectMap.this.backendMap.get(currWorld);

                        worldBackendData.put(chunkPosition.asPair(), currValue);

                        return oldValue;
                    }
                };
            }

        }

    }

    private abstract class Itr<T> implements Iterator<T> {

        protected final Iterator<Entry<String, Map<Long, V>>> worldsIterator;
        protected Iterator<Map.Entry<Long, V>> currWorldIterator = Collections.emptyIterator();
        protected String currWorld;
        protected long currChunk;
        protected V currValue;

        protected Itr() {
            this.worldsIterator = Chunk2ObjectMap.this.backendMap.entrySet().iterator();
        }

        @Override
        public final boolean hasNext() {
            return this.currWorldIterator.hasNext() || this.worldsIterator.hasNext();
        }

        @Override
        public final T next() {
            if (!this.currWorldIterator.hasNext()) {
                if (!this.worldsIterator.hasNext()) {
                    throw new NoSuchElementException();
                }

                Entry<String, Map<Long, V>> nextWorld = this.worldsIterator.next();
                this.currWorldIterator = nextWorld.getValue().entrySet().iterator();
                this.currWorld = nextWorld.getKey();
            }

            Map.Entry<Long, V> nextChunk = this.currWorldIterator.next();
            this.currChunk = nextChunk.getKey();
            this.currValue = nextChunk.getValue();

            return getNext();
        }

        protected abstract T getNext();

        @Override
        public final void remove() {
            Map<Long, V> worldBackendData = Chunk2ObjectMap.this.backendMap.get(this.currWorld);

            this.currWorldIterator.remove();
            Chunk2ObjectMap.this.onRemove(this.currValue);

            if (worldBackendData.isEmpty())
                this.worldsIterator.remove();
        }
    }

}
