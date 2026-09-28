package com.oop.model;

/**
 * Anything that can appear as a line in the report.
 */
public interface Reportable {

    /** Returns a ready-to-print line describing this entry. */
    String reportLine();

    /** Returns true if this entry should be flagged for the reader. */
    boolean needsAttention();
}
