package com.github.manevolent.atlas.ui.component.footer;

import com.github.manevolent.atlas.ui.Labels;
import com.github.manevolent.atlas.ui.EditorForm;

import javax.swing.*;
import java.awt.*;

public class EditorFooter extends Footer<EditorForm> {
    public EditorFooter(EditorForm editor) {
        super(editor);
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
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerBar.add(left, BorderLayout.WEST);

        left.add(Labels.text("Test"));
    }
}