package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.Separators;
import com.github.manevolent.atlas.ui.component.window.TableEditor;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;

public class TableEditorToolbar extends Toolbar<TableEditor> {
    public TableEditorToolbar(TableEditor editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JToolBar toolbar) {
        toolbar.add(makeButton(FontAwesomeSolid.CALCULATOR, "calc", "Run custom function"));
        toolbar.addSeparator();
        toolbar.add(makeButton(FontAwesomeSolid.TIMES, "multiply", "Multiply value at selected cells"));
        toolbar.add(makeButton(FontAwesomeSolid.DIVIDE, "divide", "Divide value at selected cells"));
        toolbar.add(makeButton(FontAwesomeSolid.PLUS, "add", "Add value to selected cells"));
        toolbar.addSeparator();
        toolbar.add(makeButton(FontAwesomeSolid.PERCENTAGE, "percent", "Scale values with a percentage"));
        toolbar.add(makeButton(FontAwesomeSolid.EQUALS, "average", "Average values in selection"));
        toolbar.addSeparator();
        toolbar.add(makeButton(FontAwesomeSolid.GRIP_HORIZONTAL, "interpolate-horizontal", "Interpolate horizontally"));
        toolbar.add(makeButton(FontAwesomeSolid.GRIP_VERTICAL, "interpolate-vertical", "Interpolate vertically"));

        // Right side
        toolbar.add(Separators.vertical());
        toolbar.add(makeButton(CarbonIcons.CHART_CUSTOM, "define", "Edit table definition"));

    }
}
