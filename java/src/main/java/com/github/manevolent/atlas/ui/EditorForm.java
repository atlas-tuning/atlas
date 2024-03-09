package com.github.manevolent.atlas.ui;

import com.github.manevolent.atlas.model.Rom;
import com.github.manevolent.atlas.model.Table;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.settings.Setting;
import com.github.manevolent.atlas.settings.Settings;
import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.component.footer.EditorFooter;
import com.github.manevolent.atlas.ui.component.menu.editor.FileMenu;
import com.github.manevolent.atlas.ui.component.menu.editor.VehicleMenu;
import com.github.manevolent.atlas.ui.component.menu.editor.WindowMenu;
import com.github.manevolent.atlas.ui.component.tab.*;
import com.github.manevolent.atlas.ui.component.window.DataLoggingWindow;
import com.github.manevolent.atlas.ui.component.window.TableDefinitionEditor;
import com.github.manevolent.atlas.ui.component.window.TableEditor;
import com.github.manevolent.atlas.ui.component.window.Window;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;

public class EditorForm extends JFrame implements InternalFrameListener {
    private static final Color splitPaneBorderColor = Color.GRAY.darker();

    // Desktop
    private JDesktopPane desktop;

    // Menus
    private FileMenu fileMenu;
    private WindowMenu windowMenu;
    private VehicleMenu vehicleMenu;

    // Tabs
    private TablesTab tablesTab;
    private ProjectTab projectTab;
    private ConsoleTab consoleTab;
    private ParametersTab parametersTab;
    private HelpTab helpTab;
    private FormatsTab formatsTab;

    // State variables (open windows, etc.)
    private File romFile;
    private Rom rom;
    private java.util.List<Window> openWindows = new ArrayList<>();
    private Map<Table, TableEditor> openedTables = new LinkedHashMap<>();
    private Map<Table, TableDefinitionEditor> openedTableDefs = new LinkedHashMap<>();
    private boolean dirty;

    public EditorForm(Rom rom) {
        // Just to make sure it shows up in the taskbar/dock/etc.
        setType(Type.NORMAL);

        openRom(null, rom);

        initComponents();

        pack();

        setBackground(Color.BLACK);
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
    }

    public Rom getActiveRom() {
        return rom;
    }

