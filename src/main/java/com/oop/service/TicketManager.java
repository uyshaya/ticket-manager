package com.oop.service;

import com.oop.model.OwnerWorkload;
import com.oop.model.Ticket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores the tickets, keyed by ticket code, and answers questions about them. Every list it
 * returns is a copy.
 */
public class TicketManager {

    private final Map<Integer, Ticket> ticketsByCode = new LinkedHashMap<>();

    /**
     * Stores a ticket.
     *
     * @return true if it was stored, false if it was null or its code is already taken
     */
    public boolean add(Ticket ticket) {
        if (ticket == null || ticketsByCode.containsKey(ticket.getCode())) {
            return false;
        }
        ticketsByCode.put(ticket.getCode(), ticket);
        return true;
    }

    /** Returns the ticket with the given code, or null if there is none. */
    public Ticket findByCode(int code) {
        return ticketsByCode.get(code);
    }

    /**
     * Removes the ticket with the given code.
     *
     * @return true if there was a ticket to remove
     */
    public boolean delete(int code) {
        return ticketsByCode.remove(code) != null;
    }

    public int size() {
        return ticketsByCode.size();
    }

    public boolean isEmpty() {
        return ticketsByCode.isEmpty();
    }

    /** Returns every ticket as a new list, in the order they were added. */
    public List<Ticket> allTickets() {
        return new ArrayList<>(ticketsByCode.values());
    }

    /** Returns every ticket as a new list, soonest deadline first. */
    public List<Ticket> ticketsByUrgency() {
        List<Ticket> sorted = allTickets();
        sorted.sort(Comparator.comparingDouble(Ticket::getResolutionDeadlineInHours));
        return sorted;
    }

    /**
     * Returns the tickets matching a search term. The code must match exactly; owner, status,
     * priority, severity and ticket type match on any part of the value and ignore case. An
     * empty or missing term matches nothing.
     */
    public List<Ticket> search(String term) {
        List<Ticket> matches = new ArrayList<>();
        if (term == null || term.isBlank()) {
            return matches;
        }

        String needle = term.trim().toLowerCase();
        for (Ticket ticket : ticketsByCode.values()) {
            if (matches(ticket, needle)) {
                matches.add(ticket);
            }
        }
        return matches;
    }

    // Tests one ticket against a search term that is already trimmed and lower-cased.
    private boolean matches(Ticket ticket, String needle) {
        return String.valueOf(ticket.getCode()).equals(needle)
                || ticket.getOwner().toLowerCase().contains(needle)
                || ticket.getStatus().toLowerCase().contains(needle)
                || ticket.getPriority().toLowerCase().contains(needle)
                || ticket.getSeverity().toLowerCase().contains(needle)
                || ticket.getClass().getSimpleName().toLowerCase().contains(needle);
    }

    /** Returns one summary per owner, sorted by owner name. */
    public List<OwnerWorkload> workloadByOwner() {
        Map<String, List<Ticket>> byOwner = new TreeMap<>();
        for (Ticket ticket : ticketsByCode.values()) {
            byOwner.computeIfAbsent(ticket.getOwner(), key -> new ArrayList<>()).add(ticket);
        }

        List<OwnerWorkload> workloads = new ArrayList<>();
        for (Map.Entry<String, List<Ticket>> group : byOwner.entrySet()) {
            workloads.add(new OwnerWorkload(group.getKey(), group.getValue()));
        }
        return workloads;
    }

    /** Returns how many tickets sit in each status. */
    public Map<String, Integer> countByStatus() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Ticket ticket : ticketsByCode.values()) {
            counts.merge(ticket.getStatus(), 1, Integer::sum);
        }
        return counts;
    }
}
