package fr.kbertet.lattice.io;

/*
 * ContextReaderCsv.java
 *
 * Copyright: 2010-2014 Karell Bertet, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices, free package. You can redistribute it and/or modify
 * it under the terms of CeCILL-B license.
 */

import fr.kbertet.lattice.Context;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * This class defines the way for reading a context from a csv file.
 *
 * ![ContextReaderCsv](ContextReaderCsv.png)
 *
 * @uml ContextReaderCsv.png
 * !include src/fr/kbertet/lattice/io/ContextReaderCsv.iuml
 * !include src/fr/kbertet/lattice/io/ContextReader.iuml
 *
 * hide members
 * show ContextReaderCsv members
 * class ContextReaderCsv #LightCyan
 * title ContextReaderCdv UML graph
 */
public final class ContextReaderCsv implements ContextReader {
    /**
     * This class is not designed to be publicly instantiated.
     */
    private ContextReaderCsv() {
    }

    /**
     * The singleton instance.
     */
    private static ContextReaderCsv instance = null;

    /**
     * Return the singleton instance of this class.
     *
     * @return  the singleton instance
     */
    public static ContextReaderCsv getInstance() {
        if (instance == null) {
            instance = new ContextReaderCsv();
        }
        return instance;
    }

    /**
     * Register this class for reading .csv files.
     */
    public static void register() {
        ContextReaderFactory.register(ContextReaderCsv.getInstance(), "csv");
    }

    /**
     * Read a context from a csv file.
     *
     * The following format is respected:
     *
     * The first line contains the attribute names, the other lines contains the observations identifier followed by boolean values
     *
     * ~~~
     * "",a,b,c,d,e
     * 1,1,0,1,0,0
     * 2,1,1,0,0,0
     * 3,0,1,0,1,1
     * 4,0,0,1,0,1
     * ~~~
     *
     * If the first attribute is the empty string, the first column corresponds to the individual identifiers. In the other case,
     * the individual identifiers will be generated by successive integers.
     *
     * ~~~
     * a,b,c,d,e
     * 1,0,1,0,0
     * 1,1,0,0,0
     * 0,1,0,1,1
     * 0,0,1,0,1
     * ~~~
     *
     * @param   context  a context to read
     * @param   file     a file
     *
     * @throws  IOException  When an IOException occurs
     */
    public void read(Context context, BufferedReader file) throws IOException {
        CSVParser parser = CSVFormat.RFC4180.parse(file);
        List<CSVRecord> records = parser.getRecords();
        int length = records.size();

        if (length == 0) {
            throw new IOException("CSV cannot be empty");
        }

        CSVRecord attributes = records.get(0);
        int size = attributes.size();

        if (size == 1 && attributes.get(0).equals("")) {
            throw new IOException("Attribute size cannot be 0");
        }

        int first;

        if (attributes.get(0).equals("")) {
            first = 1;
        } else {
            first = 0;
        }

        for (int i = first; i < size; i++) {
            String attribute = attributes.get(i);
            if (!context.addToAttributes(attribute)) {
                throw new IOException("Duplicated attribute");
            }

            if (attribute.equals("")) {
                throw new IOException("Empty attribute");
            }
        }

        for (int j = 1; j < length; j++) {
            CSVRecord record = records.get(j);

            if (record.size() != size) {
                throw new IOException("Line has a different number of attributes");
            }

            String identifier;

            if (first == 1) {
                identifier = record.get(0);
            } else {
                identifier = new String(j + "");
            }

            context.addToObservations(identifier);

            for (int i = first; i < size; i++) {
                if (record.get(i).equals("1")) {
                    context.addExtentIntent(identifier, attributes.get(i));
                }
            }
        }

        parser.close();
        context.setBitSets();
    }
}

