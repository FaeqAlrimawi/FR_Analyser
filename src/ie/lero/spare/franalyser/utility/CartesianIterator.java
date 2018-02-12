package ie.lero.spare.franalyser.utility;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.IntFunction;

public class CartesianIterator<T> implements Iterator<T[]> {
    private final T[][] sets;
    private final IntFunction<T[]> arrayConstructor;

    private int count = 0;
    private T[] next = null;

    public CartesianIterator(T[][] sets, IntFunction<T[]> arrayConstructor) {
        Objects.requireNonNull(sets);
        Objects.requireNonNull(arrayConstructor);

        this.sets = copySets(sets);
        this.arrayConstructor = arrayConstructor;
    }

    private static <T> T[][] copySets(T[][] sets) {
        // If any of the arrays are empty, then the entire iterator is empty.
        // This prevents division by zero in `hasNext`.
        for (T[] set : sets) {
            if (set.length == 0) {
                return Arrays.copyOf(sets, 0);
            
            }
        }
        return sets.clone();
    }

    @Override
    public boolean hasNext() {
        if (next != null) {
            return true;
        }

        int tmp = count;
        T[] value = arrayConstructor.apply(sets.length);
        for (int i = 0; i < value.length; i++) {
            T[] set = sets[i];

            int radix = set.length;
            int index = tmp % radix;

            value[i] = set[index];

            tmp /= radix;
        }

        if (tmp != 0) {
            // Overflow.
            return false;
        }

        next = value;
        count++;

        return true;
    }

    @Override
    public T[] next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        T[] tmp = next;
        next = null;
        return tmp;
    }
}