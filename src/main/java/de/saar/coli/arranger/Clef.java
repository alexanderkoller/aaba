package de.saar.coli.arranger;

public class Clef {
    private String name;
    private static final String ALLOWED_CLEFS[] = new String[] { "treble-8", "bass", "treble", "bass+8" };

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
        if( "treble-8".equals(name) ) {
            return "middle=B, clef=treble-8";
        } else if( "bass+8".equals(name)) {
            return "middle=D clef=bass+8";
        } else {
            return "clef=" + name;
        }
    }
}
