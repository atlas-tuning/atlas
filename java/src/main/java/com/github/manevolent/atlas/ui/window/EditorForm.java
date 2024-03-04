package com.github.manevolent.atlas.ui.window;

import com.github.manevolent.atlas.definition.Rom;
import com.github.manevolent.atlas.definition.Table;
import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.component.menu.FileMenu;
import com.github.manevolent.atlas.ui.component.menu.WindowMenu;
import com.github.manevolent.atlas.ui.component.tab.*;
import com.github.manevolent.atlas.ui.component.window.TableWindow;
import com.github.manevolent.atlas.ui.component.window.Window;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EditorForm extends JFrame implements InternalFrameListener {
    private static final Color splitPaneBorderColor = Color.GRAY.darker();

    // Desktop
    private JDesktopPane desktop;

    // Menus
    private FileMenu fileMenu;
    private WindowMenu windowMenu;

    // Tabs
    private TablesTab tablesTab;
    private ProjectTab projectTab;
    private ConsoleTab consoleTab;
    private DataLoggingTab dataLoggingTab;
    private HelpTab helpTab;

    // State variables (open windows, etc.)
    private Rom rom;
    private java.util.List<Window> openWindows = new ArrayList<>();
    private Map<Table, TableWindow> openedTables = new LinkedHashMap<>();

    public EditorForm(Rom rom) {
        // Just to make sure it shows up in the taskbar/dock/etc.
        setType(Type.NORMAL);

        openRom(rom);

        initComponents();

        pack();

        setBackground(Color.BLACK);
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    public Rom getActiveRom() {
        return rom;
    }

    public void openRom(Rom rom) {
        this.rom = rom;
        setTitle("Atlas Tuning - " + rom.getVehicle().toString());
    }

    public JDesktopPane getDesktop() {
        return desktop;
    }

    public TablesTab getTablesTab() {
        return tablesTab;
    }

    public WindowMenu getWindowMenu() {
        return windowMenu;
    }

    private void initComponents() {
        setIconImage(Icons.getImage(CarbonIcons.METER_ALT, Color.WHITE).getImage());
        setJMenuBar(initMenu());

        this.desktop = initDesktop();

        JSplitPane eastWestSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                initLeftPane(),
                this.desktop
        );
        eastWestSplitPane.setDividerLocation(200);

        JSplitPane northSouthSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                eastWestSplitPane,
                initBottomPane()
        );
        northSouthSplitPane.setDividerLocation(507);

        add(northSouthSplitPane);

        setContentPane(northSouthSplitPane);
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

        JMenu vehicleMenu = new JMenu("Vehicle");
        menuBar.add(vehicleMenu);
        {
            JMenuItem connect = new JMenuItem("Connect...");
            vehicleMenu.add(connect);

            JMenuItem disconnect = new JMenuItem("Disconnect");
            disconnect.setEnabled(false);
            vehicleMenu.add(disconnect);
        }

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
        window.getComponent().addInternalFrameListener(this);
        window.getComponent().setFocusable(true);
        window.getComponent().setVisible(true);
        window.getComponent().addInternalFrameListener(this);

        openWindows.add(window);
        desktop.add(window.getComponent());
        windowMenu.update();

        return window;
    }

    public void openTable(Table table) {
        TableWindow opened;

        opened = openedTables.get(table);

        if (opened == null) {
            opened = openWindow(new TableWindow(this, table));
        }

        opened.focus();

        openedTables.put(table, opened);
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
        tabbedPane.addTab(initConsoleTab());
        tabbedPane.addTab(initLoggingTab());
        tabbedPane.addTab(initHelpTab());
        bottomPane.add(tabbedPane.getComponent());

        return bottomPane;
    }

    private Tab initConsoleTab() {
       return (consoleTab = new ConsoleTab(this));
    }

    private Tab initLoggingTab() {
        return (dataLoggingTab = new DataLoggingTab(this));
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

            if (window instanceof TableWindow) {
                openedTables.remove(((TableWindow)window).getTable());
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

        withWindowComponent(e.getInternalFrame(), TableWindow.class, (window) -> {
            tableFocused(window.getTable());
        });
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        getWindowMenu().update();
    }

    public void exit() {
        //TODO save before exit, etc.
        dispose();
    }

    public void tableFocused(Table table) {
        tablesTab.tableOpened(table);
    }
}
