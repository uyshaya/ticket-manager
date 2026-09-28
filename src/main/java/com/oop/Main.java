package com.oop;

import com.oop.ui.ConsoleApp;

import java.util.Scanner;

/**
 * Starts the ticket manager.
 */
public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleApp(scanner).run();
        }
    }
}
