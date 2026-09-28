package com.oop.model;

import java.util.List;

/**
 * A summary of one person's tickets: how many they hold, how many hours, and how many are late.
 */
public class OwnerWorkload implements Reportable {

    private final String owner;
    private final int ticketCount;
    private final double totalAllocatedHours;
    private final int atRiskCount;

    public OwnerWorkload(String owner, List<Ticket> tickets) {
        double hours = 0.0;
        int atRisk = 0;
        for (Ticket ticket : tickets) {
            hours += ticket.getAllocatedTimeInHours();
            if (ticket.isAtRisk()) {
                atRisk++;
            }
        }

        this.owner = owner;
        this.ticketCount = tickets.size();
        this.totalAllocatedHours = hours;
        this.atRiskCount = atRisk;
    }

    @Override
    public String reportLine() {
        return String.format("%-19s %2d ticket(s)  %6.1fh allocated  %2d at risk",
                owner, ticketCount, totalAllocatedHours, atRiskCount);
    }

    @Override
    public boolean needsAttention() {
        return atRiskCount > 0;
    }
}
