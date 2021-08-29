package info.kgeorgiy.ja.kuleshov.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

    private final Comparator<? super E> comparator;
    private final List<E> data;

    public ArraySet() {
        this(List.of(), null);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        if (collection == null) {
            throw new NullPointerException("expected collection is not null");
        }
        this.comparator = comparator;
        List<E> sorted = new ArrayList<>(collection);
        sorted.sort(comparator);
        List<E> currentData = new ArrayList<>();
        for (E e : sorted) {
            if (currentData.size() == 0 || compare(currentData.get(currentData.size() - 1), e) != 0) {
                currentData.add(e);
            }
        }
        data = Collections.unmodifiableList(currentData);
    }

    private ArraySet(List<E> list, Comparator<? super E> comparator) {
        this.data = list;
        this.comparator = comparator;
    }

    @SuppressWarnings("unchecked")
    private int compare(E a, E b) {
        return this.comparator != null ? comparator.compare(a, b) : ((Comparable<E>) a).compareTo(b);
    }

    private int findLower(E e, boolean b) {
        int index = Collections.binarySearch(data, e, comparator);
        if (index >= 0 && b) {
            return index;
        } else if (index >= 0) {
            return index - 1;
        } else {
            return -(index + 2);
        }
    }

    private int findHigher(E e, boolean b) {
        int index = Collections.binarySearch(data, e, comparator);
        if (index >= 0 && b) {
            return index;
        } else if (index >= 0) {
            return index + 1;
        } else {
            return -(index + 1);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (E) o, comparator) >= 0;
    }

    @Override
    public E lower(E e) {
        int pos = findLower(e, false);
        return pos != -1 ? data.get(pos) : null;
    }

    @Override
    public E floor(E e) {
        int pos = findLower(e, true);
        return pos != -1 ? data.get(pos) : null;
    }

    @Override
    public E higher(E e) {
        int pos = findHigher(e, false);
        return pos != data.size() ? data.get(pos) : null;
    }

    @Override
    public E ceiling(E e) {
        int pos = findHigher(e, true);
        return pos != data.size() ? data.get(pos) : null;
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("ArraySet immutable structure");
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("ArraySet immutable structure");
    }

    @Override
    public Iterator<E> iterator() {
        return data.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReversedList<>(data), comparator == null ?
                Collections.reverseOrder() : comparator.reversed());
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E e, boolean b, E e1, boolean b1) {
        if (compare(e, e1) > 0) {
            throw new IllegalArgumentException("from(" + e + ") > to(" + e1 + ")");
        }
        int l = findHigher(e, b);
        int r = findLower(e1, b1) + 1;
        if (l > r) {
            r = l;
        }
        return new ArraySet<>(data.subList(l, r), comparator);
    }

    @Override
    public NavigableSet<E> headSet(E e, boolean b) {
        return new ArraySet<>(data.subList(0, findLower(e, b) + 1), comparator);
    }

    @Override
    public NavigableSet<E> tailSet(E e, boolean b) {
        return new ArraySet<>(data.subList(findHigher(e, b), data.size()), comparator);
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.comparator;
    }

    @Override
    public SortedSet<E> subSet(E e, E e1) {
        return subSet(e, true, e1, false);
    }

    @Override
    public SortedSet<E> headSet(E e) {
        return headSet(e, false);
    }

    @Override
    public SortedSet<E> tailSet(E e) {
        return tailSet(e, true);
    }

    @Override
    public E first() {
        if (data.size() == 0) {
            throw new NoSuchElementException("arraySet is empty");
        }
        return data.get(0);
    }

    @Override
    public E last() {
        if (data.size() == 0) {
            throw new NoSuchElementException("arraySet is empty");
        }
        return data.get(data.size() - 1);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ArraySet immutable structure");
    }

    @Override
    public int size() {
        return data.size();
    }

    private static class ReversedList<T> extends AbstractList<T> implements List<T> {
        private final List<T> list;
        private final boolean isReverse;

        public ReversedList(List<T> list) {
            if (list.getClass() == ReversedList.class) {
                this.list = ((ReversedList<T>) list).list;
                this.isReverse = !((ReversedList<T>) list).isReverse;
            } else {
                this.list = list;
                isReverse = true;
            }
        }

        @Override
        public T get(int i) {
            return list.get(isReverse ? list.size() - 1 - i : i);
        }

        @Override
        public int size() {
            return list.size();
        }
    }
}