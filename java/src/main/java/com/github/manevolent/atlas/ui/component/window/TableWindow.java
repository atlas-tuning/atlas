package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.definition.Axis;
import com.github.manevolent.atlas.definition.Series;
import com.github.manevolent.atlas.definition.Table;
import com.github.manevolent.atlas.ui.IconHelper;
import com.github.manevolent.atlas.ui.component.JRotateLabel;
import com.github.manevolent.atlas.ui.component.RowNumberTable;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.Ikonli;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.kordamp.ikonli.swing.FontIcon;
import org.kordamp.ikonli.swing.IkonResolver;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.manevolent.atlas.definition.Axis.X;
import static com.github.manevolent.atlas.definition.Axis.Y;

public class TableWindow extends Window implements FocusListener {
    private static final Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private final Table table;

    private RowNumberTable rowNumberTable;
    private JTable tableComponent;

    private float min, max;

    public TableWindow(EditorForm editor, Table table) {
        super(editor);

        this.table = table;
    }

    @Override
    protected void preInitComponent(JInternalFrame component) {
        component.setTitle(table.getName());
    }

    @Override
    protected void postInitComponent(JInternalFrame component) {
        super.postInitComponent(component);

        component.setPreferredSize(tableComponent.getPreferredSize());
        component.setSize(tableComponent.getPreferredSize());
    }

