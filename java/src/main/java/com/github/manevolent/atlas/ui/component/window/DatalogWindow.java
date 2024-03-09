package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.connection.MemoryFrame;
import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.ui.*;
import com.github.manevolent.atlas.ui.component.menu.datalog.FileMenu;
import com.github.manevolent.atlas.ui.component.menu.datalog.ViewMenu;
import com.github.manevolent.atlas.ui.component.toolbar.DataLoggingToolbar;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.manevolent.atlas.ui.Fonts.getTextColor;

public class DatalogWindow extends Window implements
        MouseListener, MouseMotionListener, MouseWheelListener, InternalFrameListener, KeyListener {
    private final Set<MemoryParameter> activeParameters = new LinkedHashSet<>();
    private final Map<MemoryParameter, ParameterPanel> panelMap = new LinkedHashMap<>();
    private final List<MemoryFrame> frames = new CopyOnWriteArrayList<>();

    private static final Color textBackgroundColor = Colors.withAlpha(Color.BLACK, 180);
    private static final Color lineColor = Colors.withAlpha(Color.WHITE, 160);
    private static final Stroke graphStroke = new BasicStroke(2f);
    private static final Stroke breakStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            0, new float[]{4}, 0);

    private JPanel graphContainer;
    private DataLoggingToolbar toolbar;
    private JScrollPane scrollPane;
    private Integer cursorX;
    private Timer timer;
    private long maximumHistoryMillis = 5_000L;
    private long windowWidthMillis = 60_000L;
    private long timeOffset = 0L;
    private Instant currentInstant = Instant.now();

    private boolean paused = false;
    private boolean dragging = false;
    private float drag_left, drag_right;
    private FileMenu fileMenu;

    public DatalogWindow(EditorForm editor) {
        super(editor);
    }

    private JPanel initParameterPanel(MemoryParameter parameter) {
        return new ParameterPanel(parameter);
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

    @Override
    protected void initComponent(JInternalFrame window) {
        window.setLayout(new BorderLayout());
        window.add((toolbar = new DataLoggingToolbar(this)).getComponent(), BorderLayout.NORTH);
        window.setJMenuBar(initMenu());

        graphContainer = new JPanel(new GridBagLayout());

        int panelIndex = 0;
        for (MemoryParameter parameter : getParent().getActiveRom().getParameters()) {
            graphContainer.add(initParameterPanel(parameter),
                    Layout.gridBagConstraints(GridBagConstraints.WEST,
                            GridBagConstraints.HORIZONTAL, 0, panelIndex, 1, 1));
            panelIndex++;
        }

        scrollPane = new JScrollPane(graphContainer) {
            @Override
            public void paint(Graphics g) {
                if (!paused) {
                    currentInstant = Instant.now();
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
        window.addKeyListener(this);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        window.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return "Data Logging";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.CHART_AVERAGE, getTextColor());
    }

    @Override
    public void reload() {

    }

    private void debugging_generate_data() {
        activeParameters.addAll(getParent().getActiveRom().getParameters());
        double millis = (System.currentTimeMillis() % 1000) / 1000D;
        byte data = (byte) ((int)(millis * 255) & 0xFF);
        MemoryFrame frame = new MemoryFrame();
        for (MemoryParameter parameter : activeParameters) {
            byte[] bytes = new byte[parameter.getScale().getFormat().getSize()];
            Arrays.fill(bytes, data);
            frame.setData(parameter, bytes);
        }
        addFrame(frame);
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

    private void startTimer() {
        if (timer == null) {
            Timer timer = new Timer("Paint");
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    debugging_generate_data();
                    scrollPane.revalidate();
                    scrollPane.repaint();
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

    public Instant getTime(float x) {
        long millis = (long) (windowWidthMillis - (windowWidthMillis * (x / graphContainer.getWidth())));
        return getRight().minusMillis(millis);
    }

    private void select() {
        paused = true;

        Instant left = getTime(Math.min(drag_left, drag_right));
        Instant right = getTime(Math.max(drag_left, drag_right));

        long widthMillis = right.toEpochMilli() - left.toEpochMilli();
        if (widthMillis < 500L) {
            return;
        }

        currentInstant = right;
        windowWidthMillis = Math.max(1_000, widthMillis);
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

        if (paused && e.isShiftDown()) {
            currentInstant = currentInstant.plusMillis((int) rotation);
        } else if (e.isControlDown()) {
            windowWidthMillis += (int) rotation;
            windowWidthMillis = Math.max(1_000, windowWidthMillis);
            e.consume();
        } else {
            e.consume();
        }

        scrollPane.revalidate();
        scrollPane.repaint();
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

    private Instant getHistoryLeft() {
        return getRight().minusMillis(maximumHistoryMillis);
    }

    private Instant getLeft() {
        return getRight().minusMillis(windowWidthMillis);
    }

    public Instant getRight() {
        return currentInstant;
    }

    private static void drawText(Rectangle outerBounds,
                                 Graphics2D g2d, Color textColor, float x, float y, String text) {
        FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
        Rectangle2D bounds = metrics.getStringBounds(text, g2d);
        int overshoot = metrics.getMaxAscent() - metrics.getAscent();

        x = Math.max(0, x);
        y = Math.max(0, y);

        x = Math.min(x, outerBounds.width - ((float)bounds.getWidth() + 6));
        y = Math.min(y, outerBounds.height - ((float)bounds.getHeight() + 6));

        Rectangle2D rectangle2D = new Rectangle2D.Float(
                x - 3,
                y + metrics.getMaxDescent() - 3,
                (float)bounds.getWidth() + 6,
                (float)bounds.getHeight() + 6);

        g2d.setColor(textBackgroundColor);
        g2d.fill(rectangle2D);

        g2d.setColor(textColor);
        g2d.drawString(text, (int)x, (int)y+(int)bounds.getHeight());
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_KP_LEFT || e.getKeyCode() == KeyEvent.VK_LEFT) {
            paused = true;
            currentInstant = currentInstant.minusMillis(windowWidthMillis / 4);
        } else if (e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            paused = true;
            currentInstant = currentInstant.plusMillis(windowWidthMillis / 4);
        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            if (frames.size() > 1) {
                paused = true;
                currentInstant = frames.getFirst().getInstant();
                windowWidthMillis = frames.getFirst().getInstant().toEpochMilli() -
                        frames.getLast().getInstant().toEpochMilli();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_P) {
            paused = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    class ParameterPanel extends JPanel {
        private final MemoryParameter parameter;
        private final Color textColor;

        ParameterPanel(MemoryParameter parameter) {
            this.parameter = parameter;

            var color = parameter.getColor();
            Color fullColor;
            if (color == null) {
                fullColor = Color.WHITE;
            } else {
                fullColor = color.toAwtColor();
            }

            textColor = Colors.withAlpha(fullColor, 200);
            initComponent();
        }

        private void initComponent() {
            setMinimumSize(new Dimension(256, 100));
            setPreferredSize(new Dimension(256, 100));
            setBackground(getBackground().darker());
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            setFont(Fonts.VALUE_FONT);

            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            Rectangle bounds = getBounds();
            Point mousePosition = getMousePosition();
            if (mousePosition != null) {
                g.setColor(getBackground());
            } else {
                g.setColor(getBackground().darker());
            }
            g.fillRect(0, 0, bounds.width, bounds.height);

            if (dragging) {
                g.setColor(Colors.withAlpha(getBackground().brighter(), 180));

                if (drag_left <= drag_right) {
                    g.fillRect((int) drag_left, 0, (int) (drag_right - drag_left), bounds.height);
                } else {
                    g.fillRect((int) drag_right, 0, (int) (drag_left - drag_right), bounds.height);
                }
            }

            Instant right = getRight();
            Instant left = getLeft();

            // Calculate avg, min, max, etc.
            float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
            float sum = 0f;
            int num = 0;
            for (int i = 0; i < frames.size(); i ++) {
                MemoryFrame frame = frames.get(i);
                Float value = frame.getValue(parameter);
                if (value == null)
                    continue; // Skip any null values (draw between)

                min = Math.min(min, value);
                max = Math.max(max, value);
                sum += value;
                num ++;

                if (frame.getInstant().isBefore(left)) {
                    break;
                }
            }

            if (min == max) {
                max = min + 1;
            }

            Color color;
            if (parameter.getColor() != null) {
                color = parameter.getColor().toAwtColor(160);
            } else {
                color = new Color(255, 255, 255, 160);
            }
            g.setColor(color);

            // Draw
            Path2D.Float path = new Path2D.Float();
            float last_x = 0f, last_y = 0f;
            float cursor_y = 0f;
            float last_value_y = 0f;
            Float value_y = null;
            for (int i = 0; i < num && i < frames.size(); i ++) {
                MemoryFrame frame = frames.get(i);
                Float value = frame.getValue(parameter);
                if (value == null) continue;

                float x_ratio = ChronoUnit.MILLIS.between(left, frame.getInstant());
                x_ratio /= (float)windowWidthMillis;
                float y_ratio = (value - min) / (max - min);

                float x = x_ratio * (float)bounds.getWidth();
                float y = (float)bounds.getHeight() - ((float) (y_ratio * bounds.getHeight()));

                if (i == 0) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                    if (cursorX != null && cursorX <= last_x && cursorX >= x) {
                        float ratio = (cursorX - x) / (last_x - x);
                        value_y = value + ((last_value_y - value) * ratio);
                        cursor_y = y + ((last_y - y) * ratio);
                    }
                }

                last_x = x;
                last_y = y;
                last_value_y = value;
            }

            if (mousePosition != null) {
                g2d.setColor(Colors.withAlpha(textColor, 255));
            }

            g2d.draw(path);

            Color labelColor;
            if (mousePosition != null) {
                labelColor = Colors.withAlpha(textColor, 255);
            } else {
                labelColor = Colors.withAlpha(textColor, 185);
            }

            if (value_y != null) {
                drawText(bounds, g2d, labelColor, cursorX + 2, cursor_y, String.format("%.2f" +
                        parameter.getScale().getUnit().getText(), value_y));
            }

            drawText(bounds, g2d, labelColor, 5, 5, parameter.getName());;

            drawText(bounds, g2d, Colors.withAlpha(Color.GREEN, labelColor.getAlpha()), 5, 30, String.format("Max: %.2f" +
                    parameter.getScale().getUnit().getText(), max));
            drawText(bounds, g2d, Colors.withAlpha(Color.RED, labelColor.getAlpha()), 5, 55, String.format("Min: %.2f" +
                    parameter.getScale().getUnit().getText(), min));

            g.setColor(getBackground().brighter());
            g2d.setStroke(breakStroke);
            g.drawLine(0, bounds.height, bounds.width, bounds.height);

            g2d.setStroke(graphStroke);

            g.setColor(lineColor);
            if (cursorX != null) {
                g.drawLine(cursorX, 0, cursorX, bounds.height);
            }
        }
    }
}
