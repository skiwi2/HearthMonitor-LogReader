package com.github.skiwi2.hearthmonitor.logreader;

import com.github.skiwi2.hearthmonitor.logapi.LogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ABCEntryReaders;
import com.github.skiwi2.hearthmonitor.logreader.logentries.ALogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.BLogEntry;
import com.github.skiwi2.hearthmonitor.logreader.logentries.CLogEntry;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class MonitoringFileLogReaderTest {
    @Test
    public void testReadEntry() throws InterruptedException {
        List<LogEntry> logEntries = Collections.synchronizedList(new ArrayList<>());

        AtomicReference<Exception> exceptionReference = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(getClass().getResource("test.log").toURI()), StandardCharsets.UTF_8);
                try (CloseableLogReader logReader = new MonitoringFileLogReader(bufferedReader, new ABCEntryReaders())) {
                    logEntries.add(logReader.readEntry());
                    logEntries.add(logReader.readEntry());
                    logEntries.add(logReader.readEntry());
                    logReader.readEntry();
                } catch (NoMoreInputException ex) {
                    //ok
                }
            } catch (Exception ex) {
                exceptionReference.set(ex);
            }
        });

        thread.start();

        Thread.sleep(250);
        thread.interrupt();
        Thread.sleep(250);

        if (exceptionReference.get() != null) {
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

        AtomicInteger noMoreInputExceptions = new AtomicInteger(0);
        AtomicReference<Exception> exceptionReference = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                try (CloseableLogReader logReader = new MonitoringFileLogReader(Files.newBufferedReader(logFile, StandardCharsets.UTF_8), new ABCEntryReaders())) {
                    logReader.readEntry();
                    logReader.readEntry();
                    logReader.readEntry();
                    logReader.readEntry();
                } catch (NoMoreInputException ex) {
                    noMoreInputExceptions.incrementAndGet();
                }
            } catch (Exception ex) {
                exceptionReference.set(ex);
            }
        });
        thread.start();

        Thread.sleep(250);
        thread.interrupt();
        Thread.sleep(250);

        if (exceptionReference.get() != null) {
            fail();
        }

        assertEquals(1, noMoreInputExceptions.get());

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

        AtomicReference<Exception> exceptionReference = new AtomicReference<>();

        List<LogEntry> logEntries = Collections.synchronizedList(new ArrayList<>());

        Thread thread = new Thread(() -> {
            try {
                try (CloseableLogReader logReader = new MonitoringFileLogReader(Files.newBufferedReader(logFile, StandardCharsets.UTF_8), new ABCEntryReaders())) {
                    logEntries.add(logReader.readEntry());
                    logEntries.add(logReader.readEntry());
                    logEntries.add(logReader.readEntry());
                    logEntries.add(logReader.readEntry());
                    logReader.readEntry();
                } catch (NoMoreInputException ex) {
                    //ok
                }
            } catch (Exception ex) {
                exceptionReference.set(ex);
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

        if (exceptionReference.get() != null) {
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