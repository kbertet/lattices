package lattice;

/*
 * Context.java
 *
 * Copyright: 2013-2014 Karell Bertet, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices, free package. You can redistribute it and/or modify
 * it under the terms of CeCILL-B license.
 */

import dgraph.Node;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class gives a standard representation for a context.
 * A context is a binary table, with attributes in column, and observations
 * in row.
 *
 * A context is composed of
 *
 * - attributes, a treeset of comparable objects;
 * - observations, a treeset of comparable objects;
 * - a Galois connexion (extent,intent) between objects and attributes where
 * `extent` is a TreeMap that associates to each attribute a TreeSet of observations and
 * `intent` is a TreeMap that associates  to each observation a TreeSet of attributes.
 *
 * This class provides methods implementing classical operation on a context:
 * closure, reduction, reverse, ...
 *
 * A context owns properties of a closure system, and thus extends the abstract class
 * {@link ClosureSystem} and implements methods {@link #getSet} and {@link #closure}.
 * Therefore, the closed set lattice of a context can be generated by invoking method {@link ClosureSystem#closedSetLattice} of a closure system.
 * However, this class also provides a method generating the concept lattice of this component
 * by completing each closed set of the closed set lattice.
 *
 * A context can be instancied from and save to a text file in the following format:
 *
 * - the list of observations separated by a space on the first line;
 * - the list of attrbutes separated by a space on the second line;
 * - then, for each observations o, the list of its intent on a line, written like o a1 a2 ...
 *
 * ~~~
 * Observations: 1 2 3
 * Attributes: a b c d e
 * 1 a c
 * 2 a b
 * 3 b d e
 * 4 c e
 * ~~~
 *
 * ![Context](Context.png)
 *
 * @uml Context.png
 * !include src/lattice/Context.iuml
 * !include src/lattice/ClosureSystem.iuml
 *
 * hide members
 * show Context members
 * class Context #LightCyan
 * title Context UML graph
 */
public class Context extends ClosureSystem {
    /*
     * Register txt writer and reader
     */
    static {
        if (ContextWriterFactory.get("txt") == null) {
            ContextWriterText.register();
        }
        if (ContextReaderFactory.get("txt") == null) {
            ContextReaderText.register();
        }
    }

    /* ------------- FIELD ------------------ */

    /**
     * A set of observations.
     */
    private TreeSet<Comparable> observations;

    /**
     * A set of attributes.
     */
    private TreeSet<Comparable> attributes;

    /**
     * A map to associate a set of attributes to each observation.
     */
    private TreeMap<Comparable, TreeSet<Comparable>> intent;

    /**
     * A map to associate a set of observations to each attribute.
     */
    private TreeMap<Comparable, TreeSet<Comparable>> extent;

    /* ------------- BITSET ADDON ------------------ */

    /**
     * A bit set for attributes.
     */
    private BitSet bitsetAttributes;

    /**
     * A bit set for observations.
     */
    private BitSet bitsetObservations;

    /**
     * A bit set for intent.
     */
    private TreeMap<Comparable, BitSet> bitsetIntent;

    /**
     * A bit set for extent.
     */
    private TreeMap<Comparable, BitSet> bitsetExtent;

    /**
     * An array for observations.
     */
    private ArrayList<Comparable> arrayObservations;

    /**
     * An array for attributes.
     */
    private ArrayList<Comparable> arrayAttributes;

    /* ------------- CONSTRUCTORS ------------------ */

    /**
     * Constructs a new empty context.
     */
    public Context() {
        this.init();
    }

    /**
     * Constructs a new context as a copy of the specified context.
     *
     * @param   context  context to be copied
     */
    public Context(Context context) {
        this();
        this.attributes.addAll(context.getAttributes());
        this.observations.addAll(context.getObservations());
        for (Comparable o : context.getObservations()) {
            this.intent.put(o, new TreeSet(context.getIntent(o)));
        }
        for (Comparable a : context.getAttributes()) {
            this.extent.put(a, new TreeSet(context.getExtent(a)));
        }
        this.setBitSets();
    }

    /**
     * Constructs this component from the specified file.
     *
     * The file have to respect a certain format:
     *
     * The list of observations separated by a space on the first line ;
     * the list of attrbutes separated by a space on the second line ;
     * then, for each observations o, the list of its intent on a line, written like o a1 a2 ...
     *
     * ~~~
     * Observations: 1 2 3
     * Attributes: a b c d e
     * 1 a c
     * 2 a b
     * 3 b d e
     * 4 c e
     * ~~~
     *
     * Each observation must be declared on the first line, otherwise, it is not added
     * Each attribute must be declared on the second line, otherwise, it is not added
     *
     * @param   filename  the name of the file
     *
     * @throws  IOException  When an IOException occurs
     */
    public Context(String filename) throws IOException {
        this.parse(filename);
    }

    /**
     * Initialise the context.
     *
     * @return  this for chaining
     */
    public Context init() {
        this.observations = new TreeSet();
        this.attributes = new TreeSet();
        this.intent = new TreeMap();
        this.extent = new TreeMap();
        this.bitsetAttributes = new BitSet();
        this.bitsetObservations = new BitSet();
        this.bitsetIntent = new TreeMap();
        this.bitsetExtent = new TreeMap();
        this.arrayObservations = new ArrayList();
        this.arrayAttributes = new ArrayList();
        return this;
    }

    /**
     * Generates a partially random context.
     *
     * @param nbObs number of observations
     * @param nbGrp number of groups of attributes . Attributes are grouped such that each observation has one attribute per group.
     * @param nbAttrPerGrp number of attributes per group.
     * @return randomly generated context
     */
    public static Context random(int nbObs, int nbGrp, int nbAttrPerGrp) {
        Context ctx = new Context();
        // Generates Observations.
        for (int i = 1; i <= nbObs; i++) {
            ctx.addToObservations(Integer.toString(i));
        }
        // Generates Attributes.
        for (int i = 1; i <= nbGrp; i++) {
            for (int j = 1; j <= nbAttrPerGrp; j++) {
                int q = i;
                int rem = 0;
                String name = "";
                do {
                    rem = q % 26;
                    q = q / 26;
                    name = name + (char) (rem + 65);
                } while (q != 0);
                ctx.addToAttributes(name + Integer.toString(j)); // These names are cool ...
            }
        }
        // Generates all requested observations.
        Random r = new Random();
        int attr = r.nextInt(nbAttrPerGrp) + 1;
        for (int i = 1; i <= nbObs; i++) { // i : Observation
            for (int j = 1; j <= nbGrp; j++) { // j : Familly
                int q = j;
                int rem = 0;
                String name = "";
                do {
                    rem = q % 26;
                    q = q / 26;
                    name = name + (char) (rem + 65);
                } while (q != 0);
               name = name + Integer.toString(attr); // These names are really cool, aren't they ?
               ctx.addExtentIntent(Integer.toString(i), name);
               attr = r.nextInt(nbAttrPerGrp) + 1;
            }
        }
        ctx.setBitSets();
        return ctx;
    }

    /* --------------- HANDLING METHODS FOR ATTRIBUTES AND OBSERVATIONS ------------ */

   /**
    * Returns the set of attributes of this component.
    *
    * @return  the set of attributes of this component
    */
    public TreeSet<Comparable> getAttributes() {
        return this.attributes;
    }

    /**
     * Checks if the specified attribute belong to this component.
     *
     * @param   att  an attribute
     *
     * @return  true if the attribute belongs to this component
     */
    public boolean containsAttribute(Comparable att) {
        return this.attributes.contains(att);
    }

    /**
     * Checks if the specified set of attributes belongs to this component.
     *
     * @param   set  set of attributes
     *
     * @return  true if all attributes belong to this component
     */
    public boolean containsAllAttributes(TreeSet<Comparable> set) {
        return this.attributes.containsAll(set);
    }

    /**
     * Adds the specified element to the set of attributes of this component.
     *
     * @param   att  an attribute
     *
     * @return  true if the attribute was successfully added
     */
    public boolean addToAttributes(Comparable att) {
        if (!this.containsAttribute(att)) {
            this.extent.put(att, new TreeSet<Comparable>());
        }
        boolean ok = this.attributes.add(att);
        this.setBitSets();
        return ok;
    }

    /**
     * Adds the set of specified element to the set of attributes of this component.
     *
     * @param   set  set of attributes
     *
     * @return  true if all attributes were successfully added
     */
    public boolean addAllToAttributes(TreeSet<Comparable> set) {
        boolean all = true;
        for (Comparable att : set) {
            if (!this.addToAttributes(att)) {
                all = false;
            }
        }
        this.setBitSets();
        return all;
    }

    /**
     * Removes the specified element from the set of attributes of this component
     * and from all the intents it belongs to.
     *
     * @param   att  an attribute
     *
     * @return  true if the attribute was successfully removed
     */
    public boolean removeFromAttributes(Comparable att) {
        this.extent.remove(att);
        for (Comparable o : this.getObservations()) {
            this.intent.get(o).remove(att);
        }
        boolean ok = this.attributes.remove(att);
        this.setBitSets();
        return ok;
    }

   /**
    * Returns the set of observations of this component.
    *
    * @return  the set of observations
    */
    public TreeSet<Comparable> getObservations() {
        return this.observations;
    }

    /**
     * Checks if the specified observation belongs to this component.
     *
     * @param   obs  an observation
     *
     * @return  true if the observation belongs to this component
     */
    public boolean containsObservation(Comparable obs) {
        return this.observations.contains(obs);
    }

    /**
     * Checks if the specified set of observations belong to this component.
     *
     * @param   set  set of observations
     *
     * @return  true if all the observations are in this component
     */
    public boolean containsAllObservations(TreeSet<Comparable> set) {
        return this.observations.containsAll(set);
    }

    /**
     * Adds the specified element to the set of observations of this component.
     *
     * @param   obs  an observation
     *
     * @return  true if the observation was successfully added
     */
    public boolean addToObservations(Comparable obs) {
        if (!this.containsObservation(obs)) {
            this.intent.put(obs, new TreeSet<Comparable>());
        }
        boolean ok = this.observations.add(obs);
        this.setBitSets();
        return ok;
    }

    /**
     * Adds the set of specified element to the set of observations of this component.
     *
     * @param   set  set of observations
     *
     * @return  true if all observations were successfully added
     */
    public boolean addAllToObservations(TreeSet<Comparable> set) {
        boolean all = true;
        for (Comparable obs : set) {
            if (!this.addToObservations(obs)) {
                all = false;
            }
        }
        this.setBitSets();
        return all;
    }

    /**
     * Removes the specified element from the set of observations of this component.
     * and from all the extents it belongs to
     *
     * @param   obs  an observation
     *
     * @return  true if the observation was removed
     */
    public boolean removeFromObservations(Comparable obs) {
        this.intent.remove(obs);
        for (Comparable att : this.getAttributes()) {
            this.extent.get(att).remove(obs);
        }
        boolean ok = this.observations.remove(obs);
        this.setBitSets();
        return ok;
    }

    /**
     * Set the needed structures for the bitset optimization.
     * WARNING: this must be called each time your dataset change
     */
    public void setBitSets() {
        this.setMaps();
        this.setBitSetsIntentExtent();
    }

    /**
     * Set the mapping structure for the bitset optimization.
     */
    private void setMaps() {
        this.arrayAttributes = new ArrayList();
        this.arrayObservations = new ArrayList();
        Iterator<Comparable> i = this.attributes.iterator();
        while (i.hasNext()) {
            this.arrayAttributes.add(i.next());
        }
        i = this.observations.iterator();
        while (i.hasNext()) {
            this.arrayObservations.add(i.next());
        }
    }

    /**
     * Set the extent and intent structures for the bitset optimization.
     */
    private void setBitSetsIntentExtent() {
        this.bitsetIntent = new TreeMap();
        this.bitsetExtent = new TreeMap();
        Iterator<Comparable> i = this.attributes.iterator();
        BitSet b = new BitSet(this.observations.size());
        while (i.hasNext()) {
            Comparable att = i.next();
            for (Comparable c : this.extent.get(att)) {
                b.set(this.arrayObservations.indexOf(c));
            }
            this.bitsetExtent.put(att, (BitSet) b.clone());
            b.clear();
        }
        i = this.observations.iterator();
        b = new BitSet(this.attributes.size());
        while (i.hasNext()) {
            Comparable obs = i.next();
            for (Comparable c : this.intent.get(obs)) {
                b.set(this.arrayAttributes.indexOf(c));
            }
            this.bitsetIntent.put(obs, (BitSet) b.clone());
            b.clear();
        }
    }

    /* --------------- HANDLING METHODS FOR INTENT AND EXTENT ------------ */

    /**
     * Returns the set of attributes that are intent of the specified observation.
     *
     * @param   obs  an observation
     *
     * @return  the set of attributes
     */
    public TreeSet<Comparable> getIntent(Comparable obs) {
        if (this.containsObservation(obs)) {
            return this.intent.get(obs);
        } else {
            return new TreeSet();
        }
    }

    /**
     * Returns the set of attributes that are all intent of observations of the specified set.
     *
     * @param   set  set of observations
     *
     * @return  the set of observations
     */
    public TreeSet<Comparable> getIntent(TreeSet<Comparable> set) {
        TreeSet<Comparable> resIntent = new TreeSet(this.getAttributes());
        for (Comparable obs : set) {
            resIntent.retainAll(this.getIntent(obs));
        }
        return resIntent;
    }

    /**
     * Return the number of attributes that are all intent of observations of the specified set.
     *
     * @param   set  set of observations
     *
     * @return  the number of attributes
     */
    public int getIntentNb(TreeSet<Comparable> set) {
        int size = this.getAttributes().size();
        BitSet obsIntent = new BitSet(size);
        obsIntent.set(0, size);
        for (Comparable obs : set) {
            try {
                obsIntent.and(this.bitsetIntent.get(obs));
            } catch (NullPointerException e) {
                return 0;
            }
        }
        return obsIntent.cardinality();
    }

    /**
     * Checks if the second specified element is an intent of the first specified element.
     *
     * @param   obs  an observation
     * @param   att  an attribute
     *
     * @return  true if the attribute is an intent of the observation
     */
    public boolean containAsIntent(Comparable obs, Comparable att) {
        if (this.containsObservation(obs) && this.containsAttribute(att)) {
            return this.intent.get(obs).contains(att);
        } else {
            return false;
        }
    }

    /**
     * Returns the set of observations that are intent of the specified attribute.
     *
     * @param   att  an attribute
     *
     * @return  the set of observations
     */
    public TreeSet<Comparable> getExtent(Comparable att) {
        if (this.containsAttribute(att)) {
            return this.extent.get(att);
        } else {
            return new TreeSet();
        }
    }

    /**
     * Returns the set of observations that are all intent of attributes of the specified set.
     *
     * @param   set  set of attributes
     *
     * @return  the set of observations
     */
    public TreeSet<Comparable> getExtent(TreeSet<Comparable> set) {
        TreeSet<Comparable> attExtent = new TreeSet(this.getObservations());
        for (Comparable att : set) {
            attExtent.retainAll(this.getExtent(att));
        }
        return attExtent;
    }

    /**
     * Return the number of observations that are all intent of attributes of the specified set.
     *
     * @param   set  set of attributes
     *
     * @return  the number of observations
     */
    public int getExtentNb(TreeSet<Comparable> set) {
        int size = this.getObservations().size();
        BitSet attExtent = new BitSet(size);
        attExtent.set(0, size);
        for (Comparable att : set) {
            try {
                attExtent.and(this.bitsetExtent.get(att));
            } catch (NullPointerException e) {
                return 0;
            }
        }
        return attExtent.cardinality();
    }

    /**
     * Checks if the second specified element is an extent of the first specified element.
     *
     * @param   att  an attribute
     * @param   obs  an observation
     *
     * @return  true if the proposition is true
     */
    public boolean containAsExtent(Comparable att, Comparable obs) {
        if (this.containsObservation(obs) && this.containsAttribute(att)) {
            return this.extent.get(att).contains(obs);
        } else {
            return false;
        }
    }

    /**
     * Adds the second specified element as intent of the first one,
     * and the first one as extent of the second one.
     * The first one has to belong to the observations set
     * and the second one to the attribute set.
     *
     * @param   obs  an observation
     * @param   att  an attribute
     *
     * @return  true if both were added
     */
    public boolean addExtentIntent(Comparable obs, Comparable att) {
        if (this.containsObservation(obs) && this.containsAttribute(att)) {
            boolean ok = this.intent.get(obs).add(att) && this.extent.get(att).add(obs);
            this.setBitSets();
            return ok;
        } else {
            return false;
        }
    }

    /**
     * Removes the second specified element from the intent of the first one,
     * and the first one from the extent of the second one.
     * The first one has to belong to the observations set
     * and the second one to the attribute set.
     *
     * @param   obs  an observation
     * @param   att  an attribute
     *
     * @return  true if both were removed
     */
    public boolean removeExtentIntent(Comparable obs, Comparable att) {
        if (this.containsObservation(obs) && this.containsAttribute(att)) {
            boolean ok = this.intent.get(obs).remove(att) && this.extent.get(att).remove(obs);
            this.setBitSets();
            return ok;
        } else {
            return false;
        }
    }

    /* --------------- CONTEXT HANDLING METHODS ------------ */

    /**
     * Returns a String representation of this component.
     * The following format is respected:
     *
     * The list of observations separated by a space on the first line ;
     * the list of attrbutes separated by a space on the second line ;
     * then, for each observations o, the list of its intent on a line, written like o a1 a2 ...
     *
     * ~~~
     * Observations: 1 2 3
     * Attributes: a b c d e
     * 1 a c
     * 2 a b
     * 3 b d e
     * 4 c e
     * ~~~
     *
     * @return  the string representation of this component
     */
    @Override
    public String toString() {
        StringBuffer string = new StringBuffer();
        string.append("Observations: ");
        for (Comparable o : this.observations) {
            // first line : All observations separated by a space
            // a StringTokenizer is used to delete spaces in the
            // string description of each observation
            StringTokenizer st = new StringTokenizer(o.toString());
            while (st.hasMoreTokens()) {
                string.append(st.nextToken());
            }
            string.append(" ");
        }

        string.append("\nAttributes: ");
        for (Comparable a : this.attributes) {
            // second line : All attributes separated by a space
            // a StringTokenizer is used to delete spaces in the
            // string description of each observation
            StringTokenizer st = new StringTokenizer(a.toString());
            while (st.hasMoreTokens()) {
                string.append(st.nextToken());
            }
            string.append(" ");
        }

        // next lines : All intents of observations, one on each line:
        // observation : list of attributes
        // a StringTokenizer is used to delete spaces in the
        // string description of each observation and attributes
        string.append("\n");
        for (Comparable o : this.observations) {
            StringTokenizer st = new StringTokenizer(o.toString());
            while (st.hasMoreTokens()) {
                string.append(st.nextToken());
            }
            string.append(" : ");
            for (Comparable a : this.getIntent(o)) {
                st = new StringTokenizer(a.toString());
                while (st.hasMoreTokens()) {
                    string.append(st.nextToken());
                }
                string.append(" ");
            }
            string.append("\n");
        }
        return string.toString();
    }

    /**
     * Save the description of this component in a file whose name is specified.
     *
     * @param   filename  the name of the file
     *
     * @throws  IOException  When an IOException occurs
     */
    public void save(final String filename) throws IOException {
        String extension = "";
        int index = filename.lastIndexOf('.');
        if (index > 0) {
            extension = filename.substring(index + 1);
        }
        BufferedWriter file = new BufferedWriter(new FileWriter(filename));
        ContextWriterFactory.get(extension).write(this, file);
        file.close();
    }

    /**
     * Parse the description of this component from a file whose name is specified.
     *
     * @param   filename  the name of the file
     *
     * @return  this for chaining
     *
     * @throws  IOException  When an IOException occurs
     */
    public Context parse(final String filename) throws IOException {
        this.init();
        String extension = "";
        int index = filename.lastIndexOf('.');
        if (index > 0) {
            extension = filename.substring(index + 1);
        }
        BufferedReader file = new BufferedReader(new FileReader(filename));
        ContextReaderFactory.get(extension).read(this, file);
        file.close();
        return this;
    }

    /**
     * Removes from this component reducible attributes.
     *
     * Reducible attributes are attributes equivalent by closure to others attributes.
     * They are computed by `getReducibleElements` od `ClosureSystem` in O(|A|^3|O|)
     *
     * @return  the set of reducibles removed attributes, with their equivalent attributes
     */
    public TreeMap<Comparable, TreeSet<Comparable>> attributesReduction() {
        // compute the reducible elements
        TreeMap red = this.getReducibleElements();
        // remove the reducible elements from the attributes set
        for (Object att : red.keySet()) {
            this.removeFromAttributes((Comparable) att);
        }
        return red;
    }

    /**
     * Removes from this component reducible observations.
     *
     * Reducible observations are attributes equivalent by closure to others observations.
     * They are computed by `getReducibleElements` od `ClosureSystem`
     * applied on the reverse context in O(|O|^3|A|)
     *
     * @return  the set of reducibles removed attributes, with their equivalent attributes
     */
    public TreeMap<Comparable, TreeSet<Comparable>> observationsReduction() {
        // compute the reducible elements of the reverse context
        this.reverse();
        TreeMap red = this.getReducibleElements();
        this.reverse();
        // remove the reducible elements from the observations set
        for (Object att : red.keySet()) {
            this.removeFromObservations((Comparable) att);
        }
        return red;
    }

    /**
     * Removes from this component reducible attributes and observations.
     *
     * They are computed by `attributesReduction` then
     * `observationsReduction` in O(|A|^3|O|+|O|^3|A|)
     *
     * @return  the set of reducibles removed attributes and observations with their equivalent elements
     */
    public TreeMap<Comparable, TreeSet<Comparable>> reduction() {
        TreeMap<Comparable, TreeSet<Comparable>> red = this.attributesReduction();
        red.putAll(this.observationsReduction());
        return red;
    }

    /**
     * Reverses this component by replacing attributes by observations and observations by
     * attributes. Intent and extent are exchanged in the same way.
     */
    public void reverse() {
        TreeSet<Comparable> tmp = this.attributes;
        this.attributes = this.observations;
        this.observations = tmp;
        TreeMap<Comparable, TreeSet<Comparable>> sauv = this.intent;
        this.intent = this.extent;
        this.extent = sauv;
    }

    /**
     * Return a new reversed Context.
     *
     * @return  a new reversed Context
     */
    public Context getReverseContext() {
        Context context = new Context(this);
        context.reverse();
        context.setBitSets();
        return context;
    }

    /* --------------- IMPLEMENTATION OF CLOSURE SYSTEM ABSTRACT METHODS ------------ */
    /* --------------- AND CONCEPT LATTICE GENERATION------------ */

    /**
     * Returns the set of attributes as elements set used by the lattice generator abstract class
     * to generate closed set lattice on attributes. The closed set lattice on abservations can
     * be otained using the reverse method of this class.
     *
     * @return  the set of attributes
     */
    @Override
    public TreeSet<Comparable> getSet() {
        return this.attributes;
    }

    /**
     * Builds the closure of a set X of attributes.
     *
     * The closure corresponds to the maximal set of attributes having the
     * same intent as the specified one.
     *
     * This treatment is performed in O(|A||O|)
     *
     * @param   set  a TreeSet of indexed elements
     *
     * @return  the closure of the set for this component
     */
    @Override
    public TreeSet<Comparable> closure(TreeSet<Comparable> set) {
        return this.getIntent(this.getExtent(set));
    }

    /**
     * Returns the set of union of observations that are intent with one of attributes of the specified set.
     *
     * @param   set  a specified set
     *
     * @return  the set of union of observations
     */
      public TreeSet<Comparable> getExtentUnion(TreeSet<Comparable> set) {
        TreeSet<Comparable> ext = new TreeSet();
        for (Comparable att : set) {
            for (Comparable obs : this.getExtent(att)) {
                if (this.containAsExtent(att, obs) && !ext.contains(obs)) {
                    ext.add(obs);
                }
            }
        }
        return ext;
    }

    /**
     * Builds the inverse of the closure operator of a set of observations.
     *
     * The inverse closure corresponds to the maximal set of observations having the
     * same intent as the specified one.
     * This treatment is performed in O(|A||O|)
     *
     * @param   set  a TreeSet of indexed elements
     *
     * @return  the closure of the set for this component
     */
    public ComparableSet inverseClosure(ComparableSet set) {
        return new ComparableSet(this.getExtent(this.getIntent((TreeSet) set)));
    }

    /**
     * Returns the concept lattice of this component.
     *
     * A true value of the boolean `diagram` indicates that the
     * Hasse diagramm of the lattice is computed (i.e. it is transitively reduced),
     * whereas a false value indicates that the lattice is transitively closed
     *
     * The closed set lattice is first generated using
     * `ConceptLattice closedSetLattice (boolean  diagram)`
     * Then, nodes of the lattice are completed as concepts.
     *
     * @param   diagram  a boolean indicating if the Hasse diagramm of the lattice is computed or not.
     *
     * @return  The concept lattice induced by this component
     */
    public ConceptLattice conceptLattice(boolean  diagram) {
        ConceptLattice csl = this.closedSetLattice(diagram);
        // TreeMap<Concept, Concept> nodes = new TreeMap<Concept, Concept>();
        for (Node n : csl.getNodes()) {
             Concept cl = (Concept) n;
             cl.putSetB(new ComparableSet(this.getExtent(cl.getSetA())));
        }
        return csl;
    }

    /**
     * Returns the lattice of this component.
     *
     * @return  The lattice induced by this component
     */
    public ConceptLattice lattice() {
        return this.conceptLattice(true);
    }
}
