package com.oop.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A customer support ticket. Its deadline and severity come from the customer's service tier.
 */
public class SupportTicket extends Ticket {

    private static final Set<String> VALID_SERVICE_TIERS =
            new LinkedHashSet<>(Arrays.asList("Premium", "Standard", "Basic"));

    private final String serviceTier;

    public SupportTicket(int code,
                         String owner,
                         String priority,
                         String status,
                         double allocatedTimeInHours,
                         String serviceTier) {
        super(code, owner, priority, status, allocatedTimeInHours);
        this.serviceTier = serviceTier;
    }

    public static Set<String> getValidServiceTiers() {
        return VALID_SERVICE_TIERS;
    }

    public String getServiceTier() {
        return serviceTier;
    }

    /** Returns the deadline for the tier: 6 hours Premium, 18 Standard, 24 Basic. */
    @Override
    public double getResolutionDeadlineInHours() {
        if ("Premium".equalsIgnoreCase(serviceTier)) {
            return 6.0;
        } else if ("Standard".equalsIgnoreCase(serviceTier)) {
            return 18.0;
        }
        return 24.0;
    }

    @Override
    public String toCsvRow() {
        return csvCommonColumns("SUPPORT") + "," + serviceTier;
    }

    /** Returns "High" for Premium customers and "Normal" for everyone else. */
    @Override
    public String getSeverity() {
        return "Premium".equalsIgnoreCase(serviceTier) ? "High" : "Normal";
    }
}
