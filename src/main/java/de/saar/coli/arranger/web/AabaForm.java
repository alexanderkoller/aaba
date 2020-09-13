package de.saar.coli.arranger.web;

import io.javalin.http.Context;

public class AabaForm implements AutoCloseable {
    private String input_abc;

    public static AabaForm parse(Context ctx) throws FormValidationException {
        if( ctx.formParam("input_abc") == null ) {
            throw new FormValidationException("input_abc");
        }

        AabaForm ret = new AabaForm();
        ret.input_abc = ctx.formParam("input_abc").trim();
        return ret;
    }

    public String input_abc() {
        return input_abc;
    }

    public void setInput_abc(String input_abc) {
        this.input_abc = input_abc;
    }

    @Override
    public void close() {

    }
}
