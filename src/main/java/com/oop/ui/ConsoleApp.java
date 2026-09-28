package com.oop.ui;

import com.oop.model.BugTicket;
import com.oop.model.MaintenanceTicket;
import com.oop.model.Reportable;
import com.oop.model.SupportTicket;
import com.oop.model.Ticket;
import com.oop.service.TicketManager;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * The menu the user sees. Reads input, asks {@link TicketManager} to do the work, and prints
 * the results.
 */
public class ConsoleApp {

    private static final int LINE_WIDTH = 86;

    private final TicketManager manager = new TicketManager();

    private final Scanner scanner;

    public ConsoleApp(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Loads the sample tickets, then shows the menu until the user exits or input runs out. */
    public void run() {
        seedSampleTickets();
        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Choose an option: ");
            if (choice == null) {
                break;
            }

            switch (choice) {
                case "1" -> addTicket();
                case "2" -> viewReport();
                case "3" -> searchTickets();
                case "4" -> deleteTicket();
                case "5" -> running = false;
                default -> System.out.println("  Unknown option. Please pick 1 to 5.");
            }
        }

        System.out.println();
        System.out.println("Goodbye.");
    }

    /** Asks for a new ticket's details and stores it. */
    private void addTicket() {
        printHeading("ADD TICKET");
        System.out.println("  1) Bug          2) Support          3) Maintenance");

        String type = readLine("  Ticket type (1-3): ");
        if (type == null || !List.of("1", "2", "3").contains(type)) {
            System.out.println("  Cancelled: that is not a ticket type.");
            return;
        }

        Integer code = readInt("  Ticket code: ");
        if (code == null) {
            return;
        }
        if (manager.findByCode(code) != null) {
            System.out.println("  Cancelled: ticket #" + code + " already exists.");
            return;
        }

        String owner = readLine("  Owner: ");
        if (owner == null) {
            return;
        }

        String priority = readFromSet("  Priority", Ticket.getValidPriorities());
        if (priority == null) {
            return;
        }

        Double hours = readDouble("  Allocated time in hours: ");
        if (hours == null) {
            return;
        }

        Ticket ticket = createTicket(type, code, owner, priority, hours);
        if (ticket == null) {
            return;
        }

        if (manager.add(ticket)) {
            System.out.println();
            System.out.println("  Added: " + ticket.reportLine().trim());
        } else {
            System.out.println("  Could not add the ticket.");
        }
    }

    /**
     * Builds the ticket type the user chose, asking for the one extra field that type needs.
     *
     * @param type the menu choice: "1" for bug, "2" for support, anything else for maintenance
     * @return the new ticket, or null if the input ran out
     */
    private Ticket createTicket(String type, int code, String owner, String priority, double hours) {
        switch (type) {
            case "1" -> {
                Integer level = readInt("  Bug level (" + BugTicket.MIN_BUG_LEVEL
                        + "=worst to " + BugTicket.MAX_BUG_LEVEL + "=cosmetic): ");
                if (level == null) {
                    return null;
                }
                return new BugTicket(code, owner, priority, "Open", hours, level);
            }
            case "2" -> {
                String tier = readFromSet("  Service tier", SupportTicket.getValidServiceTiers());
                if (tier == null) {
                    return null;
                }
                return new SupportTicket(code, owner, priority, "Open", hours, tier);
            }
            default -> {
                Integer days = readInt("  Days until maintenance window: ");
                if (days == null) {
                    return null;
                }
                Boolean downtime = readYesNo("  Requires downtime (y/n): ");
                if (downtime == null) {
                    return null;
                }
                return new MaintenanceTicket(code, owner, priority, "Open", hours, days, downtime);
            }
        }
    }

    /** Prints every ticket, every owner's workload, and the status tally. */
    private void viewReport() {
        printHeading("TICKET REPORT");
        if (manager.isEmpty()) {
            System.out.println("  No tickets yet. Add one with option 1.");
            return;
        }

        printSection("TICKETS", manager.ticketsByUrgency());
        printSection("WORKLOAD BY OWNER", manager.workloadByOwner());
        printStatusSummary();
    }

    /** Asks for a search term and prints the tickets that match it. */
    private void searchTickets() {
        printHeading("SEARCH TICKETS");
        System.out.println("  Matches on code, owner, status, priority, severity or type.");

        String term = readLine("  Search for: ");
        if (term == null) {
            return;
        }

        List<Ticket> matches = manager.search(term);
        if (matches.isEmpty()) {
            System.out.println("  Nothing matched \"" + term + "\".");
            return;
        }

        printSection(matches.size() + " MATCH(ES) FOR \"" + term + "\"", matches);
    }