    /**
     *
     * @return true if the editor is ready to close
     */
    public boolean closing() {
        new ArrayList<>(getOpenWindows()).forEach(window -> window.getComponent().doDefaultCloseAction());

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
                    return saveRom();
                case JOptionPane.NO_OPTION:
            }
        }

        return getOpenWindows().isEmpty();
    }

    public boolean saveRom() {
        File file;
        if (this.romFile == null || !romFile.exists()) {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter def = new FileNameExtensionFilter("Atlas project files", "atlas");
            fileChooser.addChoosableFileFilter(def);
            fileChooser.setFileFilter(def);
            fileChooser.setDialogTitle("Save Project");
            if (fileChooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
            } else {
                return false;
            }
        } else {
            file = this.romFile;
        }

        try {
            rom.saveToArchive(file);
            setDirty(false);
            Log.ui().log(Level.INFO, "Project saved to " + file.getPath());
            Settings.set(Setting.LAST_OPENED_PROJECT, file.getAbsolutePath());

            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save project!\r\nSee console output for more details.",
                    "Save failed",
                    JOptionPane.ERROR_MESSAGE);

            return false;
        }
    }

    public void openRom() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter def = new FileNameExtensionFilter("Atlas project files (*.atlas)", "atlas");
        fileChooser.addChoosableFileFilter(def);
        fileChooser.setFileFilter(def);
        fileChooser.setDialogTitle("Open Project");
        if (fileChooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                openRom(file, Rom.loadFromArchive(file));
                Log.ui().log(Level.INFO, "Project opened from " + file.getPath());
                Settings.set(Setting.LAST_OPENED_PROJECT, file.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to open project!\r\nSee console output for more details.",
                        "Open failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void openRom(File file, Rom rom) {
        closing();

        this.rom = rom;
        this.romFile = file;

        updateTitle();
        if (parametersTab != null) {
            parametersTab.reinitialize();
        }
        if (formatsTab != null) {
            formatsTab.reinitialize();
        }
        if (projectTab != null) {
            projectTab.reinitialize();
        }
        if (tablesTab != null) {
            tablesTab.reinitialize();
        }
    }

    public void updateTitle() {
        String title;
        if (rom.getVehicle() == null) {
            title = ("Atlas - Empty Project");
        } else {
            title = ("Atlas - " + rom.getVehicle().toString());
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
        setJMenuBar(initMenu());

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
        add(northSouthSplitPane, BorderLayout.CENTER);
        add(new EditorFooter(this).getComponent(), BorderLayout.SOUTH);
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

        vehicleMenu = new VehicleMenu(this);
        menuBar.add(vehicleMenu.getComponent());

        windowMenu = new WindowMenu(this);
        menuBar.add(windowMenu.getComponent());

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        {

        }

        return menuBar;
    }

    public Collection<Window> getOpenWindows() {
        return Collections.unmodifiableCollection(openWindows);
    }

    /**
     * Opens a Window in the editor, keeping track of its state.
     * @param window Window to open.
     * @return opened Window.
     */
    public <T extends Window> T openWindow(T window) {
        Log.ui().log(Level.FINER, "Opening window \"" + window.getTitle() + "\" [" + window.getClass() + "]...");
        window.getComponent().addInternalFrameListener(this);
        window.getComponent().setFocusable(true);
        window.getComponent().setVisible(true);
        window.getComponent().addInternalFrameListener(this);

        openWindows.add(window);
        desktop.add(window.getComponent());
        windowMenu.update();

        Log.ui().log(Level.FINER, "Opened window \"" + window.getTitle() + "\" [" + window.getClass() + "].");
        return window;
    }

    public void openTable(Table table) {
        TableEditor opened;

        opened = openedTables.get(table);

        if (opened == null) {
            opened = openWindow(new TableEditor(this, table));
        }

        opened.focus();

        openedTables.put(table, opened);
    }

    public void openTableDefinition(Table table) {
        TableDefinitionEditor opened;

        opened = openedTableDefs.get(table);

        if (opened == null) {
            opened = openWindow(new TableDefinitionEditor(this, table));
        }

        opened.focus();

        openedTableDefs.put(table, opened);
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

        TabbedPane tabbedPane = new TabbedPane(this);
        tabbedPane.addTab(initProjectTab());
        tabbedPane.addTab(initTablesTab());
        leftPane.add(tabbedPane.getComponent());

        return leftPane;
    }

    private Tab initTablesTab() {
        return (tablesTab = new TablesTab(this));
    }

    private Tab initProjectTab() {
        return (projectTab = new ProjectTab(this));
    }

    private JPanel initBottomPane() {
        JPanel bottomPane = new JPanel();
        bottomPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                splitPaneBorderColor));
        bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.Y_AXIS));

        TabbedPane tabbedPane = new TabbedPane(this);
        tabbedPane.addTab(initLoggingTab());
        tabbedPane.addTab(initFormatsTab());
        tabbedPane.addTab(initConsoleTab());
        tabbedPane.addTab(initHelpTab());
        bottomPane.add(tabbedPane.getComponent());

        return bottomPane;
    }

    private Tab initConsoleTab() {
       return (consoleTab = new ConsoleTab(this));
    }

    private Tab initLoggingTab() {
        return (parametersTab = new ParametersTab(this));
    }

    private Tab initFormatsTab() {
        return (formatsTab = new FormatsTab(this));
    }

    private Tab initHelpTab() {
        return (helpTab = new HelpTab(this));
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

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        getWindowMenu().update();

        withWindowComponent(e.getInternalFrame(), TableEditor.class, (window) -> {
            tableFocused(window.getTable());
        });
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
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
                .filter(w -> w instanceof DataLoggingWindow)
                .findFirst().orElse(null);

        if (dataLoggingWindow == null) {
            dataLoggingWindow = new DataLoggingWindow(this);
            openWindow(dataLoggingWindow);
        }

        dataLoggingWindow.focus();
    }
}
