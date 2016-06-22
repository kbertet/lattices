package org.thegalactic.context.constraint.numerical;

/*
 * NumericalStorage.java
 *
 * Copyright: 2016 The Galactic Organization, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices.
 * You can redistribute it and/or modify it under the terms of the CeCILL-B license.
 */
import java.util.ArrayList;

/**
 * Binary Storage.
 */
public final class NumericalStorage {

    /**
     * inf values.
     */
    private final ArrayList<Double> inf;

    /**
     * sup values.
     */
    private final ArrayList<Double> sup;

    /**
     * Factory method to construct a numerical storage.
     *
     * @param size number of values
     *
     * @return a new NumericalStorage object
     */
    public static NumericalStorage create(int size) {
        return new NumericalStorage(size);
    }

    /**
     * This class is not designed to be publicly instantiated.
     *
     * @param size number of values
     */
    private NumericalStorage(int size) {
        inf = new ArrayList<Double>(size);
        sup = new ArrayList<Double>(size);
        // Initialise as an empty set
        for (int index = 0; index < size; index++) {
            inf.add(index, Double.POSITIVE_INFINITY);
            sup.add(index, Double.NEGATIVE_INFINITY);
        }
    }

    /**
     * Set the set at specified index empty.
     *
     * @param index index of the numerical set
     *
     * @return this for chaining
     */
    public NumericalStorage setEmpty(int index) {
        inf.set(index, Double.POSITIVE_INFINITY);
        sup.set(index, Double.NEGATIVE_INFINITY);
        return this;
    }

    /**
     * Is the set at specified index empty?
     *
     * @param index index of the numerical set
     *
     * @return this for chaining
     */
    public boolean isEmpty(int index) {
        return inf.get(index) > sup.get(index);
    }

    /**
     * Set the set at specified index a point.
     *
     * @param index index of the numerical set
     * @param value the new value
     *
     * @return this for chaining
     */
    public NumericalStorage setPoint(int index, double value) {
        inf.set(index, value);
        sup.set(index, value);
        return this;
    }

    /**
     * Is the set at specified index a point?
     *
     * @param index index of the numerical set
     *
     * @return this for chaining
     */
    public boolean isPoint(int index) {
        return inf.get(index).equals(sup.get(index));
    }

    /**
     * Set the inf mark to specified value.
     *
     * @param index the specified index
     * @param value the new value
     *
     * @return this for chaining
     */
    public NumericalStorage setInf(int index, double value) {
        if (isEmpty(index)) {
            setPoint(index, value);
        } else if (value > sup.get(index)) {
            setEmpty(index);
        } else {
            inf.set(index, value);
        }
        return this;
    }

    /**
     * Set the sup mark to specified value.
     *
     * @param index the specified index
     * @param value the new value
     *
     * @return this for chaining
     */
    public NumericalStorage setSup(int index, double value) {
        if (isEmpty(index)) {
            setPoint(index, value);
        } else if (value < inf.get(index)) {
            setEmpty(index);
        } else {
            sup.set(index, value);
        }
        return this;
    }

    /**
     * Get the inf value at specified index.
     *
     * @param index the specified index
     *
     * @return the inf value
     */
    public double getInf(int index) {
        return inf.get(index);
    }

    /**
     * Get the sup value at specified index.
     *
     * @param index the specified index
     *
     * @return the inf value
     */
    public double getSup(int index) {
        return sup.get(index);
    }

    /**
     * Reduce by inf value.
     *
     * @param index value to be reduced
     * @param value value used to reduce
     *
     * @return this for chaining.
     */
    public NumericalStorage reduceInf(int index, double value) {
        inf.set(index, Math.max(inf.get(index), value));
        return this;
    }

    /**
     * Reduce by sup value.
     *
     * @param index value to be reduced
     * @param value value used to reduce
     *
     * @return this for chaining.
     */
    public NumericalStorage reduceSup(int index, double value) {
        sup.set(index, Math.min(sup.get(index), value));
        return this;
    }

    /**
     * Extend by inf value.
     *
     * @param index value to be extended
     * @param value value used to extend
     *
     * @return this for chaining
     */
    public NumericalStorage extendInf(int index, double value) {
        inf.set(index, Math.min(inf.get(index), value));
        return this;
    }

    /**
     * Extend by sup value.
     *
     * @param index value to be extended
     * @param value value used to extend
     *
     * @return this for chaining
     */
    public NumericalStorage extendSup(int index, double value) {
        sup.set(index, Math.max(sup.get(index), value));
        return this;
    }

    /**
     * Extends value.
     *
     * @param index value to be extended
     * @param value value used to extend
     *
     * @return this for chaining.
     */
    public NumericalStorage extend(int index, double value) {
        extendInf(index, value);
        extendSup(index, value);
        return this;
    }

    /**
     * Intersection.
     *
     * @param storage NumericalStorage
     *
     * @return this for chaining
     */
    public NumericalStorage intersection(NumericalStorage storage) {
        int size = inf.size();
        if (storage.inf.size() == size) {
            for (int index = 0; index < size; index++) {
                inf.set(index, Math.max(inf.get(index), storage.inf.get(index)));
                sup.set(index, Math.min(sup.get(index), storage.sup.get(index)));
            }
            return this;
        } else {
            throw new IllegalArgumentException("NumericalStorage objects must have the same size");
        }
    }

    /**
     * Union.
     *
     * An approximation is made for union when the two intervals are disjoint.
     * The result is the minimum interval containing both intervals.
     *
     * @param storage NumericalStorage
     *
     * @return this for chaining
     */
    public NumericalStorage union(NumericalStorage storage) {
        int size = inf.size();
        if (storage.inf.size() == size) {
            for (int index = 0; index < size; index++) {
                inf.set(index, Math.min(inf.get(index), storage.inf.get(index)));
                sup.set(index, Math.max(sup.get(index), storage.sup.get(index)));
            }
            return this;
        } else {
            throw new IllegalArgumentException("NumericalStorage objects must have the same size");
        }
    }

    /**
     * Get the size.
     *
     * @return the size
     */
    public int size() {
        return inf.size();
    }

    /**
     * Returns a String representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("[");

        int size = inf.size();
        for (int index = 0; index < size; index++) {
            if (index != 0) {
                builder.append(", ");
            }
            if (isEmpty(index)) {
                builder.append("@");
            } else if (isPoint(index)) {
                builder.append(inf.get(index));
            } else {
                builder.append("(");
                builder.append(inf.get(index));
                builder.append(",");
                builder.append(sup.get(index));
                builder.append(")");
            }
        }

        builder.append("]");

        return builder.toString();
    }
}
