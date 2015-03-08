package org.thegalactic.lattice;

/*
 * ConceptLattice.java
 *
 * Copyright: 2010-2014 Karell Bertet, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices.
 * You can redistribute it and/or modify it under the terms of the CeCILL-B license.
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.thegalactic.dgraph.DAGraph;
import org.thegalactic.dgraph.DGraph;
import org.thegalactic.dgraph.Edge;
import org.thegalactic.dgraph.Node;
import org.thegalactic.lattice.io.ConceptLatticeWriter;
import org.thegalactic.lattice.io.ConceptLatticeWriterDot;
import org.thegalactic.lattice.io.ConceptLatticeWriterFactory;
import org.thegalactic.util.ComparableSet;

/**
 * This class extends class {@link Lattice} to provide specific methods
 * to manipulate both a concept lattice or a closed set lattice.
 *
 * This class provides methods implementing classical operation on a concept lattice:
 * join and meet reduction, concepts sets reduction, ...
 *
 * This class also provides two static method generating a concept lattice:
 * methods {@link #diagramLattice} and {@link #completeLattice} both computes
 * the closed set lattice of a given closure system.
 * The firt one computes the hasse diagram of the closed set lattice
 * by invoking method {@link #immediateSuccessors}. This method implements an
 * adaptation of the well-known Bordat algorithm that also
 * computes the dependance graph of the lattice where at once the minimal generators and the canonical
 * direct basis of the lattice are encoded.
 * The second static method computes the transitively closure of the lattice
 * as the inclusion relation defined on all the closures
 * generated by method {@link ClosureSystem#allClosures} that implements
 * the well-known Wille algorithm.
 *
 * ![ConceptLattice](ConceptLattice.png)
 *
 * @uml ConceptLattice.png
 * !include resources/org/thegalactic/dgraph/DAGraph.iuml
 * !include resources/org/thegalactic/dgraph/DGraph.iuml
 * !include resources/org/thegalactic/dgraph/Edge.iuml
 * !include resources/org/thegalactic/dgraph/Node.iuml
 * !include resources/org/thegalactic/lattice/Lattice.iuml
 * !include resources/org/thegalactic/lattice/ConceptLattice.iuml
 * !include resources/org/thegalactic/lattice/Concept.iuml
 *
 * hide members
 * show ConceptLattice members
 * class ConceptLattice #LightCyan
 * title ConceptLattice UML graph
 */
public class ConceptLattice extends Lattice {
    /*
     * Register dot writer
     */

    static {
        if (ConceptLatticeWriterFactory.get("dot") == null) {
            ConceptLatticeWriterDot.register();
        }
    }

    /**
     * Generate the lattice composed of all the antichains of this component
     * ordered with the inclusion relation.
     *
     * This treatment is performed in O(??) by implementation of Nourine algorithm
     * that consists in a sequence of doubling intervals of nodes.
     *
     * @param dag a directed acyclic graph
     *
     * @return the concept lattice
     */
    public static ConceptLattice idealLattice(DAGraph dag) {
        if (!dag.isAcyclic()) {
            return null;
        }
        // initialise the poset of ideals with the emptyset
        ConceptLattice conceptLattice = new ConceptLattice();
        int id = 1;
        conceptLattice.addNode(new Concept(true, false));
        // travel on graph according to a topological sort
        DAGraph graph = new DAGraph(dag);
        graph.transitiveClosure();
        // treatment of nodes according to a topological sort
        ArrayList<Node> sort = graph.topologicalSort();
        for (Node x : sort) {
            // computation of Jx
            TreeSet<Node> jxmoins = new TreeSet<Node>(graph.getPredecessorNodes(x));
            // storage of new ideals in a set
            TreeSet<Concept> toAdd = new TreeSet<Concept>();
            for (Node j1 : conceptLattice.getNodes()) {
                if (((Concept) j1).containsAllInA(jxmoins)) {
                    Concept newJ = new Concept(true, false);
                    newJ.addAllToA(((TreeSet) ((Concept) j1).getSetA()));
                    newJ.addToA(x);
                    toAdd.add(newJ);
                }
            }
            // addition of the new ideals in conceptLattice
            for (Concept newJ : toAdd) {
                conceptLattice.addNode(newJ);
            }
        }
        // computation of the inclusion relaton
        for (Node node1 : conceptLattice.getNodes()) {
            for (Node node2 : conceptLattice.getNodes()) {
                if (((Concept) node1).containsAllInA(((Concept) node2).getSetA())) {
                    conceptLattice.addEdge(node2, node1);
                }
            }
        }
        conceptLattice.transitiveReduction();
        return conceptLattice;
    }

