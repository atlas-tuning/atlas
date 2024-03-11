package com.github.manevolent.atlas.ui.component.footer;

import com.github.manevolent.atlas.ApplicationMetadata;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Labels;
import com.github.manevolent.atlas.ui.Editor;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TimerTask;

public class EditorFooter extends Footer<Editor> {
    private JLabel statusLabel;
    private Instant statusInstant;
    private String statusString;

    private java.util.Timer timer;

    public EditorFooter(Editor editor) {
        super(editor);

        this.statusInstant = Instant.now();
        this.statusString = "Initialized";
    }

    @Override
    protected void preInitComponent(JPanel footerBar) {
        footerBar.setLayout(new BorderLayout());
        footerBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                Color.GRAY.darker()));
    }

    /**
     * Can be reinitialized
     * @param footerBar footer bar
     */
    @Override
    protected void initComponent(JPanel footerBar) {
        Font smallFont = Fonts.getTextFont().deriveFont(11f);
        Color color = Fonts.getTextColor().darker();

        JPanel left = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerBar.add(left, BorderLayout.WEST);
        left.add(statusLabel = Labels.text("", color, smallFont));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerBar.add(right, BorderLayout.EAST);
        String applicationName = ApplicationMetadata.getName() + " " + ApplicationMetadata.getVersion();
        right.add(Labels.text(applicationName, color, smallFont));
    }

    @Override
    protected void postInitComponent(JPanel component) {
        timer = new java.util.Timer("Status");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    updateStatus();
                });
            }
        }, 1000, 1000);
    }

    public void updateStatus() {
        String timeString;
        Instant now = Instant.now();
        long minutes = ChronoUnit.MINUTES.between(statusInstant, now);
        long seconds = ChronoUnit.SECONDS.between(statusInstant, now);
        if (minutes == 1) {
            timeString = "a minute ago";
        } else if (minutes > 1) {
            timeString = minutes + " minutes ago";
        } else {
            timeString = "moments ago";
        }

        statusLabel.setText(statusString + " (" + timeString + ")");
    }

    public void setStatus(String status) {
        this.statusInstant = Instant.now();
        this.statusString = status;

        updateStatus();
    }
}