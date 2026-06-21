package com.zaryxstudios.okaso.common;

import java.util.Objects;

public class Pair<L, R> {

    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    public L getLeft() { return left; }
    public R getRight() { return right; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> p = (Pair<?, ?>) o;
        return Objects.equals(left, p.left) && Objects.equals(right, p.right);
    }

    @Override
    public int hashCode() { return Objects.hash(left, right); }

    @Override
    public String toString() { return "(" + left + ", " + right + ")"; }
}
