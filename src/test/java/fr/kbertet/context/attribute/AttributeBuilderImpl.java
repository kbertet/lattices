package fr.kbertet.context.attribute;

/*
 * AttributeBuilderImpl.java
 *
 * Copyright: 2010-2014 Karell Bertet, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices, free package. You can redistribute it and/or modify
 * it under the terms of CeCILL-B license.
 */

/**
 * Basic AttributeBuilder implementation.
 */
class AttributeBuilderImpl extends AbstractAttributeBuilder {
    /**
     * Create the attribute.
     *
     * @return the a new attribute
     */
    public Attribute create() {
        return new AttributeImpl(null, "", "");
    }
}
