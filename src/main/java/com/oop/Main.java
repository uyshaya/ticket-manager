package com.oop;

import com.oop.storage.TicketFileStore;
import com.oop.ui.ConsoleApp;

import java.util.Scanner;

/**
 * Starts the ticket manager.
 */
public class Main {

    private static final String DATA_FILE = "tickets.csv";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleApp(scanner, new TicketFileStore(DATA_FILE)).run();
        }
    }
}
