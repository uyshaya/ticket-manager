package com.oop.storage;

import java.io.Serial;

/**
 * Thrown when a line in the ticket file cannot be read as a ticket.
 */
public class TicketDataException extends Exception {

    @Serial
    private static final long serialVersionUID = -654874220494580312L;

    private final int lineNumber;

    public TicketDataException(int lineNumber, String problem) {
        super("line " + lineNumber + ": " + problem);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
