package com.oop.storage;

import com.oop.model.BugTicket;
import com.oop.model.MaintenanceTicket;
import com.oop.model.SupportTicket;
import com.oop.model.Ticket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketFileStoreTest {

    @TempDir
    Path folder;

    private TicketFileStore storeIn(String fileName) {
        return new TicketFileStore(folder.resolve(fileName).toString());
    }

    @Test
    void savedTicketsComeBackAfterRestart() throws IOException {
        List<Ticket> saved = Arrays.asList(
                new SupportTicket(101, "Chandler", "Normal", "Open", 8, "Premium"),
                new BugTicket(103, "Ross", "Low", "Open", 8, 4),
                new MaintenanceTicket(105, "Phoebe", "Normal", "Closed", 12, 3, true));

        TicketFileStore writer = storeIn("tickets.csv");
        writer.save(saved);

        // A new store stands in for the next run of the program.
        List<Ticket> loaded = storeIn("tickets.csv").load();

        assertEquals(3, loaded.size());
        for (int i = 0; i < saved.size(); i++) {
            assertEquals(saved.get(i).toCsvRow(), loaded.get(i).toCsvRow());
        }
    }

    @Test
    void everyFieldSurvivesTheRoundTrip() throws IOException {
        Ticket original = new MaintenanceTicket(42, "Jane Doe", "High", "In-progress", 7.5, 2, true);

        TicketFileStore store = storeIn("round-trip.csv");
        store.save(List.of(original));
        Ticket loaded = store.load().get(0);

        assertEquals(42, loaded.getCode());
        assertEquals("Jane Doe", loaded.getOwner());
        assertEquals("High", loaded.getPriority());
        assertEquals("In-progress", loaded.getStatus());
        assertEquals(7.5, loaded.getAllocatedTimeInHours());
        assertEquals("Planned - Downtime", loaded.getSeverity());
        assertEquals(16.0, loaded.getResolutionDeadlineInHours());
    }

    @Test
    void missingFileLoadsNothingInsteadOfFailing() throws IOException {
        TicketFileStore store = storeIn("not-created-yet.csv");

        assertFalse(store.exists());
        assertTrue(store.load().isEmpty());
        assertTrue(store.getLoadWarnings().isEmpty());
    }

    @Test
    void savingCreatesTheFileOnFirstRun() throws IOException {
        TicketFileStore store = storeIn("new.csv");
        assertFalse(store.exists());

        store.save(new ArrayList<>());

        assertTrue(store.exists());
        assertEquals(1, Files.readAllLines(folder.resolve("new.csv")).size());
    }

    @Test
    void badLinesAreSkippedAndTheGoodOnesStillLoad() throws IOException {
        Files.write(folder.resolve("messy.csv"), List.of(
                "type,code,owner,priority,status,hours,extra1,extra2",
                "BUG,abc,Ross,Low,Open,8.0,4",
                "BUG,103",
                "ALIEN,104,Rachel,Normal,Open,8.0,2",
                "BUG,105,Rachel,Urgent,Open,8.0,2",
                "BUG,106,Rachel,Normal,Pending,8.0,2",
                "MAINTENANCE,107,Phoebe,Normal,Open,12.0,3",
                "MAINTENANCE,108,Joey,High,Open,12.0,3,maybe",
                "BUG,109,Ross,Low,Open,eight,4",
                "SUPPORT,110,Chandler,Normal,Open,8.0,Gold",
                "BUG,111,Ross,Low,Open,8.0,99",
                "BUG,112,Ross,Low,Open,-4.0,1",
                "",
                "BUG,113,Valid,Normal,Open,8.0,1"));

        TicketFileStore store = storeIn("messy.csv");
        List<Ticket> loaded = store.load();

        assertEquals(1, loaded.size());
        assertEquals(113, loaded.get(0).getCode());
        assertEquals(11, store.getLoadWarnings().size());
    }

    @Test
    void eachKindOfBadLineSaysWhatIsWrong() throws IOException {
        Files.write(folder.resolve("one-bad.csv"), List.of("BUG,abc,Ross,Low,Open,8.0,4"));

        TicketFileStore store = storeIn("one-bad.csv");
        store.load();

        assertEquals(List.of("line 1: code 'abc' is not a whole number"), store.getLoadWarnings());
    }

    @Test
    void warningsAreClearedBetweenLoads() throws IOException {
        Path path = folder.resolve("changing.csv");
        Files.write(path, List.of("BUG,abc,Ross,Low,Open,8.0,4"));

        TicketFileStore store = storeIn("changing.csv");
        store.load();
        assertEquals(1, store.getLoadWarnings().size());

        Files.write(path, List.of("BUG,113,Ross,Low,Open,8.0,1"));
        store.load();
        assertTrue(store.getLoadWarnings().isEmpty());
    }

    @Test
    void anUnreadableFileIsReportedRatherThanTreatedAsEmpty() throws IOException {
        Path path = folder.resolve("locked.csv");
        Files.write(path, List.of("BUG,113,Ross,Low,Open,8.0,1"));

        File locked = path.toFile();
        if (!locked.setReadable(false)) {
            return; // The running user can read it regardless, so there is nothing to check.
        }

        try {
            TicketFileStore store = storeIn("locked.csv");
            assertThrows(IOException.class, store::load);
        } finally {
            locked.setReadable(true);
        }
    }
}
