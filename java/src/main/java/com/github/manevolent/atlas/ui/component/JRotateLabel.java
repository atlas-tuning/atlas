
package com.github.manevolent.atlas.ui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class JRotateLabel extends JPanel {
    private static final long serialVersionUID = 0L;

    private static Font _defaultFont;
    {
        if (_defaultFont == null) {
            _defaultFont = new JLabel("text").getFont();
            if (_defaultFont == null) _defaultFont = getFont();
        }
        setFont(_defaultFont);
        addPropertyChangeListener("font", e -> remeasure());
    }

    private boolean _faceLeft;
    private Dimension _size;
    private String _text;

    /**
     * Creates a {@link JRotateLabel} with no initial text.
     */
    public JRotateLabel() {
        setText(null);
    }

    /**
     * Creates a {@link JRotateLabel} with the specified initial text.
     * @param text the text to display
     */
    public JRotateLabel(String text) {
        setText(text);
    }

    /**
     * Creates a {@link JRotateLabel} with the specified initial text and facing.
     * @param text the text to display
     * @param faceLeft {@code true} if the baseline of the {@link JRotateLabel}
     *                 should face left, {@code false} if it should face right
     */
    public JRotateLabel(String text, boolean faceLeft) {
        _faceLeft = faceLeft;
        setText(text);
    }

    /**
     * Indicates whether the {@link JRotateLabel} is facing left.
     * The default is {@code false}.
     * @return {@code true} if the baseline of the {@link JRotateLabel}
     *         is facing left, {@code false} if it is facing right
     */
    public boolean getFaceLeft() {
        return _faceLeft;
    }

    /**
     * Determines whether the {@link JRotateLabel} is facing left.
     * @param faceLeft {@code true} if the baseline of the {@link JRotateLabel}
     *                 should face left, {@code false} if it should face right
     */
    public void setFaceLeft(boolean faceLeft) {
        _faceLeft = faceLeft;
        repaint(); // same size, no remeasure needed
    }

    /**
     * Gets the text displayed in the {@link JRotateLabel}.
     * @return the text displayed in the {@link JRotateLabel}
     */
    public String getText() {
        return _text;
    }

    /**
     * Sets the text to display in the {@link JRotateLabel}.
     * @param text the text to display in the {@link JRotateLabel}
     */
    public void setText(String text) {
        _text = text;
        remeasure();
    }

    /**
     * Remeasures and repaints the {@link JRotateLabel}.
     */
    private void remeasure() {
        if (_text == null || _text.isEmpty())
            _size = new Dimension();
        else {
            final FontMetrics metrics = getFontMetrics(getFont());
            _size = new Dimension(metrics.getHeight() + 1,
                    metrics.stringWidth(_text) + 1);
        }
        setMinimumSize(_size);
        setMaximumSize(_size);
        setPreferredSize(_size);

        repaint();
    }

    /**
     * Invoked by Swing to draw the content area of the {@link JRotateLabel}.
     * @param g the {@link Graphics2D} context in which to paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (_text == null || _text.isEmpty())
            return;

        final Graphics2D g2 = (Graphics2D) g;
        final AffineTransform transform = g2.getTransform();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (_faceLeft) {
            g2.rotate(Math.toRadians(90));
            g2.drawString(_text, 1,
                    -1 - g2.getFontMetrics().getDescent() - getInsets().top);
        } else {
            g2.rotate(Math.toRadians(-90));
            g2.drawString(_text, 1 - _size.height - getInsets().top,
                    g2.getFontMetrics().getAscent());
        }
        g2.setTransform(transform);
    }
}