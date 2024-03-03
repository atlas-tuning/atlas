package com.github.manevolent.atlas.ui.window;

import com.github.manevolent.atlas.definition.Rom;
import com.github.manevolent.atlas.definition.Table;
import com.github.manevolent.atlas.ui.component.tab.*;
import com.github.manevolent.atlas.ui.component.window.TableWindow;
import com.github.manevolent.atlas.ui.component.window.Window;
import net.codecrete.usb.windows.Win;

import javax.swing.*;
import java.awt.*;

public class EditorForm extends JFrame {
    private static final Color splitPaneBorderColor = Color.GRAY.darker();

    private JDesktopPane desktop;
    private Rom rom;

    private TablesTab tablesTab;
    private ProjectTab projectTab;
    private ConsoleTab consoleTab;
    private DataLoggingTab dataLoggingTab;
    private HelpTab helpTab;

    public EditorForm(Rom rom) {
        openRom(rom);

        initComponents();

        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);

        setBackground(Color.BLACK);

        pack();
    }

    public Rom getActiveRom() {
        return rom;
    }

    public void openRom(Rom rom) {
        this.rom = rom;
        setTitle("Atlas Tuning - " + rom.getVehicle().toString());
    }

    private void initComponents() {
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

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        {
            JMenuItem newRom = new JMenuItem("New ROM...");
            newRom.addActionListener((e) -> {
                NewRomForm newRomForm = new NewRomForm();
                newRomForm.setVisible(true);
            });
            fileMenu.add(newRom);

            JMenuItem openRom = new JMenuItem("Open ROM...");
            fileMenu.add(openRom);

            JMenuItem recentRoms = new JMenu("Recent ROMs");

            //TODO: Recent roms
            JMenuItem noRecentRoms = new JMenuItem("No recent ROMs");
            noRecentRoms.setEnabled(false);
            recentRoms.add(noRecentRoms);

            fileMenu.add(recentRoms);

            fileMenu.addSeparator();

            JMenuItem saveRom = new JMenuItem("Save ROM");
            saveRom.setEnabled(false);
            fileMenu.add(saveRom);

            fileMenu.addSeparator();
            JMenuItem exit = new JMenuItem("Exit");
            exit.addActionListener((e) -> {
                EditorForm.this.dispose();
            });
            fileMenu.add(exit);
        }

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

        JMenu windowMenu = new JMenu("Window");
        menuBar.add(windowMenu);
        {

        }

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
        {

        }

        return menuBar;
    }

    /**
     * Opens a Window in the editor, keeping track of its state.
     * @param window Window to open.
     * @return opened Window.
     */
    public Window openWindow(Window window) {
        //TODO track state

        window.getComponent().setFocusable(true);
        window.getComponent().setVisible(true);

        desktop.add(window.getComponent());

        return window;
    }

    public void openTable(Table table) {
        Window opened;

        //TODO don't reopen the same table

        opened = openWindow(new TableWindow(this, table));

        desktop.moveToFront(opened.getComponent());
        opened.getComponent().grabFocus();

        tablesTab.tableOpened(table);
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

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Project", initProjectTab());
        tabbedPane.addTab("Tables", initTablesTab());
        leftPane.add(tabbedPane);

        return leftPane;
    }

    private Component initTablesTab() {
        return (tablesTab = new TablesTab(this)).getComponent();
    }

    private Component initProjectTab() {
        return (projectTab = new ProjectTab(this)).getComponent();
    }

    private JPanel initBottomPane() {
        JPanel bottomPane = new JPanel();
        bottomPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                splitPaneBorderColor));
        bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.Y_AXIS));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Console", initConsoleTab());
        tabbedPane.addTab("Data Logging", initLoggingTab());
        tabbedPane.addTab("Help", initHelpTab());
        bottomPane.add(tabbedPane);

        return bottomPane;
    }

    private JPanel initConsoleTab() {
       return (consoleTab = new ConsoleTab(this)).getComponent();
    }

    private JPanel initLoggingTab() {
        return (dataLoggingTab = new DataLoggingTab(this)).getComponent();
    }

    private JPanel initHelpTab() {
        return (helpTab = new HelpTab(this)).getComponent();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
