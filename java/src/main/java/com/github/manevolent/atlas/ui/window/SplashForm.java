package com.github.manevolent.atlas.ui.window;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static java.awt.Font.SANS_SERIF;

public class SplashForm extends JFrame {
    private final BufferedImage splashImage;
    private final Font headerFont;
    private final Timer timer;

    private float progress;

    public SplashForm() throws IOException, FontFormatException {
        splashImage = ImageIO.read(SplashForm.class.getResource("/splash.png"));

        setIgnoreRepaint(false);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setResizable(false);

        timer = new Timer(250, (e) -> {
            if (isVisible()) {
                SplashForm.this.repaint();
            }
        });

        Font font = Font.createFont(Font.TRUETYPE_FONT,
                getClass().getResourceAsStream("/fonts/splash_header.otf"));

        this.headerFont = font.deriveFont(Font.BOLD, 30);

        JPanel backgroundImage = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Draw the background image.
                g.drawImage(splashImage, 0, 0,
                        getWidth(), getHeight(), this);
                g.setFont(headerFont);
                g.setColor(Color.WHITE);
                g.drawString("Atlas Tuning", 20, 50);

                Font serif = new Font(SANS_SERIF, Font.PLAIN, 14);
                g.setFont(serif);
                g.drawString("V 1.0.0.0", 20, 80);

                int progressHeight = 5;
                g.setColor(Color.GRAY);
                g.fillRect(0, getHeight() - progressHeight, getWidth(), getHeight());

                String progressString = String.format("%.0f", progress * 100) + "%";

                // Get the FontMetrics
                FontMetrics metrics = g.getFontMetrics(serif);

                // Determine the X coordinate for the text
                g.setColor(Color.WHITE);
                int x = (getWidth() / 2) - (metrics.stringWidth(progressString) / 2);

                if (progress > 0f) {
                    g.drawString(progressString, x, getHeight() - progressHeight - 16);
                }

                g.setColor(Color.GREEN.darker());
                g.fillRect(0, getHeight() - progressHeight, (int) (getWidth() * progress), getHeight());
            }
        };

        backgroundImage.setSize(getPreferredSize());
        backgroundImage.setBackground(Color.BLACK);

        add(backgroundImage);

        pack();
        setLocationRelativeTo(null);

        timer.start();
    }

    @Override
    public void dispose() {
        setVisible(false);

        super.dispose();

        timer.stop();
    }

    public void setProgress(float progress) {
        this.progress = progress;
        java.awt.EventQueue.invokeLater(SplashForm.this::repaint);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();

        height /= 3;

        float imageAspectRatio = (float)splashImage.getWidth() / (float)splashImage.getHeight();

        width = height * imageAspectRatio;

        return new Dimension((int)Math.round(width), (int)Math.round(height));
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }
}
