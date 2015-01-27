package com.github.skiwi2.hearthmonitor.logreader.logreaders;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logreader.CloseableLogReader;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.EntryParsers;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class MonitoringFileLogReaderTest {
    @Test
    public void testConstructorPECS() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
        try (CloseableLogReader logReader = new MonitoringFileLogReader(bufferedReader, EntryParsers.getABExtendedEntryParsers())) {
            assertNotNull(logReader);
        }
    }

    @Test
    public void testConstructorWithFilterPredicatePECS() throws Exception {
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
        Predicate<Object> predicate = obj -> true;
        try (CloseableLogReader logReader = new MonitoringFileLogReader(bufferedReader, EntryParsers.getABExtendedEntryParsers(), predicate)) {
            assertNotNull(logReader);
        }
    }

    @Test
    public void testReadEntry() throws InterruptedException {
        List<LogEntry> logEntries = Collections.synchronizedList(new ArrayList<>());

        AtomicReference<Throwable> throwableReference = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
                try (CloseableLogReader logReader = new MonitoringFileLogReader(bufferedReader, EntryParsers.getABCEntryParsers())) {

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    //still believes there is a next entry
                    assertTrue(logReader.hasNextEntry());
                    logReader.readNextEntry();
                } catch (NoSuchElementException ex) {
                    //ok
                }
            } catch (Throwable throwable) {
                throwableReference.set(throwable);
            }
        });

        thread.start();

        Thread.sleep(250);
        thread.interrupt();
        Thread.sleep(250);

        if (throwableReference.get() != null) {
            throwableReference.get().printStackTrace();
            fail();
        }

        assertEquals(3, logEntries.size());
        assertEquals(ALogEntry.class, logEntries.get(0).getClass());
        assertEquals(BLogEntry.class, logEntries.get(1).getClass());
        assertEquals(CLogEntry.class, logEntries.get(2).getClass());
    }

    @Test
    public void testReadEntryFilterLines() throws InterruptedException {
        List<LogEntry> logEntries = Collections.synchronizedList(new ArrayList<>());

        AtomicReference<Throwable> throwableReference = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test-filter.log").toURI()), StandardCharsets.UTF_8);
                try (CloseableLogReader logReader = new MonitoringFileLogReader(bufferedReader, EntryParsers.getABCEntryParsers(), string -> !string.equals("0"))) {

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    //still believes there is a next entry
                    assertTrue(logReader.hasNextEntry());
                    logReader.readNextEntry();
                } catch (NoSuchElementException ex) {
                    //ok
                }
            } catch (Throwable throwable) {
                throwableReference.set(throwable);
            }
        });

        thread.start();

        Thread.sleep(250);
        thread.interrupt();
        Thread.sleep(250);

        if (throwableReference.get() != null) {
            throwableReference.get().printStackTrace();
            fail();
        }

        assertEquals(3, logEntries.size());
        assertEquals(ALogEntry.class, logEntries.get(0).getClass());
        assertEquals(BLogEntry.class, logEntries.get(1).getClass());
        assertEquals(CLogEntry.class, logEntries.get(2).getClass());
    }

    @Test
    public void testReadEntryInterruptWhileReading() throws IOException, InterruptedException {
        Path logFile = Files.createTempFile("test", "log");

        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(logFile, StandardCharsets.UTF_8))) {
            printWriter.println("A");
            printWriter.println("B");
            printWriter.println("C");
        }

        AtomicInteger noSuchElementExceptions = new AtomicInteger(0);
        AtomicReference<Throwable> throwableReference = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                try (CloseableLogReader logReader = new MonitoringFileLogReader(Files.newBufferedReader(logFile, StandardCharsets.UTF_8), EntryParsers.getABCEntryParsers())) {

                    assertTrue(logReader.hasNextEntry());
                    logReader.readNextEntry();

                    assertTrue(logReader.hasNextEntry());
                    logReader.readNextEntry();

                    assertTrue(logReader.hasNextEntry());
                    logReader.readNextEntry();

                    //still believes there is a next entry
                    assertTrue(logReader.hasNextEntry());
                    logReader.readNextEntry();
                } catch (NoSuchElementException ex) {
                    noSuchElementExceptions.incrementAndGet();
                }
            } catch (Throwable throwable) {
                throwableReference.set(throwable);
            }
        });
        thread.start();

        Thread.sleep(250);
        thread.interrupt();
        Thread.sleep(250);

        if (throwableReference.get() != null) {
            throwableReference.get().printStackTrace();
            fail();
        }

        assertEquals(1, noSuchElementExceptions.get());

        Files.delete(logFile);
    }

    @Test
    public void testReadEntryAddWhileReading() throws IOException, InterruptedException {
        Path logFile = Files.createTempFile("test", "log");

        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(logFile, StandardCharsets.UTF_8))) {
            printWriter.println("A");
            printWriter.println("B");
            printWriter.println("C");
        }

        AtomicReference<Throwable> throwableReference = new AtomicReference<>();

        List<LogEntry> logEntries = Collections.synchronizedList(new ArrayList<>());

        Thread thread = new Thread(() -> {
            try {
                try (CloseableLogReader logReader = new MonitoringFileLogReader(Files.newBufferedReader(logFile, StandardCharsets.UTF_8), EntryParsers.getABCEntryParsers())) {
                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    //still believes there is a next entry
                    assertTrue(logReader.hasNextEntry());
                    logReader.readNextEntry();
                } catch (NoSuchElementException ex) {
                    //ok
                }
            } catch (Throwable throwable) {
                throwableReference.set(throwable);
            }
        });
        thread.start();

        Thread.sleep(250);
        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(logFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND))) {
            printWriter.println("A");
        }
        Thread.sleep(250);
        thread.interrupt();
        Thread.sleep(250);

        if (throwableReference.get() != null) {
            throwableReference.get().printStackTrace();
            fail();
        }

        assertEquals(4, logEntries.size());
        assertEquals(ALogEntry.class, logEntries.get(0).getClass());
        assertEquals(BLogEntry.class, logEntries.get(1).getClass());
        assertEquals(CLogEntry.class, logEntries.get(2).getClass());
        assertEquals(ALogEntry.class, logEntries.get(3).getClass());

        Files.delete(logFile);
    }

    @Test
    public void testReadEntryAddWhileReadingFilterLines() throws IOException, InterruptedException {
        Path logFile = Files.createTempFile("test-filter", "log");

        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(logFile, StandardCharsets.UTF_8))) {
            printWriter.println("0");
            printWriter.println("A");
            printWriter.println("0");
            printWriter.println("B");
            printWriter.println("0");
            printWriter.println("C");
            printWriter.println("0");
        }

        AtomicReference<Throwable> throwableReference = new AtomicReference<>();

        List<LogEntry> logEntries = Collections.synchronizedList(new ArrayList<>());

        Thread thread = new Thread(() -> {
            try {
                try (CloseableLogReader logReader = new MonitoringFileLogReader(Files.newBufferedReader(logFile, StandardCharsets.UTF_8), EntryParsers.getABCEntryParsers(), string -> !string.equals("0"))) {
                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    assertTrue(logReader.hasNextEntry());
                    logEntries.add(logReader.readNextEntry());

                    //still believes there is a next entry
                    assertTrue(logReader.hasNextEntry());
                    logReader.readNextEntry();
                } catch (NoSuchElementException ex) {
                    //ok
                }
            } catch (Throwable throwable) {
                throwableReference.set(throwable);
            }
        });
        thread.start();

        Thread.sleep(250);
        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(logFile, StandardCharsets.UTF_8, StandardOpenOption.APPEND))) {
            printWriter.println("0");
            printWriter.println("A");
            printWriter.println("0");
        }
        Thread.sleep(250);
        thread.interrupt();
        Thread.sleep(250);

        if (throwableReference.get() != null) {
            throwableReference.get().printStackTrace();
            fail();
        }

        assertEquals(4, logEntries.size());
        assertEquals(ALogEntry.class, logEntries.get(0).getClass());
        assertEquals(BLogEntry.class, logEntries.get(1).getClass());
        assertEquals(CLogEntry.class, logEntries.get(2).getClass());
        assertEquals(ALogEntry.class, logEntries.get(3).getClass());

        Files.delete(logFile);
    }
}