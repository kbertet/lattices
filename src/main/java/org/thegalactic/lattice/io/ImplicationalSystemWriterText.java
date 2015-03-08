package org.thegalactic.lattice.io;

/*
 * ImplicationalSystemWriterText.java
 *
 * Copyright: 2010-2014 Karell Bertet, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-thegalactic.
 * You can redistribute it and/or modify it under the terms of the CeCILL-B license.
 */
import java.io.BufferedWriter;
import java.io.IOException;

import org.thegalactic.lattice.ImplicationalSystem;

/**
 * This class defines the way for writing an implicational system as a text file.
 *
 * ![ImplicationalSystemWriterText](ImplicationalSystemWriterText.png)
 *
 * @uml ImplicationalSystemWriterText.png
 * !include resources/org/thegalactic/lattice/io/ImplicationalSystemWriterText.iuml
 * !include resources/org/thegalactic/lattice/io/ImplicationalSystemWriter.iuml
 *
 * hide members
 * show ImplicationalSystemWriterText members
 * class ImplicationalSystemWriterText #LightCyan
 * title ImplicationalSystemWriterText UML graph
 */
public final class ImplicationalSystemWriterText implements ImplicationalSystemWriter {

    /**
     * The singleton INSTANCE.
     */
    private static final ImplicationalSystemWriterText INSTANCE = new ImplicationalSystemWriterText();

    /**
     * Return the singleton INSTANCE of this class.
     *
     * @return the singleton INSTANCE
     */
    public static ImplicationalSystemWriterText getInstance() {
        return INSTANCE;
    }

    /**
     * Register this class for writing .txt files.
     */
    public static void register() {
        ImplicationalSystemWriterFactory.register(ImplicationalSystemWriterText.getInstance(), "txt");
    }

    /**
     * This class is not designed to be publicly instantiated.
     */
    private ImplicationalSystemWriterText() {
    }

    /**
     * Saves this component in a file.
     *
     * The following format is used:
     *
     * An implicational system can be instancied from and save to a text file in the following format:
     * A list of elements separated by a space in the first line ;
     * then, each rule on a line, written like [premise] -> [conclusion]
     * where elements are separated by a space.
     *
     * ~~~
     * a b c d e
     * a b -> c d
     * c d -> e
     * ~~~
     *
     * @param system a system to write
     * @param file   a file
     *
     * @throws IOException When an IOException occurs
     */
    public void write(ImplicationalSystem system, BufferedWriter file) throws IOException {
        file.write(system.toString());
    }
}
