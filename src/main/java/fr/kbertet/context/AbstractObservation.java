package fr.kbertet.context;

/*
 * AbstractObservation.java
 *
 * Copyright: 2010-2014 Karell Bertet, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices, free package. You can redistribute it and/or modify
 * it under the terms of CeCILL-B license.
 */

import fr.kbertet.context.attribute.Attribute;

/**
 * A named observation for a context.
 */
public abstract class AbstractObservation implements Observation {
    /**
     * Get the value associated with an attribute.
     *
     * @param attribute The attribute
     *
     * @return The value associated with an attribute
     */
    public Value getValue(Attribute attribute) {
        return this.getContext().getValue(this, attribute);
    }
}
