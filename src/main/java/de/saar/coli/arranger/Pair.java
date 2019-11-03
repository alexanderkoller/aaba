package de.saar.coli.arranger;

/**
 * An immutable pair of values.
 *
 * @param <E>
 * @param <F>
 */
public class Pair<E,F> {
    private E left;
    private F right;

    public Pair(E left, F right) {
        this.left = left;
        this.right = right;
    }

    public E getLeft() {
        return left;
    }

    public F getRight() {
        return right;
    }
}
