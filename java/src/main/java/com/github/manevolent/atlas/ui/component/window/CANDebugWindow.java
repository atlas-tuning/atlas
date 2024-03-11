package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.connection.Connection;
import com.github.manevolent.atlas.connection.ConnectionMode;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.MemoryAddress;
import com.github.manevolent.atlas.model.MemorySection;
import com.github.manevolent.atlas.protocol.uds.UDSFrame;
import com.github.manevolent.atlas.protocol.uds.UDSListener;
import com.github.manevolent.atlas.protocol.uds.UDSSession;
import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.util.*;
import com.github.manevolent.atlas.ui.component.menu.canlog.FileMenu;
import com.github.manevolent.atlas.ui.component.toolbar.CANDebugToolbar;
import org.apache.commons.lang.StringEscapeUtils;
import org.checkerframework.checker.units.qual.A;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.github.manevolent.atlas.ui.util.Fonts.getTextColor;

public class CANDebugWindow extends Window implements ChangeListener, UDSListener {
    private JMenuBar menubar;
    private FileMenu fileMenu;

    private CANDebugToolbar toolbar;
    private JTabbedPane tabbedPane;

    private List<CANDebugPage> pages = new ArrayList<>();
    private CANDebugPage activePage;
    private CANDebugPage recordingPage;

    public CANDebugWindow(Editor editor) {
        super(editor);
    }

