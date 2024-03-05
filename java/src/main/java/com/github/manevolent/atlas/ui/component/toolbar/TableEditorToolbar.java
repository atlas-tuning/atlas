package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.component.window.TableEditor;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;

public class TableEditorToolbar extends Toolbar<TableEditor> {
    public TableEditorToolbar(TableEditor editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JToolBar toolbar) {
        toolbar.add(makeButton(CarbonIcons.ADD, "add", "Add value to selected cells"));
        toolbar.add(makeButton(CarbonIcons.SUBTRACT, "subtract", "Subtract value from selected cells"));
        toolbar.add(makeButton(CarbonIcons.X, "multiply", "Multiply value at selected cells"));
    }

    private JButton makeButton(Ikon ikon, String actionCommand, String toolTipText) {
        JButton add = new JButton(Icons.get(ikon, Color.WHITE, 20));
        add.setActionCommand(actionCommand);
        add.setToolTipText(toolTipText);
        return add;
    }
}
