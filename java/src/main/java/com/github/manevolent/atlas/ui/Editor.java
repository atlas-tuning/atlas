package com.github.manevolent.atlas.ui;

import com.github.manevolent.atlas.connection.Connection;
import com.github.manevolent.atlas.connection.ConnectionType;
import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.storage.ProjectStorageType;
import com.github.manevolent.atlas.settings.Setting;
import com.github.manevolent.atlas.settings.Settings;
import com.github.manevolent.atlas.ui.behavior.Edit;
import com.github.manevolent.atlas.ui.behavior.EditHistory;
import com.github.manevolent.atlas.ui.behavior.WindowAction;
import com.github.manevolent.atlas.ui.behavior.WindowHistory;
import com.github.manevolent.atlas.ui.component.footer.EditorFooter;
import com.github.manevolent.atlas.ui.component.menu.editor.FileMenu;
import com.github.manevolent.atlas.ui.component.menu.editor.WindowMenu;
import com.github.manevolent.atlas.ui.component.tab.*;
import com.github.manevolent.atlas.ui.component.toolbar.EditorToolbar;
import com.github.manevolent.atlas.ui.component.window.*;
import com.github.manevolent.atlas.ui.component.window.Window;
import com.github.manevolent.atlas.ui.dialog.DeviceSettingsDialog;
import com.github.manevolent.atlas.ui.settings.ProjectSettingsDialog;
import com.github.manevolent.atlas.ui.util.Devices;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;

/**
 *  This is the primary frame for the application
 *  It is launched via Main.java in this package
 */
public class Editor extends JFrame implements InternalFrameListener, MouseMotionListener, KeyListener {
    private static final Color splitPaneBorderColor = Color.GRAY.darker();

    // Desktop
    private JDesktopPane desktop;

    // Menus
    private JMenuBar menubar;
    private FileMenu fileMenu;
    private WindowMenu windowMenu;
    private EditorToolbar toolbar;
    private EditorFooter footer;

    // Tabs
    private TablesTab tablesTab;
    private ConsoleTab consoleTab;
    private ParametersTab parametersTab;
    private HelpTab helpTab;
    private FormatsTab formatsTab;

    // State variables (open windows, etc.)
    private File romFile;
    private Project project;
    private java.util.List<Window> openWindows = new ArrayList<>();
    private Map<Table, TableEditor> openedTables = new LinkedHashMap<>();
    private Map<Table, TableDefinitionEditor> openedTableDefs = new LinkedHashMap<>();
    private EditHistory editHistory;
    private WindowHistory windowHistory;
    private Window lastDeactivatedWindow;
    private Calibration activeCalibration;
    private boolean dirty;

    // Vehicle connection
    private Connection connection;

