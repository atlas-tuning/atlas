package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.ui.Fonts;
import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.component.toolbar.ConsoleTabToolbar;
import com.github.manevolent.atlas.ui.Editor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static com.github.manevolent.atlas.ui.Fonts.getTextColor;

public class ConsoleTab extends Tab implements FocusListener, Thread.UncaughtExceptionHandler {
    private static final String eol = "\r\n";
    private static final DateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");;
    private static final String logFormat = "[%s] [%s] [%s] %s" + eol;
    private JTextPane console;


    private static final int MAXIMUM_LINES = 1000;
    private int lines = 0;
    private ConsoleTabToolbar toolbar;

    public ConsoleTab(Editor editor, JTabbedPane tabbedPane) {
        super(editor, tabbedPane);
    }

    @Override
    public String getTitle() {
        return "Console";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.TERMINAL, getTextColor());
    }

    @Override
    protected void preInitComponent(JPanel component) {
        console = new JTextPane();

        //TODO doubt we want all the messages in production
        Log.get().setLevel(Level.ALL);
        Log.get().addHandler(new LogHandler());

        console.addFocusListener(this);
        console.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                console.setFocusable(true);
                console.grabFocus();
            }
        });

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

        JPanel noWrapPanel = new JPanel( new BorderLayout() );
        noWrapPanel.add(console);

        JScrollPane scrollPane = new JScrollPane(noWrapPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        toolbar = new ConsoleTabToolbar(this);
        panel.add(toolbar.getComponent(), BorderLayout.WEST);

        Log.get().log(Level.FINE, "Log started.");
    }

    private void edit(Runnable action) {
        try {
            console.setEditable(true);
            action.run();
        } finally {
            console.setEditable(false);
            console.getCaret().setVisible(true);
            console.getCaret().setSelectionVisible(true);
        }
    }

    private void cullConsole() {
        while (lines > MAXIMUM_LINES) {
            // Cull one line
            int eol = console.getText().indexOf(ConsoleTab.eol);
            if (eol <= 0)
                break;

            eol += ConsoleTab.eol.length();

            console.select(0, eol);

            edit(() -> {
                console.replaceSelection("");
                lines--;
            });
        }
    }

    private void appendToPane(String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, Fonts.getConsoleFontFamilyName());
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_LEFT);

        int numLines = (int) msg.lines().count();

        if (numLines > MAXIMUM_LINES && numLines > 1) {
            // Ignore this message, it's huge
            Log.ui().log(Level.WARNING, "Muted a log message with " + numLines + " lines; can't fit in console" +
                    " with a maximum of " + MAXIMUM_LINES + " lines.");
            return;
        }

        int len = console.getDocument().getLength();
        console.setCaretPosition(len);
        console.setCharacterAttributes(aset, false);

        edit(() -> {
            console.replaceSelection(msg);
            lines += numLines;
        });

        cullConsole();

        console.revalidate();
        console.repaint();
    }

    public void clearConsole() {
        edit(() -> {
            console.setText("");
            lines = 0;
        });
    }

    public void copyConsole() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(console.getText());
        clipboard.setContents(selection, selection);

        Log.ui().log(Level.INFO, "Copied console contents to clipboard.");
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
        console.grabFocus();
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

    public void saveConsole() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter def = new FileNameExtensionFilter("Text files", "txt");
        fileChooser.addChoosableFileFilter(def);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Log files", "log"));
        fileChooser.setFileFilter(def);
        fileChooser.setDialogTitle("Save Console Output");
        if (fileChooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(console.getText());
                Log.ui().log(Level.INFO, "Console log saved to " + file.getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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

            message = message.replace("\t", "    ");

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
