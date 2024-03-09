package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.connection.MemoryFrame;
import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.ui.Colors;
import com.github.manevolent.atlas.ui.Fonts;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Represents a single horizontal parameter panel in the datalog viewer.
 * This is related 1:1 with a given data parameter/series.
 */
public class DatalogParameterPanel extends JPanel {
    private static final Color textBackgroundColor = Colors.withAlpha(Color.BLACK, 180);
    private static final Color lineColor = Colors.withAlpha(Color.WHITE, 160);
    private static final Stroke graphStroke = new BasicStroke(2f);
    private static final Stroke breakStroke = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            0, new float[]{4}, 0);

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

    private final DatalogPage page;
    private final MemoryParameter parameter;
    private final Color textColor;

    public DatalogParameterPanel(DatalogPage page, MemoryParameter parameter) {
        this.page = page;
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

        java.util.List<MemoryFrame> frames = page.getFrames();

        float precision = parameter.getScale().getPrecision();
        boolean isDecimal = precision % 1 != 0;
        String valueFormat = isDecimal ? "%.2f" : "%.0f";

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

        if (mousePosition != null && page.getActiveParameters().size() > 1) {
            g.setColor(getBackground());
        } else {
            g.setColor(getBackground().darker());
        }

        g.fillRect(0, 0, bounds.width, bounds.height);

        if (page.isDragging()) {
            g.setColor(Colors.withAlpha(getBackground().brighter(), 180));

            if (page.getDragLeft() <= page.getDragRight()) {
                g.fillRect((int) page.getDragLeft(), 0, (int) (page.getDragRight() - page.getDragLeft()), bounds.height);
            } else {
                g.fillRect((int) page.getDragRight(), 0, (int) (page.getDragLeft() - page.getDragRight()), bounds.height);
            }
        }

        Instant right = page.getRight();
        Instant left = page.getLeft();

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
            x_ratio /= (float)page.getWindowWidthMillis();
            float y_ratio = (value - min) / (max - min);

            float x = x_ratio * (float)bounds.getWidth();
            float y = (float)bounds.getHeight() - ((float) (y_ratio * bounds.getHeight()));

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
                if (page.getCursorX() != null && page.getCursorX() <= last_x && page.getCursorX() >= x) {
                    float ratio = (page.getCursorX() - x) / (last_x - x);
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
            drawText(bounds, g2d, labelColor, page.getCursorX() + 2, cursor_y, String.format(valueFormat +
                    parameter.getScale().getUnit().getText(), value_y));
        }

        g2d.setFont(Fonts.bold(Fonts.VALUE_FONT));
        drawText(bounds, g2d, labelColor, 5, 5, parameter.getName());

        g2d.setFont(Fonts.VALUE_FONT);
        if (max != -Float.MAX_VALUE) {
            drawText(bounds, g2d, Colors.withAlpha(Color.GREEN, labelColor.getAlpha()), 5, 30,
                    String.format("Max: " + valueFormat + parameter.getScale().getUnit().getText(), max));
        }

        if (min != Float.MAX_VALUE) {
            drawText(bounds, g2d, Colors.withAlpha(Color.RED, labelColor.getAlpha()), 5, 55,
                    String.format("Min: " + valueFormat + parameter.getScale().getUnit().getText(), min));
        }

        g.setColor(getBackground().brighter());
        g2d.setStroke(breakStroke);
        g.drawLine(0, bounds.height, bounds.width, bounds.height);

        g2d.setStroke(graphStroke);

        g.setColor(lineColor);
        if (page.getCursorX() != null) {
            g.drawLine(page.getCursorX(), 0, page.getCursorX(), bounds.height);
        }
    }
}