    /** Asks for a ticket code, shows that ticket, and removes it once confirmed. */
    private void deleteTicket() {
        printHeading("DELETE TICKET");
        if (manager.isEmpty()) {
            System.out.println("  There is nothing to delete.");
            return;
        }

        Integer code = readInt("  Ticket code to delete: ");
        if (code == null) {
            return;
        }

        Ticket ticket = manager.findByCode(code);
        if (ticket == null) {
            System.out.println("  No ticket with code #" + code + ".");
            return;
        }

        System.out.println("  " + ticket.reportLine().trim());
        Boolean confirmed = readYesNo("  Delete this ticket (y/n): ");
        if (confirmed == null || !confirmed) {
            System.out.println("  Left alone.");
            return;
        }

        manager.delete(code);
        System.out.println("  Deleted ticket #" + code + ". " + manager.size() + " remaining.");
    }

    /**
     * Prints a titled block of entries. Every entry supplies its own line and decides whether it
     * needs flagging, so no type checks are needed here.
     */
    private void printSection(String title, List<? extends Reportable> entries) {
        System.out.println();
        System.out.println("  " + title);
        System.out.println("  " + "-".repeat(LINE_WIDTH - 2));

        for (Reportable entry : entries) {
            System.out.printf("  %s%s%n",
                    entry.reportLine(),
                    entry.needsAttention() ? "   << NEEDS ATTENTION" : "");
        }
    }

    /** Prints the ticket total and how many sit in each status. */
    private void printStatusSummary() {
        StringBuilder line = new StringBuilder();
        for (Map.Entry<String, Integer> count : manager.countByStatus().entrySet()) {
            if (!line.isEmpty()) {
                line.append("   ");
            }
            line.append(count.getKey()).append(": ").append(count.getValue());
        }

        System.out.println();
        System.out.println("  " + manager.size() + " ticket(s)   " + line);
    }

    /** Prints the title shown once at start-up. */
    private void printBanner() {
        System.out.println();
        System.out.println("=".repeat(LINE_WIDTH));
        System.out.println("  TICKET MANAGER");
        System.out.println("=".repeat(LINE_WIDTH));
        System.out.println("  Loaded " + manager.size() + " sample ticket(s).");
    }

    /** Prints the list of menu options. */
    private void printMenu() {
        System.out.println();
        System.out.println("-".repeat(LINE_WIDTH));
        System.out.println("  1) Add ticket     2) View report     3) Search"
                + "     4) Delete ticket     5) Exit");
        System.out.println("-".repeat(LINE_WIDTH));
    }

    /** Prints a section heading. */
    private void printHeading(String title) {
        System.out.println();
        System.out.println("  " + title);
    }

    /** Reads one trimmed line of input. */
    private String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }

    /** Reads a whole number, asking again until one is typed. */
    private Integer readInt(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (input == null) {
                return null;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a whole number.");
            }
        }
    }

    /** Reads a number of zero or more, asking again until one is typed. */
    private Double readDouble(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (input == null) {
                return null;
            }
            try {
                double value = Double.parseDouble(input);
                if (value < 0) {
                    System.out.println("  Please enter zero or more.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a number, for example 8 or 7.5.");
            }
        }
    }

    /** Reads a yes or no answer, asking again until one is given. */
    private Boolean readYesNo(String prompt) {
        while (true) {
            String input = readLine(prompt);
            if (input == null) {
                return null;
            }
            if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
                return true;
            }
            if (input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
                return false;
            }
            System.out.println("  Please answer y or n.");
        }
    }

    /**
     * Reads one of a set of allowed values, listing them in the prompt and asking again until
     * the answer is one of them.
     *
     * @return the answer spelled as the set spells it, or null if the input ran out
     */
    private String readFromSet(String label, Set<String> allowed) {
        String prompt = label + " " + String.join(" / ", allowed) + ": ";
        while (true) {
            String input = readLine(prompt);
            if (input == null) {
                return null;
            }
            for (String option : allowed) {
                if (option.equalsIgnoreCase(input)) {
                    return option;
                }
            }
            System.out.println("  Please choose one of: " + String.join(", ", allowed));
        }
    }

    /** Loads a few tickets so the report has something to show on the first run. */
    private void seedSampleTickets() {
        manager.add(new SupportTicket(101, "Chandler", "Normal", "Open", 8, "Premium"));
        manager.add(new SupportTicket(102, "Monica", "High", "In-progress", 8, "Basic"));
        manager.add(new BugTicket(103, "Ross", "Low", "Open", 8, 4));
        manager.add(new BugTicket(104, "Rachel", "Normal", "Open", 8, 2));
        manager.add(new MaintenanceTicket(105, "Phoebe", "Normal", "Open", 12.0, 3, true));
        manager.add(new MaintenanceTicket(106, "Joey", "High", "Closed", 12.0, 3, false));
        manager.add(new BugTicket(107, "Monica", "High", "Open", 20, 1));
    }
}
