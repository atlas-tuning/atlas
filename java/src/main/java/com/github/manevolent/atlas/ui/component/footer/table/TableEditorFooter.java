package com.github.manevolent.atlas.ui.component.footer.table;

import com.github.manevolent.atlas.model.Precision;
import com.github.manevolent.atlas.model.Series;
import com.github.manevolent.atlas.model.Table;
import com.github.manevolent.atlas.model.Unit;
import com.github.manevolent.atlas.ui.Labels;
import com.github.manevolent.atlas.ui.Separators;
import com.github.manevolent.atlas.ui.component.footer.Footer;
import com.github.manevolent.atlas.ui.component.window.TableEditor;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static com.github.manevolent.atlas.model.Axis.X;
import static com.github.manevolent.atlas.model.Axis.Y;

/**
 * The table editor footer is the strip of values on the very bottom of the table editor window/pane.
 * This shows stuff like min/max values, table size, datatype, selection size, etc.
 */
public class TableEditorFooter extends Footer<TableEditor> {
    public TableEditorFooter(TableEditor editor) {
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
        TableEditor editor = getParent();
        Table table = editor.getTable();
        JTable tableComponent = editor.getJTable();
        footerBar.removeAll();

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerBar.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerBar.add(right, BorderLayout.EAST);

        String tableSizeString;

        int numColumns = tableComponent.getSelectedColumns().length;
        int numRows = tableComponent.getSelectedRows().length;
        boolean hasSelection = !getParent().isReadOnly() && (numColumns + numRows) > 2;

        Series x = table.getSeries(X);
        Series y = table.getSeries(Y);

        left.add(Labels.text(
                CarbonIcons.RULER,
                editor.getSeriesHeaderString(table.getData()),
                "Cell data series"
        ));

        left.add(Separators.vertical());

        left.add(Labels.text(
                CarbonIcons.MATRIX,
                table.getData().getFormat().name().toLowerCase(),
                Color.GRAY,
                "Data format of cells in ROM"
        ));

        left.add(Separators.vertical());

        // Calculate value precision
        if (table.getData().getFormat().getPrecision() == Precision.WHOLE_NUMBER) {
            float precision = table.getData().getScale().getPrecision();
            precision = Math.max(0.01f, precision);
            left.add(Labels.text(
                    CarbonIcons.CALIBRATE,
                    editor.formatValue(precision) + table.getData().getUnit().getText(),
                    editor.getValueFont(),
                    Color.GRAY,
                    "Precision of cell data"
            ));
        }

        left.add(Separators.vertical());

        float min, max;
        if (hasSelection) {
            min = editor.getSelectionMin();
            max = editor.getSelectionMax();
        } else {
            min = editor.getMin();
            max = editor.getMax();
        }

        if (editor.getMin() != editor.getMax()) {
            left.add(Labels.text(
                    CarbonIcons.ARROW_DOWN, Color.GRAY,
                    editor.getValueFont(), editor.formatValue(min), editor.scaleValueColor(min),
                    hasSelection ? "Minimum value in selection" : "Minimum value in table"
            ));
            left.add(Labels.text(
                    CarbonIcons.ARROW_UP, Color.GRAY,
                    editor.getValueFont(), editor.formatValue(max), editor.scaleValueColor(max),
                    hasSelection ? "Maximum value in selection" : "Maximum value in table"
            ));
        }

        if (!hasSelection && !getParent().isReadOnly()) {
            int selectedColumn = tableComponent.getSelectedColumn();
            int selectedRow = tableComponent.getSelectedRow();

            if (selectedColumn >= 0 && selectedRow >= 0) {
                if (x != null) {
                    float value;
                    try {
                        value = x.get(selectedColumn);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Unit unit = x.getUnit();
                    String unitString;
                    if (unit != null) {
                        unitString = " " + unit.getText();
                    } else {
                        unitString = "";
                    }
                    right.add(Labels.text(CarbonIcons.LETTER_XX,
                            editor.formatValue(value) + unitString,
                            editor.getValueFont(),
                            Color.GRAY,
                            "X axis value at position"
                    ));
                }

                if (y != null) {
                    float value;
                    try {
                        value = y.get(selectedRow);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Unit unit = y.getUnit();
                    String unitString;
                    if (unit != null) {
                        unitString = " " + unit.getText();
                    } else {
                        unitString = "";
                    }
                    right.add(Labels.text(
                            CarbonIcons.LETTER_YY,
                            editor.formatValue(value) + unitString,
                            editor.getValueFont(),
                            Color.GRAY,
                            "Y axis value at position"
                    ));
                }

                right.add(Separators.vertical());

                selectedColumn += 1;
                selectedRow += 1;

                right.add(Labels.text(
                        CarbonIcons.CENTER_SQUARE,
                        selectedColumn + "," + selectedRow,
                        Color.GRAY,
                        "Selected position in table"
                ));

                right.add(Separators.vertical());
            }
        }

        if (!hasSelection) {
            int numAxes = table.getAxes().size();
            if (numAxes == 0) {
                tableSizeString = "1x1";
            } else if (numAxes == 1) {
                tableSizeString = x.getLength() + "x1";
            } else if (numAxes == 2) {
                tableSizeString = x.getLength() + "x" + y.getLength();
            } else {
                tableSizeString = numAxes + "D";
            }
        } else {
            tableSizeString = "SEL " + numColumns + "x" + numRows;
        }

        right.add(Labels.text(
                hasSelection ? CarbonIcons.SELECT_01 : CarbonIcons.MAXIMIZE,
                tableSizeString, Color.GRAY,
                hasSelection ? "Selection size" : "Table size"
        ));

        footerBar.revalidate();
        footerBar.repaint();
    }
}