    /**
     * Generates and returns the complete (i.e. transitively closed) closed set lattice of the
     * specified closure system, that can be an implicational system (ImplicationalSystem) or a context.
     *
     * The lattice is generated using the well-known Next Closure algorithm.
     * All closures are first generated using the method:
     * {@link ClosureSystem#allClosures}
     * that implements the well-known Next Closure algorithm.
     * Then, all concepts are ordered by inclusion.
     *
     * @param init a closure system (an ImplicationalSystem or a Context)
     *
     * @return a concept lattice
     */
    public static ConceptLattice completeLattice(ClosureSystem init) {
        ConceptLattice lattice = new ConceptLattice();
        // compute all the closed set with allClosures
        Vector<Concept> allclosure = init.allClosures();
        for (Concept cl : allclosure) {
            lattice.addNode(cl);
        }

        // an edge corresponds to an inclusion between two closed sets
        for (Node from : lattice.getNodes()) {
            for (Node to : lattice.getNodes()) {
                if (((Concept) to).containsAllInA(((Concept) from).getSetA())) {
                    lattice.addEdge(from, to);
                }
            }
        }
        // Hasse diagram is computed
        return lattice;
    }

    /**
     * Generates and returns the Hasse diagram of the closed set lattice of the
     * specified closure system, that can be an implicational system (ImplicationalSystem) or a context.
     *
     * The Hasse diagramm of the closed set lattice is
     * obtained by a recursively generation of immediate successors of a given closed set,
     * starting from the botom closed set. Implemented algorithm is an adaptation of Bordat's
     * algorithm where the dependance graph is computed while the lattice is generated.
     * This treatment is performed in O(cCl|S|^3log g) where S is the initial set of elements,
     * c is the number of closed sets that could be exponential in the worst case,
     * Cl is the closure computation complexity
     * and g is the number of minimal generators of the lattice.
     *
     * The dependance graph of the lattice is also computed while the lattice generation.
     * The dependance graph of a lattice encodes at once the minimal generators
     * and the canonical direct basis of the lattice .
     *
     * @param init a closure system (an ImplicationalSystem or a Context)
     *
     * @return a concept lattice
     */
    public static ConceptLattice diagramLattice(ClosureSystem init) {
        ConceptLattice lattice = new ConceptLattice();
        //if (Diagram) {
        // computes the dependance graph of the closure system
        // addition of nodes in the precedence graph
        DGraph graph = new DGraph();
        for (Comparable c : init.getSet()) {
            graph.addNode(new Node(c));
        }
        lattice.setDependencyGraph(graph);
        // intialize the close set lattice with botom element
        Concept bot = new Concept(init.closure(new ComparableSet()), false);
        lattice.addNode(bot);
        // recursive genaration from the botom element with diagramLattice
        lattice.recursiveDiagramLattice(bot, init);
        // minimalisation of edge's content to get only inclusion-minimal valuation for each edge
        /**
         * for (Edge ed : lattice.dependanceGraph.getEdges()) {
         * TreeSet<ComparableSet> valEd = new TreeSet<ComparableSet>(((TreeSet<ComparableSet>)ed.getContent()));
         * for (ComparableSet X1 : valEd)
         * for (ComparableSet X2 : valEd)
         * if (X1.containsAll(X2) && !X2.containsAll(X1))
         * ((TreeSet<ComparableSet>)ed.getContent()).remove(X1);
         * }*
         */
        return lattice;
    }

