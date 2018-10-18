package com.assemblypayments.utils;

import android.support.annotation.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Helper methods for language and platform-specific operations.
 */
public final class SystemHelper {

    private static ConsoleAdapter consoleAdapter;

    public synchronized static void register(ConsoleAdapter adapter) {
        SystemHelper.consoleAdapter = adapter;
    }

    public synchronized static void unregister(ConsoleAdapter adapter) {
        if (SystemHelper.consoleAdapter == adapter) {
            SystemHelper.consoleAdapter = null;
        }
    }

    private SystemHelper() {
    }

    /**
     * Prints text to console.
     *
     * @param text Text to print.
     */
    public static void consolePrint(@NotNull String text) {
        final ConsoleAdapter adapter = SystemHelper.consoleAdapter;
        if (adapter != null) {
            adapter.print(text);
        }
    }

    /**
     * Prints line of text to console.
     *
     * @param text Text to print.
     */
    public static void consolePrintln(@NonNull String text) {
        final ConsoleAdapter adapter = SystemHelper.consoleAdapter;
        if (adapter != null) {
            adapter.println(text);
        }
    }

    /**
     * Prints empty line to console.
     */
    public static void consolePrintln() {
        consolePrintln("");
    }

    /**
     * Clears contents of the console.
     */
    public static void clearConsole() {
        final ConsoleAdapter adapter = SystemHelper.consoleAdapter;
        if (adapter != null) {
            adapter.clear();
        }
    }

    public interface ConsoleAdapter {
        void print(@NotNull String text);

        void println(@NonNull String text);

        void clear();
    }

}
