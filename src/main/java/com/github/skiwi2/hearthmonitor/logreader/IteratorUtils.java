package com.github.skiwi2.hearthmonitor.logreader;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * Utility class to do complex things with iterators.
 *
 * @author Frank van Heeswijk
 */
public final class IteratorUtils {
    private IteratorUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an iterator that is a view on the given iterator only considering elements that match the condition predicate.
     *
     * @param iterator  The input iterator
     * @param condition The condition predicate that elements have to match
     * @param <E>   The type of elements
     * @return  The iterator that is a view on the given iterator only considering elements that match the condition predicate.
     */
    public static <E> Iterator<E> filteredIterator(final Iterator<? extends E> iterator, final Predicate<? super E> condition) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false)
            .filter(condition)
            .iterator();
    }
}
