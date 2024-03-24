package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.connection.Connection;
import com.github.manevolent.atlas.connection.ConnectionMode;
import com.github.manevolent.atlas.connection.MemoryFrame;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.settings.Setting;
import com.github.manevolent.atlas.settings.Settings;
import com.github.manevolent.atlas.ui.*;
import com.github.manevolent.atlas.ui.component.menu.datalog.FileMenu;
import com.github.manevolent.atlas.ui.component.menu.datalog.ViewMenu;
import com.github.manevolent.atlas.ui.component.toolbar.DatalogToolbar;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.util.Inputs;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringEscapeUtils;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.Timer;

import java.util.logging.Level;

import static com.github.manevolent.atlas.ui.util.Fonts.getTextColor;

public class DatalogWindow extends Window implements InternalFrameListener, ChangeListener {
    private JMenuBar menubar;
    private FileMenu fileMenu;

    private DatalogToolbar toolbar;
    private JTabbedPane tabbedPane;

    private Timer paintTimer;
    private Timer datalogTimer;

    private List<DatalogPage> pages = new ArrayList<>();
    private DatalogPage activePage;
    private DatalogPage recordingPage;

    public DatalogWindow(Editor editor) {
        super(editor);
    }

    @Override
    protected void preInitComponent(JInternalFrame window) {
        super.preInitComponent(window);
        window.addInternalFrameListener(this);

        Inputs.bind(this.getComponent().getRootPane(),
                "record",
                this::toggleRecording,
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getComponent().getRootPane(),
                "maximize",
                () -> {
                    DatalogPage page = getActivePage();
                    if (page != null) {
                        page.fitToScreen();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getComponent().getRootPane(),
                "zoomOut",
                () -> {
                    DatalogPage page = getActivePage();
                    if (page != null) {
                        page.zoomOut();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getComponent().getRootPane(),
                "zoomIn",
                () -> {
                    DatalogPage page = getActivePage();
                    if (page != null) {
                        page.zoomIn();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getComponent().getRootPane(),
                "left",
                () -> {
                    DatalogPage page = getActivePage();
                    if (page != null) {
                        page.moveLeft();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)); // OSX
        Inputs.bind(this.getComponent().getRootPane(),
                "right",
                () -> {
                    DatalogPage page = getActivePage();
                    if (page != null) {
                        page.moveRight();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)); // OSX
    }

    private JMenuBar initMenu() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add((fileMenu = new FileMenu(this)).getComponent());
        menuBar.add(new ViewMenu(this).getComponent());

        return menuBar;
    }

    public DatalogPage getRecordingPage() {
        return recordingPage;
    }

    public void setRecordingPage(DatalogPage page) {
        toolbar.setPaused(page == null);

        if (page == null && this.recordingPage != null) {
            // Recording stopped
            int index = tabbedPane.indexOfComponent(this.recordingPage);
            if (index >= 0) {
                tabbedPane.setIconAt(index, Icons.get(CarbonIcons.CATALOG));
            }
        }

        this.recordingPage = page;

        if (page != null) {
            // Recording started
            page.setPaused(false, false);

            int index = tabbedPane.indexOfComponent(page);
            if (index >= 0) {
                tabbedPane.setIconAt(index, Icons.get(CarbonIcons.RECORDING_FILLED, Color.RED));
            }
        }

        updateTitle();
    }

    public Connection establishConnection() {
        try {
            Connection connection = getParent().getConnection();
            connection.changeConnectionMode(ConnectionMode.READ_MEMORY);
            return connection;
        } catch (Exception ex) {
            Log.can().log(Level.SEVERE, "Problem establishing datalog session with ECU", ex);
            JOptionPane.showMessageDialog(getParent(), "Failed to establish datalog connection with ECU!\r\n" +
                    ex.getMessage() + "\r\n" +
                            "See console output (F12) for more details.",
                    "Connection failed",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public DatalogPage getActivePage() {
        return activePage;
    }

    public void setActivePage(DatalogPage page) {
        if (this.activePage != page && this.activePage != null) {
            this.activePage.deactivated();
        }

        if (page != null) {
            page.activated();
        } else {
            toolbar.setT(0);
        }

        this.activePage = page;

        updateTitle();

        if (page != null) {
            page.revalidate();
            page.repaint();
        }
    }

    private void addPage(DatalogPage page) {
        tabbedPane.addTab(page.getTitle(), Icons.get(CarbonIcons.CATALOG), page);
        tabbedPane.setSelectedComponent(page);
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    public void deletePage(DatalogPage datalogPage) {
        if (recordingPage != null && !recordingPage.isPaused()) {
            stopRecording();
        }
    }

    public void toggleRecording() {
        if (recordingPage != null) {
            stopRecording();
        } else {
            startRecording();
        }

        updateTitle();
    }

    public void startRecording() {
        // Establish connection
        if (establishConnection() == null) {
            return;
        }

        String suggestedName = Instant.now().toString().replaceAll(":", "-");
        String newDatalogName = JOptionPane.showInputDialog(getParent(),
                "Specify a name for this recording", suggestedName);
        if (newDatalogName == null || newDatalogName.isBlank()) {
            return;
        }

        DatalogPage page = new DatalogPage(this);
        page.setName(newDatalogName);
        addPage(page);
        setRecordingPage(page);

        long frequency = Settings.get(Setting.DATALOG_FREQUENCY, 30);

        if (datalogTimer == null) { // Which it SHOULD be
            datalogTimer = new Timer("Datalog");
        }

        datalogTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                DatalogPage recordingPage = getRecordingPage();
                if (recordingPage == null || recordingPage.isPaused()) {
                    return;
                }

                Collection<MemoryParameter> parameters = recordingPage.getActiveParameters();

                try {
                    MemoryFrame frame = establishConnection().readFrame(parameters);
                    if (frame != null) {
                        recordingPage.addFrame(frame);
                    }
                } catch (Exception ex) {
                    Log.ui().log(Level.SEVERE, "Problem getting datalog frame", ex);
                }
            }
        }, 1000L, 1000 / frequency);
    }

    public void stopRecording() {
        if (datalogTimer != null) {
            datalogTimer.cancel();
            datalogTimer.purge();
            datalogTimer = null;
        }

        if (recordingPage != null) {
            DatalogPage page = recordingPage;
            setRecordingPage(null);
            tabbedPane.setSelectedComponent(page);
            page.setPaused(true, true);

            updateTitle();
        }
    }

    @Override
    protected void initComponent(JInternalFrame window) {
        window.setLayout(new BorderLayout());
        window.add((toolbar = new DatalogToolbar(this)).getComponent(), BorderLayout.NORTH);
        window.setJMenuBar(menubar = initMenu());


        tabbedPane = new JTabbedPane();
        window.add(tabbedPane, BorderLayout.CENTER);

        setActivePage(null);
        setRecordingPage(null);

        updateTitle();

        tabbedPane.addChangeListener(this);
    }

    @Override
    public String getTitle() {
        DatalogPage active = getActivePage();
        if (active != null) {
            return "Data Logging - " + active.getName();
        } else {
            return "Data Logging";
        }
    }

    @Override
    public Icon getIcon() {
        if (isRecording()) {
            return Icons.get(CarbonIcons.RECORDING_FILLED, Color.RED);
        } else {
            return Icons.get(CarbonIcons.CHART_AVERAGE, getTextColor());
        }
    }

    @Override
    public void reload() {
        updateTitle();
    }

    public DatalogToolbar getToolbar() {
        return toolbar;
    }

    private void startTimer() {
        if (paintTimer == null) {
            Timer timer = new Timer("Update");
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (DatalogWindow.this.activePage != null) {
                        tabbedPane.repaint();
                    }
                }
            }, 0L, 1000L / 40L);
        }
    }

    private void stopTimer() {
        if (paintTimer != null) {
            paintTimer.cancel();
            paintTimer.purge();
            paintTimer = null;
        }
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
        startTimer();
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        stopTimer();
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {
        stopTimer();
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {
        startTimer();
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        startTimer();
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        stopTimer();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        setActivePage((DatalogPage) tabbedPane.getSelectedComponent());
    }

    public boolean isRecording() {
        return recordingPage != null;
    }

    private static void writeCell(String value, Writer writer) throws IOException {
        String escaped = StringEscapeUtils.escapeCsv(value);
        writer.write("\"" +escaped + "\",");
    }

    private static void writeRow(Writer writer, String... cells) throws IOException {
        for (String string : cells) {
            writeCell(string, writer);
        }
        writer.write("\r\n");
    }

    public void saveDatalog(boolean includeAll) {
        DatalogPage page = getActivePage();
        if (page == null) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter def = new FileNameExtensionFilter("Comma-separated value file (*.csv)", "csv");
        fileChooser.addChoosableFileFilter(def);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Log files (*.log)", "log"));
        fileChooser.setFileFilter(def);
        fileChooser.setSelectedFile(new File(page.getName() + ".csv"));
        fileChooser.setDialogTitle("Export Datalog");
        if (fileChooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(file)) {
                writeCell("Time", writer);
                for (MemoryParameter parameter : page.getActiveParameters()) {
                    writeCell(parameter.getName(), writer);
                }
                writer.write("\r\n");

                for (MemoryFrame frame : Lists.reverse(page.getFrames())) { // Reversed for time order (asc. desired)
                    boolean inView = frame.getInstant().isAfter(page.getLeft()) &&
                            frame.getInstant().isBefore(page.getRight());

                    if (!inView && !includeAll) {
                        continue;
                    }

                    writeCell(frame.getInstant().toString(), writer);
                    for (MemoryParameter parameter : page.getActiveParameters()) {
                        byte[] data = frame.getData(parameter);
                        if (data != null) {
                            writeCell(String.format("%.2f", parameter.getValue(data)), writer);
                        } else {
                            writeCell("", writer);
                        }
                    }
                    writer.write("\r\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Log.ui().log(Level.INFO, "Datalog exported to " + file.getPath());
        }
    }
}