    public Editor(Project project) {
        // Just to make sure it shows up in the taskbar/dock/etc.
        setType(Type.NORMAL);

        openProject(null, project);
        setDirty(false);

        initComponents();

        pack();

        setBackground(Color.BLACK);
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        addKeyListener(this);

        Inputs.bind(this.getRootPane(),
                "left_keybind",
                () -> {
                    if (windowHistory.canUndo()) {
                        windowHistory.undo();
                        toolbar.update();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.META_DOWN_MASK), // OSX
                KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, InputEvent.CTRL_DOWN_MASK));
        Inputs.bind(this.getRootPane(),
                "right_keybind",
                () -> {
                    if (windowHistory.canRedo()) {
                        windowHistory.redo();
                        toolbar.update();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.META_DOWN_MASK), // OSX
                KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, InputEvent.CTRL_DOWN_MASK));
        Inputs.bind(this.getRootPane(),
                "newtable_keybind",
                this::newTable,
                KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getRootPane(),
                "datalogging_keybind",
                this::openDataLogging,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getRootPane(),
                "search_keybind",
                this::focusSearch,
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getRootPane(),
                "send_to_back",
                this::hideWindow,
                KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getRootPane(),
                "save",
                () -> this.saveProject(true),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getRootPane(),
                "newFormat",
                this::newFormat,
                KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getRootPane(),
                "newParameter",
                this::newParameter,
                KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK),
                KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.META_DOWN_MASK)); // OSX
        Inputs.bind(this.getRootPane(),
                "console",
                () -> consoleTab.focus(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0),
                KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0)); // OSX

        editHistory = new EditHistory();
        windowHistory = new WindowHistory();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
    }

    private void newParameter() {
        parametersTab.newParameter();
    }

    private void newFormat() {
        formatsTab.newFormat();
    }

    private void hideWindow() {
        desktop.moveToBack(desktop.getSelectedFrame());
    }

    private void focusSearch() {
        getTablesTab().focus();
        getTablesTab().focusSearch();
    }

    public Project getProject() {
        return project;
    }

    public Calibration getCalibration() {
        return activeCalibration;
    }

    public void setCalibration(Calibration calibration) {
        if (this.activeCalibration != calibration) {
            this.activeCalibration = calibration;

            //TODO reload stuff
        }
    }

    public void postStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            if (footer != null) {
                footer.setStatus(status);
                footer.getComponent().revalidate();
                footer.getComponent().repaint();
            }
        });
    }

    public Connection createConnection() {
        ConnectionType connectionType = getProject().getConnectionType();
        if (connectionType == null) {
            throw new IllegalArgumentException("Please set a connection type for this" +
                    " project so communication can be established.");
        }

        postStatus("Creating connection with CAN device...");
        Connection connection = connectionType.createConnection(Devices.getProvider());
        if (connection != null) {
            connection.setProject(getProject());
        }

        return connection;
    }

    public Connection getConnection() {
        if (connection == null && getProject() != null) {
            connection = createConnection();
        }

        if (connection != null) {
            postStatus("Opened connection with CAN device.");
        }

        return connection;
    }

    public void reestablishConnection() {
        try {
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
        } catch (IOException | TimeoutException e) {
            Log.can().log(Level.WARNING, "Problem disconnecting old connection", e);
        }

        try {
            connection = createConnection();
        } catch (Exception e) {
            Log.can().log(Level.WARNING, "Problem creating new connection", e);
        }

        Log.ui().log(Level.INFO, "Connection reestablished.");
    }

    /**
     *
     * @return true if the editor is ready to close
     */
    public boolean closing() {
        getOpenWindows().forEach(window -> window.getComponent().doDefaultCloseAction());

        if (dirty) {
            String message = "You have unsaved changes to your project " +
                    "that will be lost. Save changes?";

            int answer = JOptionPane.showConfirmDialog(getParent(),
                    message,
                    "Unsaved changes",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );

            switch (answer) {
                case JOptionPane.CANCEL_OPTION:
                    return false;
                case JOptionPane.YES_OPTION:
                    return saveProject(true);
                case JOptionPane.NO_OPTION:
            }
        }

        return getOpenWindows().isEmpty();
    }

    @Override
    public void setCursor(Cursor cursor) {
        super.setCursor(cursor);
        getContentPane().setCursor(cursor);
        openWindows.forEach(w -> w.getComponent().getContentPane().setCursor(cursor));
    }

    public void withWaitCursor(Runnable runnable) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            runnable.run();
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public <R> R withWaitCursor(Supplier<R> runnable) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            return runnable.get();
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public boolean saveProject(boolean existing) {
        postStatus("Saving project...");

        return withWaitCursor(() -> {
            File file;
            if (!existing || this.romFile == null || !romFile.exists()) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter def = new FileNameExtensionFilter("Atlas project files", "atlas");
                fileChooser.addChoosableFileFilter(def);
                fileChooser.setFileFilter(def);
                fileChooser.setDialogTitle("Save Project");
                fileChooser.setCurrentDirectory(romFile != null ? romFile.getParentFile() : null);
                if (fileChooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
                    file = fileChooser.getSelectedFile();
                } else {
                    return false;
                }
            } else {
                file = this.romFile;
            }

            try {
                ProjectStorageType.ZIP.getStorageFactory().createStorage().save(project, file);
                setDirty(false);
                String message = "Project saved to " + file.getPath();
                Log.ui().log(Level.INFO, message);
                postStatus(message);

                Settings.set(Setting.LAST_OPENED_PROJECT, file.getAbsolutePath());

                return true;
            } catch (Exception e) {
                postStatus("Project save failed; see console output for details.");
                JOptionPane.showMessageDialog(this, "Failed to save project!\r\nSee console output for more details.",
                        "Save failed",
                        JOptionPane.ERROR_MESSAGE);

                return false;
            }
        });
    }

    public void openProject() {
        if (!closing()) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter def = new FileNameExtensionFilter("Atlas project files (*.atlas)", "atlas");
        fileChooser.addChoosableFileFilter(def);
        fileChooser.setFileFilter(def);
        fileChooser.setDialogTitle("Open Project");
        fileChooser.setCurrentDirectory(romFile != null ? romFile.getParentFile() : null);
        if (fileChooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
                withWaitCursor(() -> {
                    try {
                        openProject(file, ProjectStorageType.ZIP.getStorageFactory().createStorage().load(file));
                    } catch (Exception e) {
                        postStatus("Open project failed; see console output for details.");
                        JOptionPane.showMessageDialog(this, "Failed to open project!\r\nSee console output for more details.",
                                "Open failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
                String message = "Project opened from " + file.getPath();
                Log.ui().log(Level.INFO, message);
                postStatus(message);
                Settings.set(Setting.LAST_OPENED_PROJECT, file.getAbsolutePath());
        }
    }

    public void openProject(File file, Project project) {
        this.project = project;
        this.activeCalibration = this.project.getCalibrations().stream().findFirst().orElse(null);

        this.romFile = file;

        updateTitle();
        if (parametersTab != null) {
            parametersTab.reinitialize();
        }
        if (formatsTab != null) {
            formatsTab.reinitialize();
        }
        if (tablesTab != null) {
            tablesTab.reinitialize();
        }
    }

    public void updateTitle() {
        String title;
        if (project.getVehicle() == null) {
            title = ("Atlas - Empty Project");
        } else {
            title = ("Atlas - " + project.getVehicle().toString());
        }

        if (dirty) {
            title += "*";
        }

        setTitle(title);
    }

    public JDesktopPane getDesktop() {
        return desktop;
    }

    public TablesTab getTablesTab() {
        return tablesTab;
    }

    public FormatsTab getFormatsTab() {
        return formatsTab;
    }

    public WindowMenu getWindowMenu() {
        return windowMenu;
    }

    public void updateWindowTitles() {
        updateTitle();
        for (Window window : openWindows) {
            window.updateTitle();
        }
    }

    private void initComponents() {
        setIconImage(Icons.getImage(CarbonIcons.METER_ALT, Color.WHITE).getImage());
        setJMenuBar(menubar = initMenu());

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });

        this.desktop = initDesktop();

        JSplitPane eastWestSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                initLeftPane(),
                this.desktop
        );

        JSplitPane northSouthSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                eastWestSplitPane,
                initBottomPane()
        );

        setLayout(new BorderLayout());

        add((toolbar = new EditorToolbar(this)).getComponent(), BorderLayout.NORTH);

        add(northSouthSplitPane, BorderLayout.CENTER);

        add((footer = new EditorFooter(this)).getComponent(), BorderLayout.SOUTH);

        addMouseMotionListener(this);
    }

    private JMenuBar initMenu() {
        JMenuBar menuBar;

        menuBar = new JMenuBar();

        fileMenu = new FileMenu(this);
        menuBar.add(fileMenu.getComponent());

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);
        {

        }

        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);
        {

        }

        JMenu tableMenu = new JMenu("Table");
        menuBar.add(tableMenu);
        {

        }

        windowMenu = new WindowMenu(this);
        menuBar.add(windowMenu.getComponent());

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        {

        }

        return menuBar;
    }

    public boolean hasWindow(Window window) {
        return openWindows.contains(window);
    }

    public Collection<Window> getOpenWindows() {
        return Collections.unmodifiableCollection(new ArrayList<>(openWindows));
    }

    public Collection<Window> getOpenWindows(Table table) {
        return getOpenWindows().stream()
                .filter(w -> ((w instanceof TableEditor) && ((TableEditor) w).getTable() == table) ||
                        ((w instanceof TableDefinitionEditor) && ((TableDefinitionEditor) w).getTable() == table))
                .toList();
    }

    /**
     * Opens a Window in the editor, keeping track of its state.
     * @param window Window to open.
     * @return opened Window.
     */
    public <T extends Window> T openWindow(T window) {
        Log.ui().log(Level.FINER, "Opening window \"" + window.getTitle() + "\" [" + window.getClass() + "]...");

        JInternalFrame component;
        try {
            component = window.getComponent();
        } catch (RuntimeException ex) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(getParent(), "Problem opening " + window.getTitle() + "!\r\n" +
                                ex.getMessage() + "\r\n"
                                + "See console output (F12) for more details.",
                        "Window Open Error",
                        JOptionPane.ERROR_MESSAGE);
            });
            Log.ui().log(Level.FINER, "Problem opening window \"" + window.getTitle() + "\", "
                    + window.getClass() + "]", ex);
            throw ex;
        }

        component.addInternalFrameListener(this);
        component.setFocusable(true);
        component.setVisible(true);

        openWindows.add(window);
        desktop.add(component);
        windowMenu.update();

        postStatus("Opened " + window.getTitle());
        Log.ui().log(Level.FINER, "Opened window \"" + window.getTitle() + "\" [" + window.getClass() + "].");
        return window;
    }

    public void newTable() {
        String suggestedTableName;
        TablesTab tab = tablesTab;
        Table selected = tab.getTable((TreeNode) tab.getTree().getLastSelectedPathComponent());
        if (selected != null && selected.getName().contains(" - ")) {
            String prefix = selected.getName().substring(0, selected.getName().lastIndexOf(" - "));
            suggestedTableName = prefix + " - New Table";
        } else {
            suggestedTableName = "New Table";
        }

        String newTableName = (String) JOptionPane.showInputDialog(getParent(), "Specify a name", "New Table",
                QUESTION_MESSAGE, null, null, suggestedTableName);

        if (newTableName == null || newTableName.isBlank()) {
            return;
        }

        Table table = Table.builder()
                .withName(newTableName)
                .withData(Series.builder()
                        .withAddress(getProject().getDefaultMemoryAddress())
                        .withLength(1)
                        .withScale(Scale.NONE))
                .build();

        tablesTab.focus();

        openTableDefinition(table);
    }

    public void openTable(Table table) {
        if (activeCalibration == null) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open table \"" + table.getName() + "\"!\r\nNo calibration has been selected. " +
                            "Please add a calibration using Project Settings before editing table data.",
                    "Open failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        TableEditor opened;

        opened = openedTables.get(table);

        if (opened == null) {
            opened = openWindow(new TableEditor(this, table));
        }

        opened.focus();

        openedTables.put(table, opened);
    }

    public void openTableDefinition(Table table) {
        if (activeCalibration == null) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open table \"" + table.getName() + "\"!\r\nNo calibration has been selected. " +
                            "Please add a calibration using Project Settings before editing table data.",
                    "Open failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        TableDefinitionEditor opened;

        opened = openedTableDefs.get(table);

        if (opened == null) {
            opened = openWindow(new TableDefinitionEditor(this, table));
        }

        opened.focus();

        openedTableDefs.put(table, opened);
    }

    public void openDeviceSettings() {
        DeviceSettingsDialog dialog = new DeviceSettingsDialog(this);
        dialog.setVisible(true);
    }

    public void openProjectSettings() {
        ProjectSettingsDialog dialog = new ProjectSettingsDialog(this, getProject());
        dialog.setVisible(true);
    }

    private JDesktopPane initDesktop() {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setMinimumSize(new Dimension(500, 500));
        return desktop;
    }

    private JPanel initLeftPane() {
        JPanel leftPane = new JPanel();
        leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
        leftPane.setMinimumSize(new Dimension(250, 0));
        leftPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                splitPaneBorderColor));

        TabbedPane leftTabs = new TabbedPane(this);
        leftTabs.addTab(initTablesTab(leftTabs.getComponent()));
        leftPane.add(leftTabs.getComponent());

        return leftPane;
    }

    private Tab initTablesTab(JTabbedPane tabbedPane) {
        return (tablesTab = new TablesTab(this, tabbedPane));
    }

    private JPanel initBottomPane() {
        JPanel bottomPane = new JPanel();
        bottomPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                splitPaneBorderColor));
        bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.Y_AXIS));

        TabbedPane tabbedPane = new TabbedPane(this);
        tabbedPane.addTab(initLoggingTab(tabbedPane.getComponent()));
        tabbedPane.addTab(initFormatsTab(tabbedPane.getComponent()));
        tabbedPane.addTab(initConsoleTab(tabbedPane.getComponent()));
        tabbedPane.addTab(initHelpTab(tabbedPane.getComponent()));
        bottomPane.add(tabbedPane.getComponent());

        return bottomPane;
    }

    private Tab initConsoleTab(JTabbedPane tabbedPane) {
       return (consoleTab = new ConsoleTab(this, tabbedPane));
    }

    private Tab initLoggingTab(JTabbedPane tabbedPane) {
        return (parametersTab = new ParametersTab(this, tabbedPane));
    }

    private Tab initFormatsTab(JTabbedPane tabbedPane) {
        return (formatsTab = new FormatsTab(this, tabbedPane));
    }

    private Tab initHelpTab(JTabbedPane tabbedPane) {
        return (helpTab = new HelpTab(this, tabbedPane));
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public Window getWindowByComponent(JInternalFrame internalFrame) {
        return openWindows.stream()
                .filter(x -> x.getComponent().equals(internalFrame))
                .findFirst().orElse(null);
    }

    public Window withWindowComponent(JInternalFrame internalFrame,
                                      Predicate<Window> predicate,
                                      Consumer<Window> action) {
        Window window = getWindowByComponent(internalFrame);

        if (window == null) {
            return null;
        }

        if (predicate.test(window)) {
            action.accept(window);
        }

        return window;
    }

    @SuppressWarnings("unchecked")
    public <W extends Window> W withWindowComponent(JInternalFrame internalFrame,
                                                    Class<W> windowClass,
                                                    Consumer<W> action) {
        Window window = getWindowByComponent(internalFrame);

        if (window == null) {
            return null;
        }

        if (!windowClass.isAssignableFrom(window.getClass())) {
            return null;
        }

        action.accept((W)window);

        return (W)window;
    }

    public Window withWindowComponent(JInternalFrame internalFrame,
                                      Consumer<Window> action) {
        return withWindowComponent(internalFrame, w -> true, action);
    }

    public EditHistory getEditHistory() {
        return editHistory;
    }

    public WindowHistory getWindowHistory() {
        return windowHistory;
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        withWindowComponent(e.getInternalFrame(), (window) -> {
            openWindows.remove(window);
            getWindowMenu().update();

            if (window instanceof TableEditor) {
                openedTables.remove(((TableEditor)window).getTable());
            } else if (window instanceof TableDefinitionEditor) {
                openedTableDefs.remove(((TableDefinitionEditor)window).getTable());
            }
        });
    }

    public void rememberEdit(Edit edit) {
        editHistory.remember(edit);
        toolbar.update();
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        Window window = getWindowByComponent(e.getInternalFrame());

        if (window != null && lastDeactivatedWindow != window && windowHistory.isRemembering()) {
            windowHistory.remember(new WindowAction(this, lastDeactivatedWindow, window));
            toolbar.update();
        }

        getWindowMenu().update();

        withWindowComponent(e.getInternalFrame(), TableEditor.class, (w) -> {
            tableFocused(w.getTable());
        });
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        if (windowHistory.isRemembering()) {
            lastDeactivatedWindow = getWindowByComponent(e.getInternalFrame());
        }
        getWindowMenu().update();
    }

    public void saveUiSettings() {

    }

    public void exit() {
        if (closing()) {
            saveUiSettings();
            dispose();
        }
    }

    public void tableFocused(Table table) {
        tablesTab.tableOpened(table);
    }

    public TableEditor getActiveTableEditor(Table realTable) {
        return openedTables.get(realTable);
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        updateTitle();
    }

    public void openDataLogging() {
        Window dataLoggingWindow = getOpenWindows().stream()
                .filter(w -> w instanceof DatalogWindow)
                .findFirst().orElse(null);

        if (dataLoggingWindow == null) {
            dataLoggingWindow = new DatalogWindow(this);
            openWindow(dataLoggingWindow);
        }

        dataLoggingWindow.focus();
    }

    public void openCanLogging() {
        Window canLoggingWindow = getOpenWindows().stream()
                .filter(w -> w instanceof CANDebugWindow)
                .findFirst().orElse(null);

        if (canLoggingWindow == null) {
            canLoggingWindow = new CANDebugWindow(this);
            openWindow(canLoggingWindow);
        }

        canLoggingWindow.focus();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (windowHistory.canUndo()) {
                windowHistory.undo();
                toolbar.update();
            }
        } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (windowHistory.canRedo()) {
                windowHistory.redo();
                toolbar.update();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
