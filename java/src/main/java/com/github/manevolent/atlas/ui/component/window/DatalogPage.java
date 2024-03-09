package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.connection.MemoryFrame;
import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.ui.EditorForm;
import com.github.manevolent.atlas.ui.Inputs;
import com.github.manevolent.atlas.ui.Labels;
import com.github.manevolent.atlas.ui.Layout;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DatalogPage extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener,
        KeyListener {
    private final DatalogWindow window;
    private JPanel graphContainer;
    private JScrollPane scrollPane;
    private JPanel addPanel;
    private Integer cursorX;
    private long maximumHistoryMillis = 60_000L;
    private long windowWidthMillis = 60_000L;
    private long timeOffset = 0L;
    private Instant currentInstant = Instant.now();

    private boolean paused = true;
    private boolean dragging = false;
    private float drag_left, drag_right;

    private String name;

    private final Set<MemoryParameter> activeParameters = new LinkedHashSet<>();
    private final Map<MemoryParameter, DatalogParameterPanel> panelMap = new LinkedHashMap<>();
    private final List<MemoryFrame> frames = new CopyOnWriteArrayList<>();

    public DatalogPage(DatalogWindow window) {
        this.window = window;
        this.name = Instant.now().toString();

        initComponent();
    }

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return this.name;
    }

    public void activated() {
        window.getToolbar().setPaused(paused);
        window.getToolbar().setT(windowWidthMillis);
    }

    public void deactivated() {
        cursorX = null;
        drag_left = 0;
        drag_right = 0;
        dragging = false;
    }

    public Integer getCursorX() {
        return cursorX;
    }

    public boolean isDragging() {
        return dragging;
    }

    public float getDragLeft() {
        return drag_left;
    }

    public float getDragRight() {
        return drag_right;
    }

    private JPanel initParameterPanel(MemoryParameter parameter) {
        return new DatalogParameterPanel(this, parameter);
    }

    private void initAddPanel() {
        if (addPanel != null) {
            graphContainer.remove(addPanel);
        }

        if (isPaused()) {
            return;
        }

        addPanel = new JPanel();
        addPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        addPanel.add(Inputs.button(CarbonIcons.ADD, "Add parameter...", "Add a new parameter to this datalog",
                this::addParameter));

        graphContainer.add(addPanel,
                Layout.gridBagConstraints(
                        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                        0, activeParameters.size(),
                        1, 1));
    }

    public EditorForm getEditor() {
        return window.getParent();
    }

    public List<MemoryParameter> getMissingParameters() {
        return getEditor().getActiveRom().getParameters().stream()
                .filter(p -> !activeParameters.contains(p))
                .toList();
    }

    public Collection<MemoryParameter> getActiveParameters() {
        return activeParameters;
    }

    public void addParameter(MemoryParameter parameter) {
        if (addPanel != null) {
            graphContainer.remove(addPanel);
        }

        graphContainer.add(initParameterPanel(parameter),
                Layout.gridBagConstraints(
                        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                        0, activeParameters.size(),
                        1, 1));

        activeParameters.add(parameter);

        initAddPanel();
    }

    public void addParameter() {

        Object[] options = getMissingParameters().toArray();

        MemoryParameter selected = (MemoryParameter) JOptionPane.showInputDialog(
                getEditor(),
                "Select a parameter to add",
                "Select Parameter",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                null
        );

        if (selected != null) {
            addParameter(selected);
        }
    }

    public void initComponent() {
        graphContainer = new JPanel(new GridBagLayout());

        initAddPanel();

        scrollPane = new JScrollPane(graphContainer) {
            @Override
            public void paint(Graphics g) {
                if (!paused) {
                    setWindowWidthMillis(windowWidthMillis);
                    setCurrentInstant(Instant.now());
                }

                super.paint(g);
            }

            @Override
            protected void processMouseEvent(MouseEvent e) {
                if (!e.isControlDown() && !e.isShiftDown() && !e.isAltDown() && !e.isMetaDown() &&
                        !e.isAltGraphDown()) {
                    super.processMouseEvent(e);
                }
            }
        };

        scrollPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    scrollPane.setWheelScrollingEnabled(false);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (!e.isControlDown()) {
                    scrollPane.setWheelScrollingEnabled(true);
                }
            }
        });

        scrollPane.addMouseListener(this);
        scrollPane.addMouseMotionListener(this);
        scrollPane.addMouseWheelListener(this);

        addKeyListener(this);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addFrame(MemoryFrame frame) {
        if (paused) {
            return;
        }

        currentInstant = frame.getInstant();

        Instant left = getLeft();
        if (frame.getInstant().isBefore(left)) {
            return;
        }

        Instant historyLeft = getHistoryLeft();
        while (!frames.isEmpty() && frames.getLast().getInstant().isBefore(historyLeft)) {
            frames.removeLast();
        }

        frames.add(0, frame);
    }

    public Instant getTime(float x) {
        long millis = (long) (windowWidthMillis - (windowWidthMillis * (x / graphContainer.getWidth())));
        return getRight().minusMillis(millis);
    }

    private void select() {
        setPaused(true);

        Instant left = getTime(Math.min(drag_left, drag_right));
        Instant right = getTime(Math.max(drag_left, drag_right));

        long widthMillis = right.toEpochMilli() - left.toEpochMilli();
        if (widthMillis < 500L) {
            return;
        }

        setWindowWidthMillis(widthMillis);
        setCurrentInstant(right);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragging) {
            dragging = false;
            select();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        cursorX = e.getX();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        cursorX = null;
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            if (drag_right != drag_left || Math.abs(drag_left - e.getX()) >= 25) {
                drag_right = e.getX();
            } else {
                drag_right = drag_left;
            }
        }
        if (!dragging) {
            drag_right = e.getX();
            drag_left = e.getX();
            dragging = true;
        }

        cursorX = e.getX();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        cursorX = e.getX();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        cursorX = e.getX();

        double rotation = e.getPreciseWheelRotation();
        rotation *= 20;

        if (paused) {
            if (e.isShiftDown()) {
                setCurrentInstant(currentInstant.plusMillis((int) rotation));
            } else if (e.isControlDown()) {
                setWindowWidthMillis(windowWidthMillis + (int) rotation);
                e.consume();
            } else {
                e.consume();
            }
        } else {
            setWindowWidthMillis(getMaximumWindowWidthMillis());
        }

        scrollPane.revalidate();
        scrollPane.repaint();
    }

    public void setPaused(boolean paused) {
        if (this.paused == paused) {
            return;
        }

        this.paused = paused;

        if (paused && window.getRecordingPage() == this) {
            window.stopRecording();
        }

        initAddPanel();
        window.getToolbar().setPaused(paused);
    }

    public boolean isPaused() {
        return this.paused;
    }

    private Instant getHistoryLeft() {
        return getRight().minusMillis(maximumHistoryMillis);
    }

    public long getMaximumWindowWidthMillis() {
        if (frames.size() > 1) {
            Instant latestInstant = frames.getFirst().getInstant();
            Instant earliestInstant = frames.getLast().getInstant();
            return latestInstant.toEpochMilli() - earliestInstant.toEpochMilli();
        } else {
            return 10_000L;
        }
    }

    public List<MemoryFrame> getFrames() {
        return frames;
    }

    public long getWindowWidthMillis() {
        return windowWidthMillis;
    }

    public Instant getLeft() {
        return getRight().minusMillis(windowWidthMillis);
    }

    public Instant getRight() {
        return currentInstant;
    }


    public long setWindowWidthMillis(long millis) {
        millis = Math.max(1_000, millis);
        millis = Math.min(getMaximumWindowWidthMillis(), millis);
        this.windowWidthMillis = millis;
        window.getToolbar().setT(millis);
        return millis;
    }

    public void setCurrentInstant(Instant instant) {
        if (paused && frames.size() > 1) {
            Instant latestInstant = frames.getFirst().getInstant();
            Instant earliestInstant = frames.getLast().getInstant();
            Instant earliestPossibleInstant = earliestInstant.plusMillis(windowWidthMillis);
            if (instant.isAfter(latestInstant)) {
                instant = latestInstant;
            } if (instant.isBefore(earliestPossibleInstant)) {
                instant = earliestPossibleInstant;
            }
        }
        currentInstant = instant;
    }

    public void fitToScreen() {
        if (frames.size() > 1) {
            setWindowWidthMillis(frames.getFirst().getInstant().toEpochMilli() -
                    frames.getLast().getInstant().toEpochMilli());
            setCurrentInstant(frames.getFirst().getInstant());
        }
    }

    public void moveLeft() {
        setPaused(true);
        setCurrentInstant(currentInstant.minusMillis(windowWidthMillis / 4));
    }

    public void moveRight() {
        setPaused(true);
        setCurrentInstant(currentInstant.plusMillis(windowWidthMillis / 4));
    }

    public void zoomIn() {
        setWindowWidthMillis(windowWidthMillis - windowWidthMillis / 4);
    }

    public void zoomOut() {
        setWindowWidthMillis(windowWidthMillis + windowWidthMillis / 4);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_KP_LEFT || e.getKeyCode() == KeyEvent.VK_LEFT) {
            moveLeft();
        } else if (e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            moveRight();
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            fitToScreen();
        } else if (e.getKeyCode() == KeyEvent.VK_P) {
            setPaused(true);
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            setPaused(!this.isPaused());
        } else if (e.getKeyCode() >= KeyEvent.VK_0 || e.getKeyCode() <= KeyEvent.VK_9) {
            if (frames.size() > 1) {
                setPaused(true);

                int nth;
                if (e.getKeyCode() == KeyEvent.VK_0) {
                    nth = 0;
                } else {
                    nth = Integer.parseInt(Character.toString(e.getKeyChar()));
                }
                float zoomMillis = getMaximumWindowWidthMillis() / 10f;
                zoomMillis = setWindowWidthMillis((long) zoomMillis);
                long leftOffsetMillis = (long) (zoomMillis * nth);
                long rightOffsetMillis = (long) leftOffsetMillis + (long) zoomMillis;
                Instant earliestInstant = frames.getLast().getInstant();
                setCurrentInstant(earliestInstant.plusMillis(leftOffsetMillis));
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}