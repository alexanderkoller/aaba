package de.saar.coli.arranger;

public class Clef {
    private String name;
    private static final String ALLOWED_CLEFS[] = new String[] { "treble-8", "bass" };

    private static boolean isAllowed(String clefName) {
        for( String x : ALLOWED_CLEFS ) {
            if( x.equals(clefName)) {
                return true;
            }
        }

        return false;
    }

    public Clef(String name) {
        if( ! isAllowed(name) ) {
            throw new RuntimeException("Illegal clef name: " + name);
        }

        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Specification of this clef in ABC notation.
     *
     * @return
     */
    public String getClefSpec() {
        if( "bass".equals(name) ) {
            return "clef=bass";
        } else if( "treble-8".equals(name)) {
            return "middle=B, clef=treble-8";
        }

        // this should never happen because clef names are checked for validity in constructor
        return null;
    }
}
