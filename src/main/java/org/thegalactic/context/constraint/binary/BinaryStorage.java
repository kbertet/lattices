package org.thegalactic.context.constraint.binary;

/*
 * BinaryStorage.java
 *
 * Copyright: 2016 The Galactic Organization, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices.
 * You can redistribute it and/or modify it under the terms of the CeCILL-B license.
 */
import java.util.BitSet;

/**
 * Binary Storage.
 */
public final class BinaryStorage {

    /**
     * EXCEPTION_SIZE.
     */
    private static final String EXCEPTION_SIZE = "BooleanStorage objects must have the same size";

    /**
     * # values.
     */
    private final int size;

    /**
     * Truth values.
     */
    private final BitSet values;

    /**
     * Factory method to construct a binary storage.
     *
     * @param size number of bits
     *
     * @return a new BinaryStorage object
     */
    public static BinaryStorage create(final int size) {
        return new BinaryStorage(size);
    }

    /**
     * This class is not designed to be publicly instantiated.
     *
     * @param size number of bits
     */
    private BinaryStorage(final int size) {
        this.size = size;
        this.values = new BitSet(size);
    }

    /**
     * Get truth value.
     *
     * @param index truth value to be get
     *
     * @return truth value
     */
    public boolean get(final int index) {
        return this.values.get(index);
    }

    /**
     * Set truth value.
     *
     * @param index truth value to be set
     * @param truth new truth value
     *
     * @return this for chaining.
     */
    public BinaryStorage set(final int index, final boolean truth) {
        this.values.set(index, truth);
        return this;
    }

    /**
     * Reduce truth value.
     *
     * @param index truth value to be reduced
     * @param truth truth value
     *
     * @return this for chaining.
     */
    public BinaryStorage reduce(final int index, final boolean truth) {
        this.values.set(index, truth || this.values.get(index));
        return this;
    }

    /**
     * Extends truth value.
     *
     * @param index truth value to be extended
     * @param truth truth value
     *
     * @return this for chaining.
     */
    public BinaryStorage extend(final int index, final boolean truth) {
        this.values.set(index, truth && this.values.get(index));
        return this;
    }

    /**
     * Intersection.
     *
     * @param storage BinaryStorage
     *
     * @return this for chaining.
     *
     * @throws IllegalArgumentException
     */
    public BinaryStorage intersection(final BinaryStorage storage) {
        if (storage.size == this.size) {
            this.values.and(storage.values);
        } else {
            throw new IllegalArgumentException(EXCEPTION_SIZE);
        }
        return this;
    }

    /**
     * Union.
     *
     * @param storage BinaryStorage
     *
     * @return this for chaining.
     *
     * @throws IllegalArgumentException
     */
    public BinaryStorage union(final BinaryStorage storage) {
        if (storage.size == this.size) {
            this.values.or(storage.values);
        } else {
            throw new IllegalArgumentException(EXCEPTION_SIZE);
        }
        return this;
    }

    /**
     * Get the size.
     *
     * @return the size
     */
    public int size() {
        return this.size;
    }

    /**
     * Returns a String representation of this object.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append('[');

        for (int index = 0; index < this.size; index++) {
            if (this.values.get(index)) {
                stringBuilder.append('1');
            } else {
                stringBuilder.append('0');
            }
        }

        stringBuilder.append(']');

        return stringBuilder.toString();
    }
}
