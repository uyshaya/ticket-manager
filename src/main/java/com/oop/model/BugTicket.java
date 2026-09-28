package com.oop.model;

/**
 * A software defect ticket. Its deadline and severity come from the bug level.
 */
public class BugTicket extends Ticket {

    /** The most serious bug level. */
    public static final int MIN_BUG_LEVEL = 1;

    /** The least serious bug level. */
    public static final int MAX_BUG_LEVEL = 4;

    private final int bugLevel;

    /**
     * Builds a bug ticket. The bug level runs from 1 for the worst to 4 for cosmetic; a level
     * outside that range is stored as cosmetic.
     */
    public BugTicket(int code,
                     String owner,
                     String priority,
                     String status,
                     double allocatedTimeInHours,
                     int bugLevel) {
        super(code, owner, priority, status, allocatedTimeInHours);
        this.bugLevel = isValidBugLevel(bugLevel) ? bugLevel : MAX_BUG_LEVEL;
    }

    /**
     * Returns true if the bug level is between {@link #MIN_BUG_LEVEL} and
     * {@link #MAX_BUG_LEVEL}.
     */
    public static boolean isValidBugLevel(int bugLevel) {
        return bugLevel >= MIN_BUG_LEVEL && bugLevel <= MAX_BUG_LEVEL;
    }

    public int getBugLevel() {
        return bugLevel;
    }

    /** Returns eight hours per bug level, so worse bugs get less time. */
    @Override
    public double getResolutionDeadlineInHours() {
        return bugLevel * 8.0;
    }

    /** Returns the severity for the bug level, from "Critical" down to "Cosmetic". */
    @Override
    public String getSeverity() {
        return switch (bugLevel) {
            case 1 -> "Critical";
            case 2 -> "Major";
            case 3 -> "Minor";
            default -> "Cosmetic";
        };
    }
}
