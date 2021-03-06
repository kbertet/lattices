package org.thegalactic.lattice;

/*
 * ArrowRelation.java
 *
 * Copyright: 2010-2015 Karell Bertet, France
 * Copyright: 2015-2016 The Galactic Organization, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices.
 * You can redistribute it and/or modify
 * it under the terms of the CeCILL-B license.
 */
import java.io.IOException;
import java.util.TreeSet;

import org.thegalactic.context.Context;
import org.thegalactic.dgraph.ConcreteDGraph;
import org.thegalactic.dgraph.Edge;
import org.thegalactic.dgraph.Node;
import org.thegalactic.io.Filer;
import org.thegalactic.lattice.io.ArrowRelationIOFactory;

/**
 * The ArrowRelation class encodes arrow relation between meet &
 * join-irreductibles of a lattice.
 *
 * Let m and b be respectively meet and join irreductibles of a lattice.
 * Recall that m has a unique successor say m+ and j has a unique predecessor
 * say j-, then :
 *
 * - j "Up Arrow" m (stored as "Up") iff j is not less or equal than m and
 * j is less than m+
 * - j "Down Arrow" m (stored as "Down") iff j is not less or equal than m and
 * j- is less than m
 * - j "Up Down Arrow" m (stored as "UpDown") iff j "Up" m and j "Down" m
 * - j "Cross" m (stored as "Cross") iff j is less or equal than m
 * - j "Circ" m (stored as "Circ") iff neither j "Up" m nor j "Down" m nor
 * j "Cross" m
 *
 * ![ArrowRelation](ArrowRelation.png)
 *
 * @uml ArrowRelation.png
 * !include resources/org/thegalactic/dgraph/DGraph.iuml
 * !include resources/org/thegalactic/dgraph/Edge.iuml
 * !include resources/org/thegalactic/dgraph/Node.iuml
 * !include resources/org/thegalactic/lattice/ArrowRelation.iuml
 *
 * hide members
 * show ArrowRelation members
 * class ArrowRelation #LightCyan
 * title ArrowRelation UML graph
 */
public class ArrowRelation extends ConcreteDGraph {

    /**
     * Field used to encode up arrow relation.
     */
    private static Object up = "Up";

    /**
     * Field used to encode down arrow relation.
     */
    private static Object down = "Down";

    /**
     * Field used to encode up-down arrow relation.
     */
    private static Object updown = "UpDown";

    /**
     * Field used to encode cross arrow relation.
     */
    private static Object cross = "Cross";

    /**
     * Field used to encode circ arrow relation.
     */
    private static Object circ = "Circ";

    /**
     * Unique constructor of this component from a lattice.
     *
     * Nodes are join or meet irreductibles of the lattice. Edges content
     * encodes arrows as String "Up", "Down", "UpDown", "Cross", "Circ".
     *
     * @param lattice Lattice from which this component is deduced.
     */
    public ArrowRelation(Lattice lattice) {

        /*
         * Nodes are join or meet irreductibles of the lattice.
         */
        TreeSet<Node> joins = new TreeSet<Node>(lattice.joinIrreducibles());
        for (Node node : joins) {
            this.addNode(node);
        }
        TreeSet<Node> meets = new TreeSet<Node>(lattice.meetIrreducibles());
        for (Node node : meets) {
            this.addNode(node);
        }
        Lattice transitiveClosure = new Lattice(lattice);
        transitiveClosure.transitiveClosure();
        Lattice transitiveReduction = new Lattice(lattice);
        transitiveReduction.transitiveReduction();
        Node jminus = new Node();
        Node mplus = new Node();
        Object arrow = new Object();

        /*
         * Content of edges are arrows
         */
        for (Node j : joins) {
            for (Node m : meets) {
                mplus = (Node) transitiveReduction.getSuccessorNodes(m).first();
                jminus = (Node) transitiveReduction.getPredecessorNodes(j).first();
                if (transitiveClosure.getSuccessorNodes(j).contains(m) || j.equals(m)) {
                    arrow = ArrowRelation.cross;
                } else if (transitiveClosure.getSuccessorNodes(jminus).contains(m) || jminus.equals(m)) {
                    arrow = ArrowRelation.down;
                    if (transitiveClosure.getPredecessorNodes(mplus).contains(j) || mplus.equals(j)) {
                        arrow = ArrowRelation.updown;
                    }
                } else if (transitiveClosure.getPredecessorNodes(mplus).contains(j)) {
                    arrow = ArrowRelation.up;
                } else {
                    arrow = ArrowRelation.circ;
                }
                this.addEdge(j, m, arrow);
            }
        }
    }

    /**
     * Save the description of this component in a file whose name is specified.
     *
     * @param filename the name of the file
     *
     * @throws IOException When an IOException occurs
     */
    @Override
    public void save(final String filename) throws IOException {
        Filer.getInstance().save(this, ArrowRelationIOFactory.getInstance(), filename);
    }

