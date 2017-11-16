package org.thegalactic.descriptionset;

import org.thegalactic.lattice.ConceptLattice;

/**
 * This class tests methods handling description sets.
 *
 * @author Jessie Carbonnel
 *
 */
public final class TestDescriptionSet {

    /**
     * Constructor.
     */
     private TestDescriptionSet() {
     }

    /**
     * Main.
     *
     * @param args main's arguments
     */
    public static void main(String[] args) {

        try {

                // Creating description set context
                // from an example file
                // representing attributeSet descriptions

            String filename = "src/examples/descriptionSet/attributeSet.txt";

            DescriptionSetContext<AttributeDescription> context = new DescriptionSetContext<AttributeDescription>(filename);

                // PRINTING OBSERVATIONS

            context.printObservations();

                // PRINTING INITIAL DESCRIPTION SETS

            context.printInitialDescriptionSets();

                // PRINTING ALL DESCRIPTION SETS

            context.printAllDescriptionSets();

                // PRINTING MORE SPECIALISED INTENT

            System.out.println();
            for (String o : context.getObservations()) {
                System.out.println("SpecInt(" + o + ") = " + context.getSpecIntent(o));
            }

                // PRINTING INTENT

            System.out.println();
            for (String o : context.getObservations()) {
                System.out.println("Int(" + o + ") = " + context.getIntent(o));
            }

                // PRINTING EXTENT

            System.out.println();
            for (ComparableDescriptionSet ds : context.getAllDescriptionSets()) {
                System.out.println("Ext(" + ds + ") = " + context.getExtent(ds));
            }

                // COMPUTING AND PRINTING ALL CLOSURES

            System.out.println();
            System.out.println("--------- ALL CLOSURES ---------");
            System.out.println();

            System.out.println(context.allDescriptionClosures());

            ConceptLattice cl = ConceptLattice.completeDescriptionLatticeLattice(context);
            System.out.println(cl);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}