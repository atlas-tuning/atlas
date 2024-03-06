package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;

import static com.github.manevolent.atlas.ui.Fonts.getTextColor;

public class DataLoggingTab extends Tab {
    public DataLoggingTab(EditorForm editor) {
        super(editor);
    }

    @Override
    public String getTitle() {
        return "Data Logging";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.DOCUMENT_DOWNLOAD, getTextColor());
    }

    @Override
    protected void initComponent(JPanel panel) {

    }
}