    /*
     * ------------- CONSTRUCTORS ------------------
     */
    /**
     * Constructs this component with an empty set of nodes.
     */
    public ConceptLattice() {
        super();
    }

    /**
     * Constructs this component with the specified set of concepts,
     * and empty treemap of successors and predecessors.
     *
     * @param set the set of nodes
     */
    public ConceptLattice(TreeSet<Concept> set) {
        super((TreeSet) set);
    }

    /**
     * Constructs this component as a shallow copy of the specified lattice.
     *
     * Concept lattice property is checked for the specified lattice.
     * When not verified, this component is constructed with an empty set of nodes.
     *
     * @param lattice the lattice to be copied
     */
    public ConceptLattice(Lattice lattice) {
        super(lattice);
        if (!this.isConceptLattice()) {
            this.setNodes(new TreeSet<Node>());
            this.setSuccessors(new TreeMap<Node, TreeSet<Edge>>());
            this.setPredecessors(new TreeMap<Node, TreeSet<Edge>>());
        }
    }

    /*
     * ------------- OVERLAPPING METHODS ------------------
     */
    /**
     * Adds the specified node to the set of node of this component.
     *
     * In the case where content of this node is not a concept,
     * the node will not be added
     *
     * @param n a node
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean addNode(Node n) {
        if (n instanceof Concept) {
            return super.addNode(n);
        } else {
            return false;
        }
    }

    /**
     * Adds the specified edge to this component:
     * `to` is added as a successor of `from`.
     *
     * If the cases where specified nodes don't belongs to the node set,
     * and where nodes don't contains concept as content,
     * then the edge will not be added.
     *
     * @param from the node origine of the edge
     * @param to   the node destination of the edge
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean addEdge(Node from, Node to) {
        if ((to instanceof Concept) && (from instanceof Concept)) {
            return super.addEdge(from, to);
        } else {
            return false;
        }
    }

    /*
     * ------------- CONCEPT LATTICE CHEKING METHOD ------------------
     */
    /**
     * Check if nodes of this component are concepts.
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean containsConcepts() {
        for (Node n : this.getNodes()) {
            if (!(n instanceof Concept)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if this component is a lattice whose nodes are concepts.
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean isConceptLattice() {
        if (!this.isLattice()) {
            return false;
        }
        if (!this.containsConcepts()) {
            return false;
        }
        return true;
    }

    /**
     * Check if this component is a lattice whose nodes are concepts with non null set A.
     *
     * @return a boolean
     *
     * @todo Comment the return: conception
     */
    public boolean containsAllSetA() {
        if (!this.containsConcepts()) {
            return false;
        }
        for (Node n : this.getNodes()) {
            if (!((Concept) n).hasSetA()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if this component is a lattice whose nodes are concepts with non null set A.
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean containsAllSetB() {
        if (!this.containsConcepts()) {
            return false;
        }
        for (Node n : this.getNodes()) {
            if (!((Concept) n).hasSetB()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a clone of this component composed of a clone of each concept and each edge.
     *
     * @return a concept lattice
     */
    @Override
    public ConceptLattice clone() {
        ConceptLattice conceptLattice = new ConceptLattice();
        TreeMap<Concept, Concept> copy = new TreeMap<Concept, Concept>();
        for (Node n : this.getNodes()) {
            Concept c = (Concept) n;
            Concept c2 = c.clone();
            copy.put(c, c2);
            conceptLattice.addNode(c2);
        }
        for (Edge ed : this.getEdges()) {
            conceptLattice.addEdge(new Edge(copy.get(ed.getFrom()), copy.get(ed.getTo()), ed.getContent()));
        }
        return conceptLattice;
    }

    /*
     * ------------- SET A AND SET B HANDLING METHOD ------------------
     */
    /**
     * Returns concept defined by setA and setB; null if not found.
     *
     * @param setA intent of the concept to find
     * @param setB extent of the concept to find
     *
     * @return concept defined by setA and setB; null if not found.
     */
    public Concept getConcept(ComparableSet setA, ComparableSet setB) {
        SortedSet<Node> setNodes = this.getNodes();
        Concept cpt = null;
        for (Node n : setNodes) {
            if ((setA.equals(((Concept) n).getSetA())) && (setB.equals(((Concept) n).getSetB()))) {
                cpt = (Concept) n;
            }
        }
        return cpt;
    }

    /**
     * Replace set A in each concept of the lattice with the null value.
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean removeAllSetA() {
        if (!this.containsConcepts()) {
            return false;
        }
        for (Node n : this.getNodes()) {
            Concept c = (Concept) n;
            c.putSetA(null);
        }
        return true;
    }

    /**
     * Replace set B in each concept of the lattice with the null value.
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean removeAllSetB() {
        if (!this.containsConcepts()) {
            return false;
        }
        for (Node n : this.getNodes()) {
            Concept c = (Concept) n;
            c.putSetB(null);
        }
        return true;
    }

    /**
     * Replace null set A in each join irreducible concept with a set containing node ident.
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean initialiseSetAForJoin() {
        if (!this.containsConcepts()) {
            return false;
        }
        TreeSet<Node> joinIrr = this.joinIrreducibles();
        for (Node n : this.getNodes()) {
            Concept c = (Concept) n;
            if (!c.hasSetA() && joinIrr.contains(c)) {
                ComparableSet setX = new ComparableSet();
                setX.add(new Integer(c.getIdentifier()));
                c.putSetA(setX);
            }
        }
        return true;
    }

    /**
     * Replace null set B in each meet irreducible concept with a set containing node ident.
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean initialiseSetBForMeet() {
        if (!this.containsConcepts()) {
            return false;
        }
        TreeSet<Node> meetIrr = this.meetIrreducibles();
        for (Node n : this.getNodes()) {
            Concept c = (Concept) n;
            if (!c.hasSetB() && meetIrr.contains(c)) {
                ComparableSet setX = new ComparableSet();
                setX.add(new Integer(c.getIdentifier()));
                c.putSetB(setX);
            }
        }
        return true;
    }

    /*
     * --------------- INCLUSION REDUCTION METHODS ------------
     */
    /**
     * Replaces, if not empty, set A of each concept with the difference between itself
     * and set A of its predecessors;
     * Then replaces, if not empty, set B of each concept by
     * the difference between itself and set B of its successors.
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean makeInclusionReduction() {
        if (!this.containsConcepts()) {
            return false;
        }
        boolean setA = this.containsAllSetA();
        boolean setB = this.containsAllSetB();
        if (!setA && !setB) {
            return false;
        }
        // makes setA inclusion reduction
        if (setA) {
            // computation of an inverse topological sort
            this.transpose();
            ArrayList<Node> sort = this.topologicalSort();
            this.transpose();
            // reduction of set A
            for (Node to : sort) {
                Concept cto = (Concept) to;
                for (Node from : this.getPredecessorNodes(to)) {
                    Concept cfrom = (Concept) from;
                    cto.getSetA().removeAll(cfrom.getSetA());
                }
            }
        }
        // makes setB inclusion reduction
        if (setB) {
            // computation of a topological sort
            ArrayList<Node> sort = this.topologicalSort();
            // reduction of set B
            for (Node to : sort) {
                Concept cto = (Concept) to;
                for (Node from : this.getSuccessorNodes(to)) {
                    Concept cfrom = (Concept) from;
                    cto.getSetB().removeAll(cfrom.getSetB());
                }
            }
        }
        return true;
    }

    /**
     * Replaces set A of each join irreducible node by
     * the difference between itself and set A of the unique predecessor.
     *
     * Others closed sets are replaced by an emptyset.
     *
     * @return a boolean
     *
     * @todo Comment the return
     */
    public boolean makeIrreduciblesReduction() {
        // make inclusion reduction
        if (this.makeInclusionReduction()) {
            // check if not set A reduced concepts are join irreducibles
            // and if not set B reduced concepts are meet irreducibles
            TreeSet<Node> joinIrr = this.joinIrreducibles();
            TreeSet<Node> meetIrr = this.meetIrreducibles();
            for (Node n : this.getNodes()) {
                Concept c = (Concept) n;
                if (c.hasSetA() && !c.getSetA().isEmpty() && !joinIrr.contains(c)) {
                    c.putSetA(new ComparableSet());
                }
                if (c.hasSetB() && !c.getSetB().isEmpty() && !meetIrr.contains(c)) {
                    c.putSetB(new ComparableSet());
                }
            }
        }
        return true;
    }

    /**
     * Returns a lattice where edges are valuated by the difference between
     * set A of two adjacent concepts.
     *
     * @return a boolean
     *
     * @todo Change comment
     */
    public boolean makeEdgeValuation() {
        if (!this.containsConcepts()) {
            return false;
        }
        for (Node n1 : this.getNodes()) {
            for (Edge ed : this.getSuccessorEdges(n1)) {
                if (!ed.hasContent()) {
                    Node n2 = ed.getTo();
                    TreeSet diff = new TreeSet();
                    diff.addAll(((Concept) n2).getSetA());
                    diff.removeAll(((Concept) n1).getSetA());
                    ed.setContent(diff);
                }
            }
        }
        return true;
    }

    /*
     * --------------- LATTICE GENERATION METHODS ------------
     */
    /**
     * Returns a lattice where join irreducibles node's content
     * is replaced by the first element of set A.
     *
     * Other nodes are replaced by a new comparable.
     *
     * @return a lattice
     */
    public Lattice getJoinReduction() {
        if (!this.containsConcepts()) {
            return null;
        }
        if (!this.containsAllSetA()) {
            return null;
        }
        Lattice lattice = new Lattice();
        //ConceptLattice csl = new ConceptLattice (this);
        ConceptLattice csl = this.clone();
        csl.makeIrreduciblesReduction();
        TreeSet<Node> joinIrr = csl.joinIrreducibles();
        // addition to lattice of a comparable issued from each reduced closed set
        TreeMap<Node, Node> reduced = new TreeMap<Node, Node>();
        for (Node n : csl.getNodes()) {
            Concept c = (Concept) n;
            Node nred;
            if (c.hasSetA() && joinIrr.contains(n)) {
                nred = new Node(c.getSetA().first());
            } else {
                nred = new Node();
            }
            reduced.put(n, nred);
        }
        // addtion of nodes to lattice
        for (Node n : csl.getNodes()) {
            lattice.addNode(reduced.get(n));
        }
        // addtion of edges to lattice
        for (Node from : csl.getNodes()) {
            for (Node to : csl.getSuccessorNodes(from)) {
                lattice.addEdge(reduced.get(from), reduced.get(to));
            }
        }
        return lattice;
    }

    /**
     * Returns a lattice where meet irreducibles node's content
     * is replaced by the first element of set B.
     *
     * Other nodes are replaced by a new comparable.
     *
     * @return a lattice
     */
    public Lattice getMeetReduction() {
        if (!this.containsConcepts()) {
            return null;
        }
        if (!this.containsAllSetB()) {
            return null;
        }
        Lattice lattice = new Lattice();
        if (!this.containsConcepts()) {
            return lattice;
        }
        //ConceptLattice csl = new ConceptLattice (this);
        ConceptLattice csl = this.clone();
        csl.makeIrreduciblesReduction();
        TreeSet<Node> meetIrr = csl.meetIrreducibles();
        // addition to lattice of a comparable issued from each reduced closed set
        TreeMap<Node, Node> reduced = new TreeMap<Node, Node>();
        for (Node n : csl.getNodes()) {
            Concept c = (Concept) n;
            Node nred;
            if (c.hasSetB() && meetIrr.contains(n)) {
                nred = new Node(c.getSetB().first());
            } else {
                nred = new Node();
            }
            reduced.put(n, nred);
        }
        for (Node n : csl.getNodes()) {
            lattice.addNode(reduced.get(n));
        }
        // addtion of edges to lattice
        for (Node from : csl.getNodes()) {
            for (Node to : csl.getSuccessorNodes(from)) {
                lattice.addEdge(reduced.get(from), reduced.get(to));
            }
        }
        return lattice;
    }

    /**
     * Returns a lattice where each join irreducible concept
     * is replaced by a node containing the first element of set A,
     * and each meet irreducible concept is replaced by a node contining the first element of set B.
     *
     * A concept that is at once join and meet irreducible is replaced by
     * a node containing the first element of set A and the first element of set B in a set.
     * Other nodes are replaced by an empty node.
     *
     * @return a lattice
     */
    public Lattice getIrreduciblesReduction() {
        Lattice lattice = new Lattice();
        if (!this.containsConcepts()) {
            return lattice;
        }
        //ConceptLattice csl = new ConceptLattice (this);
        ConceptLattice csl = this.clone();
        csl.makeIrreduciblesReduction();
        TreeSet<Node> joinIrr = csl.joinIrreducibles();
        TreeSet<Node> meetIrr = csl.meetIrreducibles();
        // addition to lattice of a comparable issued from each reduced closed set
        TreeMap<Node, Node> reduced = new TreeMap<Node, Node>();
        for (Node n : csl.getNodes()) {
            Concept c = (Concept) n;
            // create a new Node with two indexed elements: the first of set A and the first of set B
            if (c.hasSetA() && c.hasSetB() && meetIrr.contains(c) && joinIrr.contains(c)) {
                TreeSet<Comparable> content = new TreeSet<Comparable>();
                content.add(c.getSetA().first());
                content.add(c.getSetB().first());
                Node nred = new Node(content);
                reduced.put(n, nred);
            } else if (c.hasSetA() && joinIrr.contains(n)) {
                // create a new Node with the first element of set A
                Node nred = new Node(((Concept) n).getSetA().first());
                reduced.put(n, nred);
            } else if (c.hasSetB() && meetIrr.contains(n)) {
                // create a new Node with the first element of set A
                Node nred = new Node(((Concept) n).getSetB().first());
                reduced.put(n, nred);
            } else {
                reduced.put(n, new Node());
            }
        }
        // addtion of nodes to lattice
        for (Node n : csl.getNodes()) {
            lattice.addNode(reduced.get(n));
        }
        // addtion of edges to lattice
        for (Node from : csl.getNodes()) {
            for (Node to : csl.getSuccessorNodes(from)) {
                lattice.addEdge(reduced.get(from), reduced.get(to));
            }
        }
        return lattice;
    }

    /**
     * Returns iceberg lattice whose concept contains enough observations.
     *
     * Are kept only concept whose number of observation is over threshold.
     * A top node is added to keep the lattice structure.
     *
     * @param threshold used to determine nodes to be kept.
     *
     * @return iceberg lattice whose concept contains enough observations.
     */
    public ConceptLattice iceberg(float threshold) {
        ConceptLattice l = new ConceptLattice();
        Concept b = (Concept) this.bottom();
        int card = b.getSetB().size();
        for (Node n : this.getNodes()) {
            Concept cpt = (Concept) n;
            if ((float) cpt.getSetB().size() / (float) card >= threshold) {
                l.addNode(n);
            }
        }
        for (Node f : l.getNodes()) {
            for (Node t : l.getNodes()) {
                if (this.containsEdge(f, t)) {
                    l.addEdge(f, t);
                }
            }
        }
        Node t = this.top();
        l.addNode(t);
        for (Node n : l.getWells()) {
            if (!n.equals(t)) {
                l.addEdge(n, t);
            }
        }
        return l;
    }
    /*
     * -------- STATIC CLOSEDSET LATTICE GENERATION FROM AN ImplicationalSystem OR A CONTEXT ------------------
     */

    /**
     * Returns the Hasse diagramme of the closed set lattice of the specified closure system
     * issued from the specified concept.
     *
     * Immediate successors generation is an adaptation of Bordat's theorem
     * stating that there is a bijection
     * between minimal strongly connected component of the precedence subgraph issued
     * from the specified node, and its immediate successors.
     *
     * This treatment is performed in O(cCl|S|^3log g) where S is the initial set of elements,
     * c is the number of closed sets that could be exponential in the worst case,
     * Cl is the closure computation complexity
     * and g is the number of minimal generators of the lattice.
     *
     * @param n    a concept
     * @param init a closure system
     */
    public void recursiveDiagramLattice(Concept n, ClosureSystem init) {
        Vector<TreeSet<Comparable>> immSucc = this.immediateSuccessors(n, init);
        for (TreeSet<Comparable> setX : immSucc) {
            Concept c = new Concept(new TreeSet(setX), false);
            Concept ns = (Concept) this.getNode(c);
            if (ns != null) {
                // when ns already exists, addition of a new edge
                this.addEdge(n, ns);
            } else { // when ns don't already exists, addition of a new node and recursive treatment
                this.addNode(c);
                this.addEdge(n, c);
                this.recursiveDiagramLattice(c, init);
            }
        }
    }

    /**
     * Returns the list of immediate successors of a given node of the lattice.
     *
     * This treatment is an adaptation of Bordat's theorem stating that there is a bijection
     * between minimal strongly connected component of the precedence subgraph issued
     * from the specified node, and its immediate successors.
     *
     * This treatment is performed in O(Cl|S|^3log g) where S is the initial set of elements,
     * Cl is the closure computation complexity
     * and g is the number of minimal generators of the lattice.
     *
     * This treatment is recursively invoked by method recursiveDiagramlattice. In this case, the dependance graph
     * is initialised by method recursiveDiagramMethod, and updated by this method,
     * with addition some news edges and/or new valuations on existing edges.
     * When this treatment is not invoked by method recursiveDiagramLattice, then the dependance graph
     * is initialised, but it may be not complete. It is the case for example for on-line generation of the
     * concept lattice.
     *
     * @param n    a node
     * @param init a closure system
     *
     * @return a set of immediate successors
     */
    public Vector<TreeSet<Comparable>> immediateSuccessors(Node n, ClosureSystem init) {
        // Initialisation of the dependance graph when not initialised by method recursiveDiagramLattice
        if (!this.hasDependencyGraph()) {
            DGraph graph = new DGraph();
            for (Comparable c : init.getSet()) {
                graph.addNode(new Node(c));
            }
            this.setDependencyGraph(graph);
        }
        // computes newVal, the subset to be used to valuate every new dependance relation
        // newVal = F\predecessors of F in the precedence graph of the closure system
        // For a non reduced closure system, the precedence graph is not acyclic,
        // and therefore strongly connected components have to be used.
        ComparableSet setF = new ComparableSet(((Concept) n).getSetA());
        DGraph prec = init.precedenceGraph();
        DAGraph acyclPrec = prec.getStronglyConnectedComponent();
        ComparableSet newVal = new ComparableSet();
        newVal.addAll(setF);
        for (Object x : setF) {
            // computes nx, the strongly connected component containing x
            Node nx = null;
            for (Node cc : acyclPrec.getNodes()) {
                TreeSet<Node> setCC = (TreeSet<Node>) cc.getContent();
                for (Node y : setCC) {
                    if (x.equals(y.getContent())) {
                        nx = cc;
                    }
                }
            }
            // computes the minorants of nx in the acyclic graph
            SortedSet<Node> ccMinNx = acyclPrec.minorants(nx);
            // removes from newVal every minorants of nx
            for (Node cc : ccMinNx) {
                TreeSet<Node> setCC = (TreeSet<Node>) cc.getContent();
                for (Node y : setCC) {
                    newVal.remove(y.getContent());
                }
            }
        }
        // computes the node belonging in S\F
        TreeSet<Node> nodes = new TreeSet<Node>();
        for (Node in : this.getDependencyGraph().getNodes()) {
            if (!setF.contains(in.getContent())) {
                nodes.add(in);
            }
        }
        // computes the dependance relation between nodes in S\F
        // and valuated this relation by the subset of S\F
        TreeSet<Edge> edges = new TreeSet<Edge>();
        for (Node from : nodes) {
            for (Node to : nodes) {
                if (!from.equals(to)) {
                    // check if from is in dependance relation with to
                    // i.e. "from" belongs to the closure of "F+to"
                    ComparableSet fPlusTo = new ComparableSet(setF);
                    fPlusTo.add(to.getContent());
                    fPlusTo = new ComparableSet(init.closure(fPlusTo));
                    if (fPlusTo.contains(from.getContent())) {
                        // there is a dependance relation between from and to
                        // search for an existing edge between from and to
                        Edge ed = this.getDependencyGraph().getEdge(from, to);
                        if (ed == null) {
                            ed = new Edge(from, to, new TreeSet<ComparableSet>());
                            this.getDependencyGraph().addEdge(ed);
                        }
                        edges.add(ed);
                        // check if F is a minimal set closed for dependance relation between from and to
                        ((TreeSet<ComparableSet>) ed.getContent()).add(newVal);
                        TreeSet<ComparableSet> valEd = new TreeSet<ComparableSet>((TreeSet<ComparableSet>) ed.getContent());
                        for (ComparableSet x1 : valEd) {
                            if (x1.containsAll(newVal) && !newVal.containsAll(x1)) {
                                ((TreeSet<ComparableSet>) ed.getContent()).remove(x1);
                            }
                            if (!x1.containsAll(newVal) && newVal.containsAll(x1)) {
                                ((TreeSet<ComparableSet>) ed.getContent()).remove(newVal);
                            }
                        }
                    }
                }
            }
        }
        // computes the dependance subgraph of the closed set F as the reduction
        // of the dependance graph composed of nodes in S\A and edges of the dependance relation
        DGraph sub = this.getDependencyGraph().getSubgraphByNodes(nodes);
        DGraph delta = sub.getSubgraphByEdges(edges);
        // computes the sources of the CFC of the dependance subgraph
        // that corresponds to successors of the closed set F
        DAGraph cfc = delta.getStronglyConnectedComponent();
        SortedSet<Node> sccMin = cfc.getSinks();
        Vector<TreeSet<Comparable>> immSucc = new Vector<TreeSet<Comparable>>();
        for (Node n1 : sccMin) {
            TreeSet s = new TreeSet(setF);
            TreeSet<Node> toadd = (TreeSet<Node>) n1.getContent();
            for (Node n2 : toadd) {
                s.add(n2.getContent());
            }
            immSucc.add(s);
        }
        return immSucc;
    }

    /**
     * Save the description of this component in a file whose name is specified.
     *
     * @param filename the name of the file
     *
     * @throws IOException When an IOException occurs
     */
    public void save(final String filename) throws IOException {
        String extension = "";
        int index = filename.lastIndexOf('.');
        if (index > 0) {
            extension = filename.substring(index + 1);
        }
        ConceptLatticeWriter writer = ConceptLatticeWriterFactory.get(extension);
        if (writer == null) {
            super.save(filename);
        } else {
            BufferedWriter file = new BufferedWriter(new FileWriter(filename));
            writer.write(this, file);
            file.close();
        }
    }
}
