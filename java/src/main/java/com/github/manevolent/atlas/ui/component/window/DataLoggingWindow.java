package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.connection.MemoryFrame;
import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.ui.EditorForm;
import com.github.manevolent.atlas.ui.Fonts;
import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.Layout;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.manevolent.atlas.ui.Fonts.getTextColor;

public class DataLoggingWindow extends Window implements
        MouseListener, MouseMotionListener, MouseWheelListener, InternalFrameListener {
    private final Set<MemoryParameter> activeParameters = new LinkedHashSet<>();
    private final Map<MemoryParameter, ParameterPanel> panelMap = new LinkedHashMap<>();
    private final List<MemoryFrame> frames = new CopyOnWriteArrayList<>();

    private JScrollPane scrollPane;
    private Integer cursorX;
    private Timer timer;
    private long windowWidthMillis = 60_000L;
    private long timeOffset = 0L;
    private Instant currentInstant = Instant.now();

    public DataLoggingWindow(EditorForm editor) {
        super(editor);
    }

    private JPanel initParameterPanel(MemoryParameter parameter) {
        return new ParameterPanel(parameter);
    }

    @Override
    protected void preInitComponent(JInternalFrame window) {
        window.addInternalFrameListener(this);
    }

    @Override
    protected void initComponent(JInternalFrame window) {
        window.setLayout(new BorderLayout());

        JPanel graphContainer = new JPanel(new GridBagLayout());

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
                currentInstant = Instant.now();
                super.paint(g);
            }
        };
        scrollPane.addMouseListener(this);
        scrollPane.addMouseMotionListener(this);
        scrollPane.addMouseWheelListener(this);
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

    private void startTimer() {
        if (timer == null) {
            Timer timer = new Timer("Paint");
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }
            }, 0L, 1000L / 30L);
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
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

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
        if (e.isShiftDown() || e.isControlDown()) {
            double rotation = e.getPreciseWheelRotation();
            rotation *= 20;
            windowWidthMillis += (int) rotation;
        }

        cursorX = e.getX();
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

    public Instant getRight() {
        return currentInstant;
    }

    class ParameterPanel extends JPanel {
        private final MemoryParameter parameter;
        private final Color textColor;

        ParameterPanel(MemoryParameter parameter) {
            this.parameter = parameter;
            this.textColor = new JPanel().getForeground();
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
            g.setColor(getBackground());
            g.fillRect(0, 0, bounds.width, bounds.height);

            Instant right = getRight();
            Instant left = getRight().minusMillis(windowWidthMillis);

            // Calculate avg, min, max, etc.
            float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
            float sum = 0f;
            int num = 0;
            for (int i = 0; i < frames.size(); i ++) {
                MemoryFrame frame = frames.get(i);
                Float value = frame.getValue(parameter);
                if (value == null)
                    continue; // Skip any null values (draw between)

                if (frame.getInstant().isBefore(left))
                    break; // End of array for us

                min = Math.min(min, value);
                max = Math.max(max, value);
                sum += value;
                num ++;
            }

            Color color;
            if (parameter.getColor() != null) {
                color = parameter.getColor().toAwtColor();
            } else {
                color = Color.WHITE;
            }
            g.setColor(color);

            // Draw
            float last_x = 0f, last_y = 0f;
            for (int i = 0; i < num; i ++) {
                MemoryFrame frame = frames.get(i);
                Float value = frame.getValue(parameter);
                if (value == null) continue;

                float x_ratio = ChronoUnit.MILLIS.between(left, frame.getInstant());
                x_ratio /= (float)windowWidthMillis;
                float y_ratio = (value - min) / (max - min);

                float x = x_ratio * (float)bounds.getWidth();
                float y = (float)bounds.getHeight() - ((float) (y_ratio * bounds.getHeight()));

                if (i > 0) {
                    Line2D.Float line = new Line2D.Float(last_x, last_y, x, y);
                    g2d.draw(line);
                }

                last_x = x;
                last_y = y;
            }

            g.setColor(textColor);
            g.setFont(Fonts.VALUE_FONT);
            g.drawString(parameter.getName(), 5, 15);

            g.setColor(getBackground().brighter());
            g.drawLine(0, bounds.height, bounds.width, bounds.height);

            g.setColor(Color.WHITE);
            if (cursorX != null) {
                g.drawLine(cursorX, 0, cursorX, bounds.height);
            }
        }
    }
}
