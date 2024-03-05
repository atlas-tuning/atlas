package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ConsoleTab extends Tab implements FocusListener, Thread.UncaughtExceptionHandler {
    private static final DateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");;
    private static final String logFormat = "[%s] [%s] [%s] %s\n";
    private JTextPane console;

    public ConsoleTab(EditorForm editor) {
        super(editor);
    }

    @Override
    public String getTitle() {
        return "Console";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.TERMINAL, Color.WHITE);
    }

    @Override
    protected void preInitComponent(JPanel component) {
        console = new JTextPane();

        //TODO doubt we want all the messages in production
        Log.get().setLevel(Level.ALL);
        Log.get().addHandler(new LogHandler());

        console.addFocusListener(this);

        // Handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler(this);
        SwingUtilities.invokeLater(() -> {
            Thread.currentThread().setUncaughtExceptionHandler(ConsoleTab.this);
        });
    }

    @Override
    protected void initComponent(JPanel panel) {
        panel.setLayout(new BorderLayout());

        console.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        console.setFocusable(false);
        console.setBackground(panel.getBackground());
        console.setText("");

        JScrollPane scrollPane = new JScrollPane(console);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        Log.get().log(Level.FINE, "Log started.");
    }

    private void appendToPane(String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_LEFT);

        int len = console.getDocument().getLength();
        console.setCaretPosition(len);
        console.setCharacterAttributes(aset, false);

        console.setEditable(true);
        console.replaceSelection(msg);
        console.setEditable(false);
        console.getCaret().setVisible(true);
        console.getCaret().setSelectionVisible(true);

        console.revalidate();
        console.repaint();
    }

    private Color getConsoleColor(Level level) {
        int value = level.intValue();
        if (value >= Level.SEVERE.intValue()) {
            return Color.RED;
        } else if (value >= Level.WARNING.intValue()) {
            return Color.ORANGE;
        } else if (value >= Level.INFO.intValue()) {
            return Color.WHITE;
        } else {
            return Color.WHITE.darker();
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        console.getCaret().setVisible(true);
        console.getCaret().setSelectionVisible(true);
    }

    @Override
    public void focusLost(FocusEvent e) {

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.get().log(Level.WARNING, "Uncaught exception on thread " + t.getName(), e);
    }

    private class LogHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            String message;
            if (record.getThrown() != null) {
                message = ExceptionUtils.getStackTrace(record.getThrown());
            } else {
                message = record.getMessage();
            }
            String logMessage = String.format(logFormat,
                    dateFormatter.format(Date.from(record.getInstant())),
                    record.getLevel().getName(),
                    record.getLoggerName(),
                    message
            );
            Color color = getConsoleColor(record.getLevel());
            appendToPane(logMessage, color);
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }
}