    @Override
    protected void preInitComponent(JInternalFrame window) {
        super.preInitComponent(window);

        Inputs.bind(this.getComponent().getRootPane(),
                "record",
                this::toggleRecording,
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.META_DOWN_MASK)); // OSX
    }

    private JMenuBar initMenu() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add((fileMenu = new FileMenu(this)).getComponent());

        return menuBar;
    }

    public CANDebugPage getRecordingPage() {
        return recordingPage;
    }

    public void setRecordingPage(CANDebugPage page) {
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

    @Override
    public void onUDSFrameRead(UDSFrame frame) {
        if (recordingPage == null) {
            return;
        }

        recordingPage.addFrame(frame);
    }

    @Override
    public void onUDSFrameWrite(UDSFrame frame) {
        if (recordingPage == null) {
            return;
        }

        recordingPage.addFrame(frame);
    }

    public Connection establishConnection() {
        try {
            Connection connection = getParent().getConnection();
            UDSSession session = connection.getSession();
            if (!session.hasListener(this)) {
                session.addListener(this);
            }

            if (connection.getConnectionMode() == ConnectionMode.DISCONNECTED) {
                connection.changeConnectionMode(ConnectionMode.IDLE);
            }

            return connection;
        } catch (Exception ex) {
            Log.can().log(Level.SEVERE, "Problem establishing connection with ECU", ex);
            JOptionPane.showMessageDialog(getParent(), "Problem establishing connection with ECU!\r\n" +
                    ex.getMessage() + "\r\n" + "See console output (F12) for more details.",
                    "Connection failed",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public CANDebugPage getActivePage() {
        return activePage;
    }

    public void setActivePage(CANDebugPage page) {
        if (this.activePage != page && this.activePage != null) {
            this.activePage.deactivated();
        }

        if (page != null) {
            page.activated();
        }

        this.activePage = page;

        updateTitle();

        if (page != null) {
            page.revalidate();
            page.repaint();
        }
    }

    private void addPage(CANDebugPage page) {
        tabbedPane.addTab(page.getTitle(), Icons.get(CarbonIcons.CATALOG), page);
        tabbedPane.setSelectedComponent(page);
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    public void deletePage(CANDebugPage page) {
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

        String suggestedName = Instant.now().toString();
        String newCanLogName = JOptionPane.showInputDialog(getParent(),
                "Specify a name for this debugging session", suggestedName);
        if (newCanLogName == null || newCanLogName.isBlank()) {
            return;
        }

        CANDebugPage page = new CANDebugPage(this);
        page.setName(newCanLogName);
        addPage(page);
        setRecordingPage(page);

        toolbar.update();
    }

    public void stopRecording() {
        Connection connection = getParent().getConnection();
        if (connection != null) {
            try {
                connection.getSession().removeListener(this);
            } catch (IOException | TimeoutException e) {
                Log.ui().log(Level.WARNING, "Problem removing listener from connection", e);
            }
        }

        if (recordingPage != null) {
            CANDebugPage page = recordingPage;
            setRecordingPage(null);
            tabbedPane.setSelectedComponent(page);
            page.setPaused(true, true);

            updateTitle();
        }

        toolbar.update();
    }

    public void jumpToLatest() {
        if (activePage != null) {
            activePage.jumpToLatest();
        }
    }

    public void changeMode() {
        CANDebugPage page = getRecordingPage();
        if (page == null) {
            return;
        }

        Connection connection = establishConnection();
        if (connection == null) {
            return;
        }

        Object[] options = Arrays.stream(ConnectionMode.values())
                .filter(x -> x != connection.getConnectionMode() && x != ConnectionMode.DISCONNECTED)
                .toArray();

        ConnectionMode selected = (ConnectionMode) JOptionPane.showInputDialog(
                getParent(),
                "Select a mode to change to",
                "Select Mode",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                null
        );

        if (selected == null) {
            return;
        }

        jumpToLatest();

        Job.fork(() -> {
            try {
                connection.changeConnectionMode(selected);
            } catch (Exception e) {
                Log.can().log(Level.SEVERE, "Problem changing connection mode with ECU", e);
                JOptionPane.showMessageDialog(getParent(), "Problem changing connection mode with ECU!\r\n" +
                                e.getMessage() + "\r\n" + "See console output (F12) for more details.",
                        "Connection failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void clearDTC() {
        if (JOptionPane.showConfirmDialog(getComponent(),
                "Are you sure you want to clear diagnostic trouble codes?",
                "Clear DTC",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        Job.fork(() -> {
            Connection connection = establishConnection();

            if (connection == null) {
                return;
            }

            try {
                connection.clearDTC();
            } catch (IOException | TimeoutException e) {
                Log.can().log(Level.SEVERE, "Problem clearing DTC", e);
                JOptionPane.showMessageDialog(getParent(), "Problem clearing DTC!\r\n" +
                                e.getMessage() + "\r\n" + "See console output (F12) for more details.",
                        "Connection failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(getComponent(),
                    "Cleared diagnostic trouble codes successfully.",
                    "Clear DTC",
                    JOptionPane.INFORMATION_MESSAGE);
        });

    }

    public void clearFrames() {
        if (recordingPage != null) {
            if (JOptionPane.showConfirmDialog(getComponent(),
                    "Are you sure you want to clear all CAN frames?",
                    "Clear Frames",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            recordingPage.clearFrames();
        }
    }

    @Override
    protected void initComponent(JInternalFrame window) {
        window.setLayout(new BorderLayout());
        //window.setJMenuBar(menubar = initMenu());

        window.add((toolbar = new CANDebugToolbar(this)).getComponent(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        window.add(tabbedPane, BorderLayout.CENTER);

        setActivePage(null);
        setRecordingPage(null);

        updateTitle();

        tabbedPane.addChangeListener(this);
    }

    @Override
    public String getTitle() {
        CANDebugPage active = getActivePage();
        if (active != null) {
            return "CAN Debugging - " + active.getName();
        } else {
            return "CAN Debugging";
        }
    }

    @Override
    public Icon getIcon() {
        if (isRecording()) {
            return Icons.get(CarbonIcons.RECORDING_FILLED, Color.RED);
        } else {
            return Icons.get(CarbonIcons.DEBUG, getTextColor());
        }
    }

    @Override
    public void reload() {
        updateTitle();
    }

    public CANDebugToolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        setActivePage((CANDebugPage) tabbedPane.getSelectedComponent());
        toolbar.update();
    }

    public boolean isRecording() {
        return recordingPage != null;
    }

    public void saveSession() {
        CANDebugPage page = getActivePage();
        if (page == null) {
            return;
        }

        page.saveSession();
    }

    public void readMemory() {
        CANDebugPage page = getRecordingPage();
        if (page == null) {
            return;
        }

        Connection connection = establishConnection();
        if (connection == null) {
            return;
        }

        Object[] options = getParent().getProject().getSections().toArray();

        MemorySection selected = (MemorySection) JOptionPane.showInputDialog(
                getParent(),
                "Select a section to read",
                "Read Memory",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                null
        );

        if (selected == null) {
            return;
        }

        Job.fork(() -> {
            try {
                connection.changeConnectionMode(ConnectionMode.READ_MEMORY);
            } catch (IOException | TimeoutException e) {
                Log.can().log(Level.SEVERE, "Problem changing connection mode with ECU", e);
                JOptionPane.showMessageDialog(getParent(), "Problem changing connection mode with ECU!\r\n" +
                                e.getMessage() + "\r\n" + "See console output (F12) for more details.",
                        "Read memory failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            MemoryReader reader = new MemoryReader(getParent(), selected);
            reader.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            AtomicBoolean closed = new AtomicBoolean();
            reader.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (JOptionPane.showConfirmDialog(getParent(),
                            "Are you sure you want to cancel reading this memory section?",
                            "Cancel",
                            JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                        return;
                    }
                    super.windowClosing(e);
                    closed.set(true);
                }
            });

            byte[] memory = new byte[selected.getDataLength()];
            for (long offs = selected.getBaseAddress(); offs < selected.getBaseAddress() + memory.length;) {
                if (closed.get()) {
                    JOptionPane.showMessageDialog(getParent(), "Memory section read was canceled.",
                            "Read memory canceled",
                            JOptionPane.WARNING_MESSAGE);
                    SwingUtilities.invokeLater(reader::dispose);
                    return;
                }

                int readLength = (int) Math.min(
                        selected.getBaseAddress() + memory.length - offs,
                        connection.getMaximumReadSize());

                reader.postStatus((float) (offs - selected.getBaseAddress()) / (float) memory.length, "Reading " +
                        String.format("0x%04X", offs) + "...");

                try {
                    byte[] block = connection.readMemory(
                            MemoryAddress.builder().withSection(selected).withOffset(offs & 0xFFFFFFFFL).build(),
                            readLength);

                    readLength = block.length;

                    System.arraycopy(block, 0, memory, (int) (offs - selected.getBaseAddress()), readLength);

                    if (!reader.isVisible()) {
                        SwingUtilities.invokeLater(() -> {
                            reader.setVisible(true);
                        });
                    }
                } catch (IOException | TimeoutException e) {
                    String message = "Problem reading memory at offset " + Integer.toHexString((int) offs);
                    if (e instanceof TimeoutException || offs <= selected.getBaseAddress()) {
                        SwingUtilities.invokeLater(() -> {
                            reader.setVisible(false);
                        });
                        Log.can().log(Level.SEVERE, message, e);
                        JOptionPane.showMessageDialog(getParent(), message + "!\r\n" +
                                        e.getMessage() + "\r\n" + "See console output (F12) for more details.",
                                "Read memory failed",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    } else {
                        Log.can().log(Level.WARNING, message, e);
                    }
                }

                offs += readLength;
            }

            SwingUtilities.invokeLater(() -> {
                reader.setVisible(false);
            });

            SwingUtilities.invokeLater(() -> {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter def = new FileNameExtensionFilter("Binary files", "bin");
                fileChooser.addChoosableFileFilter(def);
                fileChooser.setFileFilter(def);
                fileChooser.setDialogTitle("Save Memory Dump");
                fileChooser.setSelectedFile(new File(selected.getName() + ".bin"));
                if (fileChooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(memory);
                        Log.ui().log(Level.INFO, "Memory section " + selected.getName() + " saved to "
                                + file.getPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

    public void readDTC() {
        Job.fork(() -> {
            Connection connection = establishConnection();

            if (connection == null) {
                return;
            }

            List<Integer> dtc;

            try {
                dtc = connection.readDTC();
            } catch (IOException | TimeoutException e) {
                Log.can().log(Level.SEVERE, "Problem reading DTC", e);
                JOptionPane.showMessageDialog(getParent(), "Problem reading DTC!\r\n" +
                                e.getMessage() + "\r\n" + "See console output (F12) for more details.",
                        "Connection failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (dtc.isEmpty()) {
                JOptionPane.showMessageDialog(getComponent(),
                        "No DTC was found.",
                        "Read DTC",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                String dtcString = dtc.stream().map(i -> String.format("0x%03X", i))
                        .collect(Collectors.joining(", "));
                JOptionPane.showMessageDialog(getComponent(),
                        "Found " + dtc.size() + " DTC:\r\n" +
                        dtcString,
                        "Read DTC",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private class MemoryReader extends JDialog {
        private final Instant start = Instant.now();
        private final MemorySection section;
        private Instant lastUpdate = start;
        private JProgressBar progressBar;
        private JLabel statusText;
        private JLabel etaText;

        private MemoryReader(Frame parent, MemorySection section) {
            super(parent, "Reading Memory...", true);

            this.section = section;

            initComponents();

            setResizable(false);

            pack();

            setMinimumSize(new Dimension(getWidth() + 100, getPreferredSize().height));
            setPreferredSize(new Dimension(getWidth() + 100, getPreferredSize().height));

            setLocationRelativeTo(parent);
        }

        private void initComponents() {
            JPanel frame = new JPanel();
            frame.setLayout(new GridLayout(4, 1));
            frame.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

            frame.add(Layout.emptyBorder(0, 0, 10, 0,
                    Labels.text("Reading " + section.getName() +
                            " section (" + numberFormat.format(section.getDataLength()) + " bytes)...")));

            frame.add(Layout.emptyBorder(10, 0, 10, 0,
                            statusText = Labels.text("")));
            statusText.setForeground(statusText.getForeground().darker());

            frame.add(Layout.emptyBorder(10, 0, 10, 0, progressBar = new JProgressBar()));

            JPanel pushRight = new JPanel(new BorderLayout());
            pushRight.add(etaText = Layout.alignRight(Labels.text("")), BorderLayout.EAST);
            frame.add(Layout.emptyBorder(pushRight));
            etaText.setForeground(etaText.getForeground().darker());

            progressBar.setValue(0);
            progressBar.setMaximum(1000);

            add(frame);
        }

        public void postStatus(float progress, String status) {
            Instant now = Instant.now();
            SwingUtilities.invokeLater(() -> {
                if (lastUpdate.isBefore(now.minus(1, ChronoUnit.SECONDS))) {
                    long millisElapsed = ChronoUnit.MILLIS.between(start, now);
                    float multiplier = 1f / progress;
                    long millisToGo = (long) (multiplier * millisElapsed) - millisElapsed;
                    int number;
                    String unit;
                    if (millisToGo < 60_000) {
                        number = (int) Math.ceil((double)millisToGo / 1000D);
                        unit = "second";
                    } else {
                        number = (int) Math.ceil((double)millisToGo / 1000D / 60D);
                        unit = "minute";
                    }
                    if (number > 1) {
                        unit += "s"; //pluralize
                    }
                    etaText.setText(number + " " + unit + " left");
                    lastUpdate = now;
                }

                progressBar.setValue((int) (progress * progressBar.getMaximum()));
                statusText.setText(status);

                statusText.revalidate();
                statusText.repaint();

                progressBar.revalidate();
                progressBar.repaint();
            });
        }
    }
}
