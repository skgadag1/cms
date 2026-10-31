package com.tbscg.doosan.magnolia.cloudinary.contentview;

import info.magnolia.dam.api.ItemKey;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * This is a {@link java.util.List} wrapper for CloudinaryAssetContainer, which prevents
 * unnecessary API requests when displaying thumbnail view of asset chooser.
 * The only reason this class exist, is {@link info.magnolia.ui.vaadin.layout.LazyLayout#updateSelectedItems()}
 * implementation which fetches the list of all assets just to get the selected one.
 */
public class LazyItemIdList implements List<ItemKey> {

    private final CloudinaryAssetContainer container;
    private final int startIndex;
    private final int indexLimit;

    public LazyItemIdList(int startIndex, int numberOfIds, CloudinaryAssetContainer container) {
        this.container = container;
        this.startIndex = startIndex;
        this.indexLimit = getIndexLimit(startIndex, numberOfIds);
        if (this.startIndex < 0 || this.startIndex > this.indexLimit) {
            throw new IllegalArgumentException("invalid range: " + startIndex + " ... " + indexLimit);
        }
    }

    private int getIndexLimit(int startIndex, int numberOfIds) {
        long sum = (long) startIndex + (long) numberOfIds;
        int result = sum > (long) Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) sum;
        return Math.min(this.container.size(), result);
    }

    private int getInternalIndex(int externalIndex) {
        return this.startIndex + externalIndex;
    }

    private void rangeCheck(int internalIndex) {
        if (internalIndex < this.startIndex || this.indexLimit <= internalIndex) {
            throw new IndexOutOfBoundsException("internalIndex out of bounds: " + internalIndex);
        }
    }

    public ItemKey get(int index) {
        int internalIndex = getInternalIndex(index);
        return getInternal(internalIndex);
    }

    private ItemKey getInternal(int index) {
        rangeCheck(index);
        return this.container.getIdByIndex(index);
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < size(); i++) {
            if (Objects.equals(o, get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = size() - 1; i >= 0; i--) {
            if (Objects.equals(o, get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public List<ItemKey> subList(int fromIndex, int toIndex) {
        return new LazyItemIdList(this.startIndex + fromIndex, toIndex - fromIndex, this.container);
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        for (int i = 0; i < size(); i++) {
            array[i] = get(i);
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size()) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
        }
        Object[] array = a;
        for (int i = 0; i < size(); i++) {
            array[i] = get(i);
        }
        return a;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    private InternalIterator internalIterator(int index) {
        int internalIndex = getInternalIndex(index);
        rangeCheck(internalIndex);
        return new InternalIterator(internalIndex);
    }

    @Override
    public Iterator<ItemKey> iterator() {
        return internalIterator(0);
    }

    @Override
    public ListIterator<ItemKey> listIterator() {
        return internalIterator(0);
    }

    @Override
    public ListIterator<ItemKey> listIterator(int index) {
        return internalIterator(index);
    }

    public int size() {
        return Math.max(0, this.indexLimit - this.startIndex);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Stream<ItemKey> parallelStream() {
        return stream();
    }

    @Override
    public boolean add(ItemKey t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemKey set(int index, ItemKey element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, ItemKey element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemKey remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends ItemKey> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends ItemKey> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(UnaryOperator<ItemKey> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort(Comparator<? super ItemKey> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    private class InternalIterator implements ListIterator<ItemKey> {

        private int currentIndex;

        public InternalIterator(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @Override
        public boolean hasNext() {
            return nextIndex() < LazyItemIdList.this.indexLimit;
        }

        @Override
        public ItemKey next() {
            int nextIndex = nextIndex();
            LazyItemIdList.this.rangeCheck(nextIndex);
            this.currentIndex += 1;
            return LazyItemIdList.this.getInternal(nextIndex);
        }

        @Override
        public boolean hasPrevious() {
            return previousIndex() >= LazyItemIdList.this.startIndex;
        }

        @Override
        public ItemKey previous() {
            int previousIndex = previousIndex();
            LazyItemIdList.this.rangeCheck(previousIndex);
            this.currentIndex -= 1;
            return LazyItemIdList.this.getInternal(previousIndex);
        }

        @Override
        public int nextIndex() {
            return this.currentIndex;
        }

        @Override
        public int previousIndex() {
            return this.currentIndex - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(ItemKey t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(ItemKey t) {
            throw new UnsupportedOperationException();
        }
    }
}
