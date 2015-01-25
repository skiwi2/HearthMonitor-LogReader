package com.github.skiwi2.hearthmonitor.logreader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Used to iterate over elements while also having the possibility to check if the next elements matches a predicate.
 *
 * @author Frank van Heeswijk
 * @param <E>   The type of elements
 */
public interface MatchingIterator<E> extends Iterator<E> {
    /**
     * Returns whether the next element matches the given condition.
     *
     * @param condition The condition predicate that the next element may match
     * @return  Whether the next element matches the given condition.
     * @throws  java.lang.NullPointerException  If condition is null.
     */
    boolean nextMatches(final Predicate<? super E> condition);

    /**
     * Returns a matching iterator constructed from an iterator.
     *
     * @param iterator  The input iterator
     * @param <E>   The type of the elements in the iterator
     * @return  The matching iterator constructed from the iterator.
     */
    static <E> MatchingIterator<E> fromIterator(final Iterator<? extends E> iterator) {
        return new MatchingIterator<E>() {
            private final List<E> peekedElements = new ArrayList<>();

            @Override
            public boolean hasNext() {
                Optional<E> peekElement = peek();
                return peekElement.isPresent();
            }

            @Override
            public E next() {
                if (!peekedElements.isEmpty()) {
                    return peekedElements.remove(0);
                }
                return iterator.next();
            }

            @Override
            public boolean nextMatches(final Predicate<? super E> condition) {
                Objects.requireNonNull(condition, "condition");
                Optional<E> peekElement = peek();
                return (peekElement.isPresent() && condition.test(peekElement.get()));
            }

            /**
             * Returns an optional containing the next element, or an empty optional if there is none.
             *
             * @return  The optional containing the next element, or the empty optional if there is none.
             */
            private Optional<E> peek() {
                if (!peekedElements.isEmpty()) {
                    return Optional.ofNullable(peekedElements.get(0));
                }
                if (!iterator.hasNext()) {
                    return Optional.empty();
                }
                E element = iterator.next();
                peekedElements.add(element);
                return Optional.ofNullable(element);
            }
        };
    }
}
