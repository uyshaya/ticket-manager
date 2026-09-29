package com.oop.model;

/**
 * A planned maintenance job. Its deadline is the notice period before the maintenance window.
 */
public class MaintenanceTicket extends Ticket {

    private final int daysUntilWindow;
    private final boolean requiresDowntime;

    /** Builds a maintenance ticket. A negative number of days is stored as zero. */
    public MaintenanceTicket(int code,
                             String owner,
                             String priority,
                             String status,
                             double allocatedTimeInHours,
                             int daysUntilWindow,
                             boolean requiresDowntime) {
        super(code, owner, priority, status, allocatedTimeInHours);
        this.daysUntilWindow = Math.max(0, daysUntilWindow);
        this.requiresDowntime = requiresDowntime;
    }

    public int getDaysUntilWindow() {
        return daysUntilWindow;
    }

    public boolean requiresDowntime() {
        return requiresDowntime;
    }

    /** Returns the notice period in working hours, counting eight hours per day. */
    @Override
    public double getResolutionDeadlineInHours() {
        return daysUntilWindow * 8.0;
    }

    @Override
    public String toCsvRow() {
        return csvCommonColumns("MAINTENANCE") + "," + daysUntilWindow + "," + requiresDowntime;
    }

    /** Returns "Planned - Downtime" if the system must go offline, otherwise "Planned". */
    @Override
    public String getSeverity() {
        return requiresDowntime ? "Planned - Downtime" : "Planned";
    }
}
