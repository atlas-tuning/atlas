package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.ui.*;
import com.github.manevolent.atlas.ui.component.menu.datalog.FileMenu;
import com.github.manevolent.atlas.ui.component.menu.datalog.ViewMenu;
import com.github.manevolent.atlas.ui.component.toolbar.DatalogToolbar;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

import static com.github.manevolent.atlas.ui.Fonts.getTextColor;

public class DatalogWindow extends Window implements InternalFrameListener, ChangeListener {
    private FileMenu fileMenu;

    private DatalogToolbar toolbar;
    private JTabbedPane tabbedPane;

    private Timer timer;

    private List<DatalogPage> pages = new ArrayList<>();
    private DatalogPage activePage;
    private DatalogPage recordingPage;

    public DatalogWindow(EditorForm editor) {
        super(editor);
    }

    @Override
    protected void preInitComponent(JInternalFrame window) {
        super.preInitComponent(window);
        window.addInternalFrameListener(this);
    }

    private JMenuBar initMenu() {
        JMenuBar menuBar;

        menuBar = new JMenuBar();

        menuBar.add((fileMenu = new FileMenu(this)).getComponent());
        menuBar.add(new ViewMenu(this).getComponent());

        return menuBar;
    }

    public DatalogPage getRecordingPage() {
        return recordingPage;
    }

    public void setRecordingPage(DatalogPage page) {
        if (page != null) {
            toolbar.setPaused(false);
        } else {
            toolbar.setPaused(true);
        }

        this.recordingPage = page;

        if (page != null) {
            page.setPaused(false);
        }

        updateTitle();
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
        tabbedPane.addTab(page.getTitle(), page);
        tabbedPane.setSelectedComponent(page);
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }

    public void toggleRecording() {
        if (recordingPage != null) {
            stopRecording();
        } else {
            DatalogPage page = new DatalogPage(this);
            addPage(page);
            setRecordingPage(page);
        }
    }

    public void stopRecording() {
        if (recordingPage != null) {
            recordingPage.setPaused(true);
            recordingPage = null;
        }
    }

    @Override
    protected void initComponent(JInternalFrame window) {
        window.setLayout(new BorderLayout());
        window.add((toolbar = new DatalogToolbar(this)).getComponent(), BorderLayout.NORTH);
        window.setJMenuBar(initMenu());

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
        return Icons.get(CarbonIcons.CHART_AVERAGE, getTextColor());
    }

    @Override
    public void reload() {
        updateTitle();
    }

    public DatalogToolbar getToolbar() {
        return toolbar;
    }

    private void startTimer() {
        if (timer == null) {
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
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
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
}