    @Override
    protected void initComponent(JInternalFrame window) {
        int x_size = table.getSeries(X) == null ? 1 : table.getSeries(X).getLength();
        int y_size = table.getSeries(Y) == null ? 1 : table.getSeries(Y).getLength();
        Object[][] data = new Float[y_size][x_size];

        window.addFocusListener(this);

        tableComponent = new JTable();
        tableComponent.setBackground(Color.GRAY.darker().darker());
        tableComponent.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableComponent.getColumnModel().setColumnSelectionAllowed(true);
        tableComponent.setBorder(BorderFactory.createEmptyBorder());
        tableComponent.getTableHeader().setReorderingAllowed(false);
        tableComponent.getTableHeader().setResizingAllowed(false);
        tableComponent.getTableHeader().setFont(font);

        // Possibly add X series headers
        Object[] columns;
        Series x = table.getSeries(X);
        Series y = table.getSeries(Y);

        if (x != null) {
            columns = new Object[x.getLength()];
            for (int i = 0; i < x.getLength(); i ++) {
                try {
                    columns[i] = String.format("%.2f", x.get(i));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            columns = new Object[1];
        }

        tableComponent.setModel(new DefaultTableModel(data, columns));
        updateMinMax();

        // Set the renderer for cells
        tableComponent.setDefaultRenderer(Object.class, new TableCellRenderer());
        tableComponent.setDefaultRenderer(String.class, new TableCellRenderer());
        tableComponent.setDefaultRenderer(Float.class, new TableCellRenderer());

        // Add table data
        Map<Axis, Integer> coordinates = new HashMap<>();
        int size = 1;

        for (Axis axis : table.getAxes()) {
            coordinates.put(axis, 0);
            size *= table.getSeries(axis).getLength();
        }

        java.util.List<Axis> orderedAxes = Arrays.stream(Axis.values())
                .filter(coordinates::containsKey).toList();
        int read = 0;
        while (read < size) {
            float value;
            try {
                value = table.getCell(coordinates);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            tableComponent.getModel().setValueAt(
                    value,
                    coordinates.getOrDefault(Y, 0),
                    coordinates.getOrDefault(X, 0)
            );

            read ++;

            // In the case of 1x1 tables (no axes)
            if (coordinates.isEmpty()) {
                break;
            }

            // Advance
            boolean carry = true;
            for (Axis axis : orderedAxes) {
                int index = coordinates.get(axis);

                if (carry) {
                    index++;
                    carry = false;
                }

                if (table.getSeries(axis).getLength() <= index) {
                    carry = true;
                    index = 0;
                }

                coordinates.put(axis, index);
            }
        }

        JScrollPane scrollPane = new JScrollPane(tableComponent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        if (table.getAxes().contains(Y)) {
            java.util.List<String> rowHeaders = generateRowHeaders();
            rowNumberTable = new RowNumberTable(tableComponent, rowHeaders);
            rowNumberTable.getTableHeader().setFont(font);
            rowNumberTable.updateWidth();

            scrollPane.setRowHeader(new JViewport());
            scrollPane.getRowHeader().add(rowNumberTable);
        }

        JPanel panel = new JPanel(new BorderLayout());
        JRotateLabel y_label;
        if (y != null) {
            y_label = new JRotateLabel(getAxisHeaderString(y));
            y_label.setForeground(Color.GRAY);
            panel.add(y_label, BorderLayout.WEST);
        } else {
            y_label = null;
        }

        if (x != null) {
            JLabel x_label = new JLabel(getAxisHeaderString(x));
            x_label.setHorizontalAlignment(JLabel.LEFT);
            x_label.setForeground(Color.GRAY);
            if (y_label != null) {
                x_label.setBorder(BorderFactory.createEmptyBorder(
                        2,
                        (int) (y_label.getPreferredSize().width + rowNumberTable.getPreferredSize().getWidth()),
                        2,
                        0
                ));
            }
            panel.add(x_label, BorderLayout.NORTH);
        }

        if (y != null) {
            y_label.setBorder(BorderFactory.createEmptyBorder(
                    (int) (tableComponent.getTableHeader().getPreferredSize().getHeight()),
                    0,
                    0,
                    0
            ));
        }

        panel.add(scrollPane, BorderLayout.CENTER);

        window.add(panel);
    }

    private String getAxisHeaderString(Series series) {
        if (series.getUnit() != null && !series.getUnit().getText().equalsIgnoreCase(series.getName())) {
            return STR."\{series.getName()} (\{series.getUnit().getText()})";
        } else {
            return series.getName();
        }
    }

    private java.util.List<String> generateRowHeaders() {
        return IntStream.range(0, table.getSeries(Y).getLength())
                .mapToObj(index -> {
                    try {
                        return table.getSeries(Y).get(index);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(value -> {
                    return String.format("%.2f", value);
                })
                .toList();
    }

    private void updateMinMax() {
        max = Float.MIN_VALUE;
        min = Float.MAX_VALUE;
        for (int i = 0; i < table.getData().getLength(); i ++) {
            try {
                max = Math.max(table.getData().get(i), max);
                min = Math.min(table.getData().get(i), min);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (min == -0) {
            min = 0;
        }

        if (max == -0) {
            max = 0;
        }
    }

    private Color getColor(float min, float value, float max) {
        if (min == max) {
            return Color.WHITE;
        } else if (table.getData().getLength() <= 1) {
            return Color.WHITE;
        }

        float green = (value - min) / (max - min);
        float red = 1f - green;

        return new Color(
                red,
                green,
                0
        );
    }

    @Override
    public Icon getIcon() {
       return IconHelper.get(CarbonIcons.DATA_TABLE, Color.WHITE);
    }

    @Override
    public void focusGained(FocusEvent e) {
        getEditor().tableFocused(table);
    }

    @Override
    public void focusLost(FocusEvent e) {

    }

    public Table getTable() {
        return table;
    }

    public class TableCellRenderer extends DefaultTableCellRenderer {
        public TableCellRenderer() {
            setVerticalAlignment(JLabel.CENTER);
            setHorizontalAlignment(JLabel.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            c.setFont(font);
            if (value != null) {
                float v;

                if (value instanceof Float) {
                    v = (Float) value;
                } else {
                    v = Float.parseFloat(value.toString());
                }

                c.setForeground(getColor(min, v, max));

                if (c instanceof JLabel) {
                    ((JLabel) c).setText(String.format("%.2f", v));
                    ((JLabel) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY.darker()));
                }
            }
            return c;
        }
    }
}
