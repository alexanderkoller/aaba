package de.saar.coli.arranger.web;

/**
 * An exception which signals that the input form on the website
 * could not be validated.
 *
 */
public class FormValidationException extends Exception {
    public FormValidationException(String message) {
        super(message);
    }
}