    /**
     * Returns the table of the lattice, composed of the join and meet
     * irreducibles nodes.
     *
     * Each attribute of the table is a copy of a join irreducibles node. Each
     * observation of the table is a copy of a meet irreducibles node. An
     * attribute is extent of an observation when its join irreducible node is
     * in double arrow relation with the meet irreducible node in the lattice.
     *
     * @return the table of the lattice
     *
     * @todo Avoid using for (Object edge : this.getEdges()). Use for (Edge edge : this.getEdges())
     */
    public Context getDoubleArrowTable() {
        Context context = new Context();
        // observations are join irreductibles
        // attributes are meet irreductibles
        for (Object edge : this.getEdges()) {
            context.addToObservations(((Edge) edge).getSource());
            context.addToAttributes(((Edge) edge).getTarget());
        }
        // generation of extent-intent
        for (Object edge : this.getEdges()) {
            if (((Edge) edge).getContent() == ArrowRelation.updown) {
                context.addExtentIntent(((Edge) edge).getSource(), ((Edge) edge).getTarget());
            }
        }
        return context;
    }

    /**
     * Returns the table of the lattice, composed of the join and meet
     * irreducibles nodes.
     *
     * Each attribute of the table is a copy of a join irreducibles node. Each
     * observation of the table is a copy of a meet irreducibles node. An
     * attribute is extent of an observation when its join irreducible node is
     * in down arrow relation with the meet irreducible node in the lattice.
     *
     * @return the table of the lattice
     */
    public Context getDoubleDownArrowTable() {
        Context context = new Context();
        // observations are join irreductibles
        // attributes are meet irreductibles
        for (Object edge : this.getEdges()) {
            context.addToObservations(((Edge) edge).getSource());
            context.addToAttributes(((Edge) edge).getTarget());
        }
        // generation of extent-intent
        for (Object edge : this.getEdges()) {
            if (((Edge) edge).getContent() == ArrowRelation.down || ((Edge) edge).getContent() == ArrowRelation.updown) {
                context.addExtentIntent(((Edge) edge).getSource(), ((Edge) edge).getTarget());
            }
        }
        return context;
    }

    /**
     * Returns the table of the lattice, composed of the join and meet
     * irreducibles nodes.
     *
     * Each attribute of the table is a copy of a join irreducibles node. Each
     * observation of the table is a copy of a meet irreducibles node. An
     * attribute is extent of an observation when its join irreducible node is
     * in up arrow relation with the meet irreducible node in the lattice.
     *
     * @return the table of the lattice
     */
    public Context getDoubleUpArrowTable() {
        Context context = new Context();
        // observations are join irreductibles
        // attributes are meet irreductibles
        for (Object edge : this.getEdges()) {
            context.addToObservations(((Edge) edge).getSource());
            context.addToAttributes(((Edge) edge).getTarget());
        }
        // generation of extent-intent
        for (Object edge : this.getEdges()) {
            if (((Edge) edge).getContent() == ArrowRelation.up || ((Edge) edge).getContent() == ArrowRelation.updown) {
                context.addExtentIntent(((Edge) edge).getSource(), ((Edge) edge).getTarget());
            }
        }
        return context;
    }

    /**
     * Returns the table of the lattice, composed of the join and meet
     * irreducibles nodes.
     *
     * Each attribute of the table is a copy of a join irreducibles node. Each
     * observation of the table is a copy of a meet irreducibles node. An
     * attribute is extent of an observation when its join irreducible node is
     * in double arrow relation or circ relation with the meet irreducible node
     * in the lattice.
     *
     * @return the table of the lattice
     */
    public Context getDoubleCircArrowTable() {
        Context context = new Context();
        // observations are join irreductibles
        // attributes are meet irreductibles
        for (Object edge : this.getEdges()) {
            context.addToObservations(((Edge) edge).getSource());
            context.addToAttributes(((Edge) edge).getTarget());
        }
        // generation of extent-intent
        for (Object edge : this.getEdges()) {
            if (((Edge) edge).getContent() == ArrowRelation.updown || ((Edge) edge).getContent() == ArrowRelation.circ) {
                context.addExtentIntent(((Edge) edge).getSource(), ((Edge) edge).getTarget());
            }
        }
        return context;
    }

    /**
     * Returns true if and only if there is an up arrow between the source and
     * the target of
     * edge e.
     *
     * @param edge edge to be tested
     *
     * @return true if and only if there is an up arrow between the source and
     *         the target of
     *         edge e
     */
    public boolean isUp(Edge edge) {
        return edge.getContent() == ArrowRelation.up;
    }

    /**
     * Returns true if and only if there is an down arrow between the source and
     * the target of
     * edge e.
     *
     * @param edge edge to be tested
     *
     * @return true if and only if there is an down arrow between the source and
     *         the target of
     *         edge e
     */
    public boolean isDown(Edge edge) {
        return edge.getContent() == ArrowRelation.down;
    }

    /**
     * Returns true if and only if there is an up-down arrow between the source
     * and the target
     * of edge e.
     *
     * @param edge edge to be tested
     *
     * @return true if and only if there is an up-down arrow between the source
     *         and the target
     *         of edge e
     */
    public boolean isUpDown(Edge edge) {
        return edge.getContent() == ArrowRelation.updown;
    }

    /**
     * Returns true if and only if there is an cross arrow between the source
     * and the target
     * of edge e.
     *
     * @param edge edge to be tested
     *
     * @return true if and only if there is an cross arrow between the source
     *         and the target
     *         of edge e
     */
    public boolean isCross(Edge edge) {
        return edge.getContent() == ArrowRelation.cross;
    }

    /**
     * Returns true if and only if there is an circ arrow between the source and
     * the target of
     * edge e.
     *
     * @param edge edge to be tested
     *
     * @return true if and only if there is an circ arrow between the source and
     *         the target of
     *         edge e
     */
    public boolean isCirc(Edge edge) {
        return edge.getContent() == ArrowRelation.circ;
    }
}
