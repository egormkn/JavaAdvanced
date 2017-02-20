package ru.ifmo.ctddev.makarenko.arrayset;

import com.sun.istack.internal.*;

import java.util.*;

/**
 * Unmodifiable Sorted Set
 *
 * @param <T> the type of elements maintained by this set
 * @see ArrayList
 * @see SortedSet
 * @see NavigableSet
 * @see TreeSet
 */

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {

    private final List<T> items;
    private final Comparator<? super T> comparator;
    private NavigableSet<T> descending = null;

    public static void main(String[] args) {
        Random random = new Random();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add("Test" + random.nextInt(10));
        }

        ArraySet<String> set = new ArraySet<>(list);
        System.out.println(set);
    }

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(@NotNull Collection<T> data) {
        this(data, null);
    }

    public ArraySet(@NotNull Collection<T> data, @Nullable Comparator<? super T> cmp) {
        comparator = cmp;
        TreeSet<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(data);
        items = Collections.unmodifiableList(new ArrayList<>(treeSet));
    }

    @Override
    public T first() {
        if (isEmpty()) {
            throw new NoSuchElementException("ArraySet is empty");
        }
        return items.get(0);
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException("ArraySet is empty");
        }
        return items.get(items.size() - 1);
    }

    @Override
    public int size() {
        return items.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(items, (T) o, comparator) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return items.listIterator();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException("ArraySet is immutable");
    }

    /**
     * Returns the greatest element in this set strictly less than the
     * given element, or {@code null} if there is no such element.
     */
    @Override
    public T lower(T e) {
        int index = lessThan(e, false);
        return index < 0 ? null : items.get(index);
    }

    /**
     * Returns the greatest element in this set less than or equal to
     * the given element, or {@code null} if there is no such element.
     */
    @Override
    public T floor(T e) {
        int index = lessThan(e, true);
        return index < 0 ? null : items.get(index);
    }

    /**
     * Returns the least element in this set greater than or equal to
     * the given element, or {@code null} if there is no such element.
     */
    @Override
    public T ceiling(T e) {
        int index = greaterThan(e, true);
        return index < items.size() ? items.get(index) : null;
    }

    /**
     * Returns the least element in this set strictly greater than the
     * given element, or {@code null} if there is no such element.
     */
    @Override
    public T higher(T e) {
        int index = greaterThan(e, false);
        return index < items.size() ? items.get(index) : null;
    }


    @Override
    public NavigableSet<T> descendingSet() {
        if (descending == null) {
            descending = new ArraySet<>(items, Collections.reverseOrder(comparator));
        }
        return descending;
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    // TODO
    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int fromI = greaterThan(fromElement, fromInclusive);
        int toI = lessThan(toElement, toInclusive) + 1;
        if (toI + 1 == fromI) {
            toI = fromI;
        }
        return new ArraySet<>(items.subList(fromI, toI), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        return new ArraySet<>(items.subList(0, lessThan(toElement, inclusive) + 1), comparator);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        return new ArraySet<>(items.subList(greaterThan(fromElement, inclusive), items.size()), comparator);
    }

    private int greaterThan(T element, boolean orEqual) {
        int index = Collections.binarySearch(items, element, comparator);
        return index < 0 ? ~index : !orEqual ? ++index : index;
    }

    private int lessThan(T element, boolean orEqual) {
        int index = Collections.binarySearch(items, element, comparator);
        return index < 0 ? ~index - 1 : !orEqual ? --index : index;
    }
}