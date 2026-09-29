package com.oop.storage;

import com.oop.model.BugTicket;
import com.oop.model.MaintenanceTicket;
import com.oop.model.SupportTicket;
import com.oop.model.Ticket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the tickets as a CSV file.
 */
public class TicketFileStore {

    private static final String HEADER = "type,code,owner,priority,status,hours,extra1,extra2";

    // Bug and support rows have one extra column, maintenance rows have two.
    private static final int MIN_COLUMNS = 7;
    private static final int MAINTENANCE_COLUMNS = 8;

    private final File file;
    private final List<String> loadWarnings = new ArrayList<>();

    public TicketFileStore(String fileName) {
        this.file = new File(fileName);
    }

    public String getFileName() {
        return file.getPath();
    }

    public boolean exists() {
        return file.exists();
    }

    /** Returns a message for each line that could not be read during the last load. */
    public List<String> getLoadWarnings() {
        return loadWarnings;
    }

    /**
     * Reads every ticket from the file. A missing file gives an empty list, and any line that
     * cannot be read is skipped and added to the load warnings.
     *
     * @throws IOException if the file is there but cannot be read
     */
    public List<Ticket> load() throws IOException {
        loadWarnings.clear();
        List<Ticket> tickets = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String row = line.trim();
                if (row.isEmpty() || row.equals(HEADER)) {
                    continue;
                }

                try {
                    tickets.add(parseLine(row, lineNumber));
                } catch (TicketDataException e) {
                    loadWarnings.add(e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            // FileReader throws this both for a missing file and for one it may not open.
            // A file that is there but unreadable is a real error.
            if (file.exists()) {
                throw e;
            }
            return new ArrayList<>();
        }

        return tickets;
    }

    /**
     * Writes every ticket to the file, replacing what was there before.
     *
     * @throws IOException if the file cannot be written
     */
    public void save(List<Ticket> tickets) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(HEADER);
            writer.newLine();

            for (Ticket ticket : tickets) {
                writer.write(ticket.toCsvRow());
                writer.newLine();
            }
        }
    }

    /**
     * Turns one CSV line into a ticket.
     *
     * @throws TicketDataException if the line cannot be read as a ticket
     */
    private Ticket parseLine(String line, int lineNumber) throws TicketDataException {
        String[] parts = line.split(",");
        if (parts.length < MIN_COLUMNS) {
            throw new TicketDataException(lineNumber,
                    "expected at least " + MIN_COLUMNS + " columns but found " + parts.length);
        }

        String type = parts[0].trim().toUpperCase();
        int code = readInt(parts[1], "code", lineNumber);
        String owner = parts[2].trim();
        String priority = readFrom(parts[3], Ticket.getValidPriorities(), "priority", lineNumber);
        String status = readFrom(parts[4], Ticket.getValidStatuses(), "status", lineNumber);
        double hours = readDouble(parts[5], "hours", lineNumber);

        if (hours < 0) {
            throw new TicketDataException(lineNumber, "hours cannot be negative");
        }

        switch (type) {
            case "BUG" -> {
                int bugLevel = readInt(parts[6], "bug level", lineNumber);
                if (!BugTicket.isValidBugLevel(bugLevel)) {
                    throw new TicketDataException(lineNumber, "bug level " + bugLevel + " is outside "
                            + BugTicket.MIN_BUG_LEVEL + " to " + BugTicket.MAX_BUG_LEVEL);
                }
                return new BugTicket(code, owner, priority, status, hours, bugLevel);
            }
            case "SUPPORT" -> {
                String tier = readFrom(parts[6], SupportTicket.getValidServiceTiers(),
                        "service tier", lineNumber);
                return new SupportTicket(code, owner, priority, status, hours, tier);
            }
            case "MAINTENANCE" -> {
                if (parts.length < MAINTENANCE_COLUMNS) {
                    throw new TicketDataException(lineNumber,
                            "a maintenance ticket needs " + MAINTENANCE_COLUMNS + " columns");
                }
                int days = readInt(parts[6], "days until window", lineNumber);
                if (days < 0) {
                    throw new TicketDataException(lineNumber, "days until window cannot be negative");
                }
                boolean downtime = readBoolean(parts[7], lineNumber);
                return new MaintenanceTicket(code, owner, priority, status, hours, days, downtime);
            }
            default -> throw new TicketDataException(lineNumber, "unknown ticket type '" + type + "'");
        }
    }

    private int readInt(String value, String field, int lineNumber) throws TicketDataException {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new TicketDataException(lineNumber, field + " '" + value.trim() + "' is not a whole number");
        }
    }

    private double readDouble(String value, String field, int lineNumber) throws TicketDataException {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new TicketDataException(lineNumber, field + " '" + value.trim() + "' is not a number");
        }
    }

    private boolean readBoolean(String value, int lineNumber) throws TicketDataException {
        String trimmed = value.trim();
        if (trimmed.equalsIgnoreCase("true")) {
            return true;
        }
        if (trimmed.equalsIgnoreCase("false")) {
            return false;
        }
        throw new TicketDataException(lineNumber, "requires downtime '" + trimmed + "' must be true or false");
    }

    private String readFrom(String value, Iterable<String> allowed, String field, int lineNumber)
            throws TicketDataException {
        String trimmed = value.trim();
        for (String option : allowed) {
            if (option.equalsIgnoreCase(trimmed)) {
                return option;
            }
        }
        throw new TicketDataException(lineNumber, field + " '" + trimmed + "' is not allowed");
    }
}
