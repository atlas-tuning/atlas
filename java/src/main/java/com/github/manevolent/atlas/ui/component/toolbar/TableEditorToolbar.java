package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.component.window.TableEditor;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;

public class TableEditorToolbar extends Toolbar<TableEditor> {
    public TableEditorToolbar(TableEditor editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JToolBar toolbar) {
        toolbar.add(makeButton(CarbonIcons.CHART_CUSTOM, "define", "Edit table definition", (e) -> {
            getParent().getParent().openTableDefinition(getParent().getTable());
        }));
        toolbar.addSeparator();
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


    }
}
