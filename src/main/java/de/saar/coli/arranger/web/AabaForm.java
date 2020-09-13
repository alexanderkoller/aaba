package de.saar.coli.arranger.web;

import io.javalin.http.Context;

/**
 * The contents of the web form for the AABA web demo.
 */
public class AabaForm implements AutoCloseable {
    private String input_abc;

    /**
     * Parses the POST parameters in the given context and
     * returns an AabaForm object.
     *
     * @param ctx
     * @return
     * @throws FormValidationException
     */
    public static AabaForm parse(Context ctx) throws FormValidationException {
        if( ctx.formParam("input_abc") == null ) {
            throw new FormValidationException("input_abc");
        }

        AabaForm ret = new AabaForm();
        ret.input_abc = ctx.formParam("input_abc").trim();
        return ret;
    }

    /**
     * Returns the value of the input field.
     *
     * @return
     */
    public String input_abc() {
        return input_abc;
    }

    /**
     * Sets the input field to a new value.
     *
     * @param input_abc
     */
    public void setInput_abc(String input_abc) {
        this.input_abc = input_abc;
    }

    @Override
    public void close() {

    }
}
