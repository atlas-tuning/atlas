package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.definition.*;
import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.Labels;
import com.github.manevolent.atlas.ui.component.GraphicsHelper;
import com.github.manevolent.atlas.ui.component.JRotateLabel;
import com.github.manevolent.atlas.ui.component.RowNumberTable;
import com.github.manevolent.atlas.ui.Separators;
import com.github.manevolent.atlas.ui.component.footer.table.TableEditorFooter;
import com.github.manevolent.atlas.ui.component.menu.table.*;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static com.github.manevolent.atlas.definition.Axis.X;
import static com.github.manevolent.atlas.definition.Axis.Y;

public class TableEditor extends Window implements
        FocusListener,
        TableModelListener,
        ListSelectionListener {
    private static final int precisionPoints = 2;
    private static final String valueFormat = "%." + precisionPoints + "f";
    private static final Font valueFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private final Table table;

    private ThreadLocal<Boolean> selfUpdate = new ThreadLocal<>();
    private RowNumberTable rowNumberTable;
    private JTable tableComponent;
    private TableEditorFooter footer;
    private int[] lastSelectionRows = new int[0], lastSelectionColumns = new int[0];

    private float min, selMin, max, selMax;

    private FileMenu fileMenu;
    private EditMenu editMenu;
    private ViewMenu viewMenu;
    private HelpMenu helpMenu;

    public TableEditor(EditorForm editor, Table table) {
        super(editor);

        this.selfUpdate.set(false);
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

        //TODO this most likely will annoy people, make a setting for it
        try {
            component.setMaximum(true);
        } catch (PropertyVetoException e) {
            // Ignore
        }
    }

    @Override
    protected void initComponent(JInternalFrame window) {
        int x_size = table.getSeries(X) == null ? 1 : table.getSeries(X).getLength();
        int y_size = table.getSeries(Y) == null ? 1 : table.getSeries(Y).getLength();
        Object[][] data = new Float[y_size][x_size];

        window.addFocusListener(this);

        tableComponent = new JTable() {
            @Override
            public boolean isCellEditable(int row, int cols)
            {
                return true;
            }
        };
        tableComponent.setBackground(Color.GRAY.darker().darker());
        tableComponent.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableComponent.getColumnModel().setColumnSelectionAllowed(true);
        tableComponent.setBorder(BorderFactory.createEmptyBorder());
        tableComponent.getTableHeader().setReorderingAllowed(false);
        tableComponent.getTableHeader().setResizingAllowed(false);
        tableComponent.getTableHeader().setFont(valueFont);

        Series x = table.getSeries(X);
        Series y = table.getSeries(Y);

        // Possibly add X series headers
        Object[] columns;
        if (x != null) {
            columns = new Object[x.getLength()];
            for (int i = 0; i < x.getLength(); i ++) {
                try {
                    columns[i] = String.format(valueFormat, x.get(i));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            columns = new Object[1];
        }

        tableComponent.setModel(new DefaultTableModel(data, columns));
        updateData();
        updateMinMax();

        // Set the renderer for cells
        tableComponent.setDefaultRenderer(Object.class, new TableCellRenderer());
        tableComponent.setDefaultRenderer(String.class, new TableCellRenderer());
        tableComponent.setDefaultRenderer(Float.class, new TableCellRenderer());
        tableComponent.setDefaultEditor(Object.class, new CellEditor());
        tableComponent.setDefaultEditor(String.class, new CellEditor());
        tableComponent.setDefaultEditor(Float.class, new CellEditor());

        tableComponent.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateSelection();
            }
        });

        tableComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updateSelection();
            }
        });

        tableComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                updateSelection();
            }
        });

        window.add(initMenuBar(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(tableComponent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        if (table.getAxes().contains(Y)) {
            java.util.List<String> rowHeaders = generateRowHeaders();
            rowNumberTable = new RowNumberTable(tableComponent, rowHeaders);
            rowNumberTable.getTableHeader().setFont(valueFont);
            rowNumberTable.updateWidth();

            scrollPane.setRowHeader(new JViewport());
            scrollPane.getRowHeader().add(rowNumberTable);
        }

        JPanel panel = new JPanel(new BorderLayout());

        JRotateLabel y_label;
        if (y != null) {
            y_label = new JRotateLabel(getSeriesHeaderString(y));
            y_label.setForeground(Color.GRAY);
            panel.add(y_label, BorderLayout.WEST);
        } else {
            y_label = null;
        }

        if (x != null) {
            JLabel x_label = new JLabel(getSeriesHeaderString(x));
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


        // Create the footer bar that displays some state data as well as
        // some quick calculations about the table and/or its selection.
        footer = new TableEditorFooter(this);
        panel.add(footer.getComponent(), BorderLayout.SOUTH);

        window.add(panel);

        tableComponent.getModel().addTableModelListener(this);
        tableComponent.setColumnSelectionInterval(0, 0);
        tableComponent.setRowSelectionInterval(0, 0);
        tableComponent.getSelectionModel().addListSelectionListener(this);
        tableComponent.putClientProperty("terminateEditOnFocusLost", true);

        updateCellWidth();
    }

    private JMenuBar initMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add((fileMenu = new FileMenu(this)).getComponent());
        menuBar.add((editMenu = new EditMenu(this)).getComponent());
        menuBar.add((viewMenu = new ViewMenu(this)).getComponent());
        menuBar.add((helpMenu = new HelpMenu(this)).getComponent());
        return menuBar;
    }

    private void updateCellWidth() {
        // Default to a minimum spacing of 6 characters
        String longestString = "000.00";

        // Find the longest string in the columns (X axis)
        Series x = table.getSeries(X);
        if (x != null) {
            for (int i = 0; i < x.getLength(); i ++) {
                float data;
                try {
                    data = x.get(i);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String formattedString = String.format(" " + valueFormat + " ", data);
                if (formattedString.length() > longestString.length()) {
                    longestString = formattedString;
                }
            }
        }

        // Find the longest string in the cells (table data)
        for (int i = 0; i < table.getData().getLength(); i ++) {
            float data;
            try {
                data = table.getData().get(i);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String formattedString = String.format(" " + valueFormat + " ", data);
            if (formattedString.length() > longestString.length()) {
                longestString = formattedString;
            }
        }

        // Get the ideal string width
        int stringWidth = GraphicsHelper.getFontMetrics(tableComponent.getFont())
                .stringWidth(longestString);

        // Grab the margin
        int margin = tableComponent.getColumnModel().getColumnMargin() * 2;

        // Grab the cell components we'll generate and check the border insets
        Component cellComponent = tableComponent.getCellRenderer(0, 0)
                .getTableCellRendererComponent(tableComponent, 0.00f, false, true, 0, 0);
        if (cellComponent instanceof JComponent) {
            Border border = ((JComponent) cellComponent).getBorder();
            Insets insets = border.getBorderInsets(cellComponent);
            margin += insets.left + insets.right;
        }

        // Some extra spacing for comfort
        margin += 5;

        // Set all the calculated spacing across the table's columns
        int spacing = stringWidth + margin;
        for (int i = 0; i < tableComponent.getColumnModel().getColumnCount(); i ++) {
            var column = tableComponent.getColumnModel().getColumn(i);
            column.setMinWidth(spacing);
            column.setPreferredWidth(spacing);
            column.setWidth(spacing);
        }
    }

    private void updateData() {
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
                setValue(coordinates, value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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

        updateCellWidth();
    }

    public String getSeriesHeaderString(Series series) {
        if (series.getUnit() != null && series.getName() == null) {
            return series.getUnit().getText();
        } else if (series.getUnit() != null && !series.getUnit().getText().equalsIgnoreCase(series.getName())) {
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
                .map(value -> String.format(valueFormat, value))
                .toList();
    }

    private void updateSelectionMinMax() {
        selMax = -Float.MAX_VALUE;
        selMin = Float.MAX_VALUE;

        int[] selectedRow = tableComponent.getSelectedRows();
        int[] selectedColumns = tableComponent.getSelectedColumns();

        for (int i = 0; i < selectedRow.length; i++) {
            for (int j = 0; j < selectedColumns.length; j++) {
                float data;
                try {
                    data = table.getCell(selectedColumns[j], selectedRow[i]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                selMax = Math.max(data, selMax);
                selMin = Math.min(data, selMin);
            }
        }

        if (selMin == -0) {
            selMin = 0;
        }

        if (selMax == -0) {
            selMax = 0;
        }
    }

    private void updateMinMax() {
        max = -Float.MAX_VALUE;
        min = Float.MAX_VALUE;

        for (int i = 0; i < table.getData().getLength(); i ++) {
            try {
                float data = table.getData().get(i);
                max = Math.max(data, max);
                min = Math.min(data, min);
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

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getSelectionMin() {
        return selMin;
    }

    public float getSelectionMax() {
        return selMax;
    }

    private Color getColor(float min, float value, float max) {
        if (min == max) {
            return Color.WHITE;
        } else if (table.getData().getLength() <= 1) {
            return Color.WHITE;
        }

        float green = (value - min) / (max - min);
        float red = 1f - green;

        green = Math.max(0, Math.min(1, green));
        red = Math.max(0, Math.min(1, red));

        return new Color(
                red,
                green,
                0
        );
    }

    public Color scaleValueColor(float value) {
        float min = this.min, max = this.max;

        if (min == max) {
            return Color.WHITE;
        } else if (table.getData().getLength() <= 1) {
            return Color.WHITE;
        }

        float green = (value - min) / (max - min);
        float red = 1f - green;

        green = Math.max(0, Math.min(1, green));
        red = Math.max(0, Math.min(1, red));

        return new Color(
                red,
                green,
                0
        );
    }

    @Override
    public Icon getIcon() {
       return Icons.get(CarbonIcons.DATA_TABLE, Color.WHITE);
    }

    @Override
    public void focusGained(FocusEvent e) {
        getParent().tableFocused(table);
    }

    @Override
    public void focusLost(FocusEvent e) {

    }

    public Table getTable() {
        return table;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (selfUpdate.get()) {
            return;
        }

        int row = e.getFirstRow();
        int col = e.getColumn();

        float value = (Float) tableComponent.getValueAt(
                e.getFirstRow(),
                e.getColumn()
        );

        float newValue;
        float oldValue;

        try {
            oldValue = table.getCell(col, row);
            newValue = table.setCell(value, col, row);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        if ((newValue > max || newValue < min) ||
                (oldValue <= min || oldValue >= max)) {
            updateMinMax();
            footer.reinitialize();
        }

        setValue(row, col, newValue);

        if (String.format(valueFormat, newValue).length() !=
                String.format(valueFormat, oldValue).length()) {
            updateCellWidth();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        updateSelection();
    }

    public void setValue(Map<Axis, Integer> coordinates, float value) {
        setValue(coordinates.getOrDefault(Y, 0),
                coordinates.getOrDefault(X, 0),
                value);
    }

    public void setValue(int row, int col, float value) {
        selfUpdate.set(true);

        try {
            tableComponent.getModel().setValueAt(
                    value,
                    row,
                    col
            );

            tableComponent.revalidate();
        } finally {
            selfUpdate.set(false);
        }
    }

    /**
     * Recalculates and updates the current selection
     */
    private void updateSelection() {
        int[] selectedRows = tableComponent.getSelectedRows();
        int[] selectedColumns = tableComponent.getSelectedColumns();
        boolean rowsEqual = Arrays.equals(selectedRows, lastSelectionRows);
        boolean columnsEqual = Arrays.equals(selectedColumns, lastSelectionColumns);
        if (!rowsEqual || !columnsEqual) {
            lastSelectionColumns = selectedColumns;
            lastSelectionRows = selectedRows;
        } else {
            // Selection hasn't actually changed; don't spend time calculating
            return;
        }

        updateSelectionMinMax();
        footer.reinitialize();
    }

    public JTable getJTable() {
        return tableComponent;
    }

    public Font getValueFont() {
        return valueFont;
    }

    public String formatValue(float value) {
        return String.format(valueFormat, value);
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
            c.setFont(valueFont);
            if (value != null) {
                float v;

                if (value instanceof Float) {
                    v = (Float) value;
                } else {
                    v = Float.parseFloat(value.toString());
                }

                c.setForeground(getColor(min, v, max));

                if (c instanceof JLabel) {
                    ((JLabel) c).setText(String.format(valueFormat, v));
                    ((JLabel) c).setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY.darker()));
                }
            }
            return c;
        }
    }

    private class CellEditor extends DefaultCellEditor implements TableCellEditor {
        private final JTextField textField;

        private CellEditor(JTextField textField) {
            super(textField);
            this.textField = textField;
        }

        private CellEditor() {
            this(new JTextField());
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return super.isCellEditable(anEvent);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.textField.setFont(table.getFont());
            this.textField.setText(value.toString());
            return textField;
        }

        @Override
        public Object getCellEditorValue() {
            return Float.parseFloat(textField.getText());
        }
    }
}
