package fr.kbertet.context.io;

/*
 * BurmeisterTest.java
 *
 * Copyright: 2010-2014 Karell Bertet, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices, free package. You can redistribute it and/or modify
 * it under the terms of CeCILL-B license.
 */

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.File;

import fr.kbertet.context.Context;

/**
 * Test the fr.kbertet.dgraph.io.SLFTest class.
 */
public class SLFTest {
    /**
     * Test getInstance.
     */
    @Test
    public void testGetInstance() {
        SLF serializer = SLF.getInstance();
        assertEquals(serializer, SLF.getInstance());
    }

    /**
     * Test read.
     */
    @Test
    public void testRead() {
        try {
            File file = File.createTempFile("junit", ".slf");
            String filename = file.getPath();
            Context context = new Context();
            context.addToAttributes("a");
            context.addToAttributes("b");
            context.addToAttributes("c");
            context.addToObservations("1");
            context.addToObservations("2");
            context.addToObservations("3");
            context.addExtentIntent("1", "a");
            context.addExtentIntent("1", "b");
            context.addExtentIntent("2", "a");
            context.addExtentIntent("3", "b");
            context.addExtentIntent("3", "c");
            context.save(filename);
            Context copy = new Context(filename);
            assertEquals(context.getAttributes(), copy.getAttributes());
            assertEquals(context.getObservations(), copy.getObservations());
            assertEquals(context.getIntent("1"), copy.getIntent("1"));
            assertEquals(context.getExtent("c"), copy.getExtent("c"));
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

