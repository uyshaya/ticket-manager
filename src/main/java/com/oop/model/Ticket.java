package com.oop.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Base class for every kind of ticket. Holds the details all tickets share; each subclass
 * supplies its own deadline and severity.
 */
public abstract class Ticket implements Reportable {

    private static final Set<String> VALID_STATUSES =
            new LinkedHashSet<>(Arrays.asList("Open", "In-progress", "Closed", "Rejected"));

    private static final Set<String> VALID_PRIORITIES =
            new LinkedHashSet<>(Arrays.asList("High", "Normal", "Low"));

    private final int code;
    private final String owner;
    private final String priority;
    private final double allocatedTimeInHours;

    private String status;

    public Ticket(int code,
                  String owner,
                  String priority,
                  String status,
                  double allocatedTimeInHours) {
        this.code = code;
        this.owner = owner;
        this.priority = priority;
        this.status = status;
        this.allocatedTimeInHours = allocatedTimeInHours;
    }

    public static Set<String> getValidPriorities() {
        return VALID_PRIORITIES;
    }

    public static Set<String> getValidStatuses() {
        return VALID_STATUSES;
    }

    public int getCode() {
        return code;
    }

    public String getOwner() {
        return owner;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public double getAllocatedTimeInHours() {
        return allocatedTimeInHours;
    }

    /**
     * Changes the status, accepting only a value from the allowed list.
     *
     * @return true if the status was changed, false if the value was refused
     */
    public boolean setStatus(String status) {
        if (status == null || !VALID_STATUSES.contains(status)) {
            return false;
        }
        this.status = status;
        return true;
    }

    /** Returns how many hours the ticket may stay open before it is late. */
    public abstract double getResolutionDeadlineInHours();

    /** Returns the severity label, such as "Critical". */
    public abstract String getSeverity();

    /** Returns the ticket as one CSV row. */
    public abstract String toCsvRow();

    /** Returns the leading CSV columns shared by every ticket type. */
    protected String csvCommonColumns(String type) {
        return type + "," + code + "," + owner + "," + priority + "," + status + ","
                + allocatedTimeInHours;
    }

    /** Returns true when the allocated time is already past the deadline. */
    public boolean isAtRisk() {
        return allocatedTimeInHours > getResolutionDeadlineInHours();
    }

    @Override
    public String reportLine() {
        return String.format("#%-5d %-19s %-10s %-12s %-7s %-19s %5.1fh / %5.1fh",
                code,
                getClass().getSimpleName(),
                owner,
                status,
                priority,
                getSeverity(),
                allocatedTimeInHours,
                getResolutionDeadlineInHours());
    }

    @Override
    public boolean needsAttention() {
        return isAtRisk();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{code=" + code +
                ", owner='" + owner + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", allocatedTimeInHours=" + allocatedTimeInHours +
                '}';
    }
}
