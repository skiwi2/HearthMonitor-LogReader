package com.github.skiwi2.hearthmonitor.logreader.hearthstone;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.IntUnaryOperator;

/**
 * @author Frank van Heeswijk
 */
public class IntMatcher {
    private final int matchValue;
    private final BiPredicate<Integer, Integer> matchPredicate;

    private IntMatcher(final int matchValue, final BiPredicate<Integer, Integer> matchPredicate) {
        this.matchValue = matchValue;
        this.matchPredicate = Objects.requireNonNull(matchPredicate, "matchPredicate");
    }

    public boolean matches(final int value) {
        return matchPredicate.test(matchValue, value);
    }

    public IntMatcher applyFunction(final IntUnaryOperator function) {
        return new IntMatcher(function.applyAsInt(matchValue), matchPredicate);
    }

    public IntMatcher plus(final int value) {
        return applyFunction(i -> i + value);
    }

    public IntMatcher minus(final int value) {
        return applyFunction(i -> i - value);
    }

    public IntMatcher multiply(final int value) {
        return applyFunction(i -> i * value);
    }

    public IntMatcher divide(final int value) {
        return applyFunction(i -> i / value);
    }

    public static IntMatcher valueEquals(final int value) {
        return new IntMatcher(value, Integer::equals);
    }

    public static IntMatcher valueNotEquals(final int value) {
        return new IntMatcher(value, (i, j) -> !i.equals(j));
    }

    public static IntMatcher valueGreaterThan(final int value) {
        return new IntMatcher(value, (i, j) -> i < j);
    }

    public static IntMatcher valueLessThan(final int value) {
        return new IntMatcher(value, (i, j) -> i > j);
    }

    public static IntMatcher valueGreaterThanOrEqual(final int value) {
        return new IntMatcher(value, (i, j) -> i <= j);
    }

    public static IntMatcher valueLessThanOrEqual(final int value) {
        return new IntMatcher(value, (i, j) -> i >= j);
    }
}
