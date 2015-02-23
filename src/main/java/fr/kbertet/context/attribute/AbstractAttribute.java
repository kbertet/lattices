package fr.kbertet.context.attribute;

/*
 * AbstractAttribute.java
 *
 * Copyright: 2010-2014 Karell Bertet, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices, free package. You can redistribute it and/or modify
 * it under the terms of CeCILL-B license.
 */

import fr.kbertet.context.Context;
import fr.kbertet.context.Observation;
import fr.kbertet.context.Value;

/**
 * A named attribute for a context.
 */
public abstract class AbstractAttribute implements Attribute {

    /**
     * Constructor.
     *
     * @param context The context the attribute belongs to
     * @param name    The attribute name
     */
    AbstractAttribute(String name, Context context) {
        this.context = context;
        this.name = name;
    }

    /**
     * Get the context.
     *
     * @return The context
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Convert a named attribute to a string.
     *
     * @return The string representation of a named attribute (i.e. its name)
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        String stringName = this.getName().replaceAll("[^a-zA-Z0-9 ]", "");
        String type = this.getType().replaceAll("[^a-zA-Z0-9 ]", "");
        if (stringName.contains(" ")) {
            stringBuilder.append("\"").append(stringName).append("\"");
        } else {
            stringBuilder.append(stringName);
        }
        stringBuilder.append("(");
        if (type.contains(" ")) {
            stringBuilder.append("\"").append(type).append("\"");
        } else {
            stringBuilder.append(type);
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    /**
     * Get the name of an attribute.
     *
     * @return The name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the value associated with an observation.
     *
     * @param observation The observation
     *
     * @return The value associated with an observation
     */
    public Value getValue(Observation observation) {
        return this.getContext().getValue(observation, this);
    }

    /**
     * Attribute name.
     */
    private final String name;

    /**
     * Attribute context.
     */
    private final Context context;
}
