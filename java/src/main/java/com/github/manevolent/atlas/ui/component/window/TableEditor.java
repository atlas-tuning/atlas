package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.ui.dialog.VariableInputDialog;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.component.JRotateLabel;
import com.github.manevolent.atlas.ui.component.RowNumberTable;
import com.github.manevolent.atlas.ui.component.footer.TableEditorFooter;
import com.github.manevolent.atlas.ui.component.menu.table.*;
import com.github.manevolent.atlas.ui.component.toolbar.TableEditorToolbar;
import com.github.manevolent.atlas.ui.Editor;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.IntStream;

import static com.github.manevolent.atlas.model.Axis.X;
import static com.github.manevolent.atlas.model.Axis.Y;
import static com.github.manevolent.atlas.ui.util.Fonts.bold;
import static com.github.manevolent.atlas.ui.util.Fonts.getTextColor;
import static java.awt.event.KeyEvent.*;

public class TableEditor extends Window implements
        FocusListener,
        TableModelListener,
        ListSelectionListener, CellEditorListener {

    private static final Set<Integer> navigationKeys = Set.of(
            VK_LEFT, VK_KP_LEFT,
            VK_RIGHT, VK_KP_RIGHT,
            VK_UP, VK_KP_UP,
            VK_DOWN, VK_KP_DOWN
    );

    private static final int precisionPoints = 2;
    private static final String valueFormat = "%." + precisionPoints + "f";
    private final Table table;

    private ThreadLocal<Boolean> selfUpdate = new ThreadLocal<>();
    private RowNumberTable rowNumberTable;
    private JPanel rootPanel;
    private JTable tableComponent;
    private TableEditorFooter footer;
    private JLabel x_label;
    private JRotateLabel y_label;
    private JScrollPane scrollPane;
    private int[] lastSelectionRows = new int[0], lastSelectionColumns = new int[0];

    private float min, selMin, max, selMax;

    private FileMenu fileMenu;
    private EditMenu editMenu;
    private ViewMenu viewMenu;
    private HelpMenu helpMenu;

    private TableEditorToolbar toolbar;

    private final boolean readOnly;

    public TableEditor(Editor editor, Table table, boolean readOnly) {
        super(editor);

        this.readOnly = readOnly;
        this.selfUpdate.set(false);
        this.table = table;
    }

    public TableEditor(Editor editor, Table table) {
        super(editor);

        this.readOnly = false;
        this.selfUpdate.set(false);
        this.table = table;
    }

    @Override
    public String getTitle() {
        return table.getName();
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.DATA_TABLE, getTextColor());
    }

    @Override
    protected void initComponent(JInternalFrame window) {
        window.addFocusListener(this);

        tableComponent = new JTable() {
            @Override
            public boolean isCellEditable(int row, int cols)
            {
                return !readOnly;
            }
        };
        tableComponent.setBackground(Color.GRAY.darker().darker());
        tableComponent.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableComponent.setBorder(BorderFactory.createEmptyBorder());
        tableComponent.getTableHeader().setReorderingAllowed(false);
        tableComponent.getTableHeader().setResizingAllowed(false);
        tableComponent.setColumnSelectionAllowed(true);
        tableComponent.setRowSelectionAllowed(true);
        tableComponent.getTableHeader().setFont(Fonts.VALUE_FONT);

        // Possibly add X series headers
        tableComponent.setModel(generateTableModel());
        updateData();
        updateMinMax();

        // Set the renderer for cells
        tableComponent.setDefaultRenderer(Object.class, new TableCellRenderer());
        tableComponent.setDefaultRenderer(String.class, new TableCellRenderer());
        tableComponent.setDefaultRenderer(Float.class, new TableCellRenderer());

        // Set a default editor, so we can control cell edit functions
        tableComponent.setDefaultEditor(Object.class, new TableCellEditor());
        tableComponent.setDefaultEditor(String.class, new TableCellEditor());
        tableComponent.setDefaultEditor(Float.class, new TableCellEditor());

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
                if (navigationKeys.contains(e.getKeyCode())) {
                    SwingUtilities.invokeLater(() -> updateSelection());
                    return;
                }

                if (Character.isDigit(e.getKeyChar()) || e.getKeyChar() == '.'
                        || e.getKeyChar() == '-') {
                    // Allow pressing numeric values
                    return;
                }

                if (e.isControlDown() || e.isMetaDown()) {
                    if (e.getKeyCode() == VK_A) {
                        SwingUtilities.invokeLater(() -> updateSelection());
                    }

                    return;
                }

                if (e.getKeyCode() == VK_S  || e.getKeyChar() == '%') {
                    scaleSelection();
                } else if (e.getKeyCode() == VK_X || e.getKeyCode() == VK_M || e.getKeyChar() == '*') {
                    multiplySelection();
                } else if (e.getKeyCode() == VK_A || e.getKeyChar() == '+') {
                    addSelection();
                } else if (e.getKeyCode() == VK_MINUS || e.getKeyChar() == '-') {
                    subtractSelection();
                } else if (e.getKeyCode() == VK_H) {
                    interpolateHorizontal();
                } else if (e.getKeyCode() == VK_V) {
                    interpolateVertical();
                } else if (e.getKeyCode() == VK_I) {
                    averageSelection();
                } else if (e.getKeyCode() == VK_D || e.getKeyCode() == VK_SLASH || e.getKeyChar() == '/') {
                    divideSelection();
                }

                if (e.getKeyCode() == VK_BACK_SPACE || e.getKeyCode() == VK_DELETE) {
                    return;
                }

                e.consume();
            }

            @Override
            public void keyTyped(KeyEvent e) {
                updateSelection();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                updateSelection();
            }
        });

        if (!readOnly) {
            JPanel north = new JPanel();
            north.setLayout(new GridLayout(2, 1));

            north.add(initMenuBar());
            north.add(initToolbar());

            window.add(north, BorderLayout.NORTH);
        }

        scrollPane = new JScrollPane(tableComponent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        updateRowHeaders();

        rootPanel = new JPanel(new BorderLayout());
        updateAxisNames();
        rootPanel.add(scrollPane, BorderLayout.CENTER);

        // Create the footer bar that displays some state data as well as
        // some quick calculations about the table and/or its selection.
        footer = new TableEditorFooter(this);
        rootPanel.add(footer.getComponent(), BorderLayout.SOUTH);

        window.add(rootPanel);

        tableComponent.getModel().addTableModelListener(this);
        tableComponent.setColumnSelectionInterval(0, 0);
        tableComponent.setRowSelectionInterval(0, 0);
        tableComponent.getSelectionModel().addListSelectionListener(this);
        tableComponent.putClientProperty("terminateEditOnFocusLost", true);

        if (readOnly) {
            tableComponent.setEnabled(false);
        } else {
            tableComponent.setEnabled(true);
        }

        updateCellWidth();
    }

    public void averageSelection() {
        int[] selectedRows = tableComponent.getSelectedRows();
        int[] selectedColumns = tableComponent.getSelectedColumns();
        float sum = 0;
        for (int selectedRow : selectedRows) {
            for (int selectedColumn : selectedColumns) {
                try {
                    sum += table.getCell(getParent().getCalibration(), selectedColumn, selectedRow);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        float avg = sum / (selectedRows.length * selectedColumns.length);
        processSelection(x -> avg);

        updateCellWidth();
        updateMinMax();
        updateSelectionMinMax();
        footer.reinitialize();
    }

    public void interpolateVertical() {

    }

    public void interpolateHorizontal() {

    }

    public void addSelection() {
        Double answer = VariableInputDialog.show(getParent(), "Add",
                "Enter value to add to cells", 0D);
        if (answer == null) {
            return;
        }
        float coefficient = answer.floatValue();
        processSelection((value) -> value + coefficient);
    }

    public void subtractSelection() {
        Double answer = VariableInputDialog.show(getParent(), "Subtract",
                "Enter value to subtract from cells", 0D);
        if (answer == null) {
            return;
        }
        float coefficient = answer.floatValue();
        processSelection((value) -> value - coefficient);
    }

    public void scaleSelection() {
        Double answer = VariableInputDialog.show(getParent(), "Scale",
                "Enter percentage to scale cells by", 100D);
        if (answer == null) {
            return;
        }
        float coefficient = answer.floatValue() / 100f;
        processSelection((value) -> value * coefficient);
    }

    public void multiplySelection() {
        Double answer = VariableInputDialog.show(getParent(), "Multiply",
                "Enter value to multiply cells by", 1D);
        if (answer == null) {
            return;
        }
        float coefficient = answer.floatValue();
        processSelection((value) -> value * coefficient);
    }

    public void divideSelection() {
        Double answer = VariableInputDialog.show(getParent(), "Divide",
                "Enter value to divide cells by", 1D);
        if (answer == null) {
            return;
        }
        float coefficient = answer.floatValue();
        processSelection((value) -> value / coefficient);
    }

    public void processSelection(Function<Float, Float> function) {
        int[] selectedRows = tableComponent.getSelectedRows();
        int[] selectedColumns = tableComponent.getSelectedColumns();

        if (selectedRows.length == 0 && selectedColumns.length == 0) {
            selectedRows = IntStream.range(0, tableComponent.getRowCount()).toArray();
            selectedColumns = IntStream.range(0, tableComponent.getColumnCount()).toArray();
        }

        for (int selectedRow : selectedRows) {
            for (int selectedColumn : selectedColumns) {
                float data;
                try {
                    data = table.getCell(getParent().getCalibration(), selectedColumn, selectedRow);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                data = function.apply(data);

                tableComponent.getModel().setValueAt(data, selectedRow, selectedColumn);
            }
        }

        updateCellWidth();
        updateMinMax();
        updateSelectionMinMax();
        footer.reinitialize();
    }

    private TableModel generateTableModel() {
        int x_size = table.getSeries(X) == null ? 1 : table.getSeries(X).getLength();
        int y_size = table.getSeries(Y) == null ? 1 : table.getSeries(Y).getLength();
        Object[][] data = new Float[y_size][x_size];

        Series x = table.getSeries(X);

        Object[] columns;
        if (x != null) {
            columns = new Object[x.getLength()];
            for (int i = 0; i < x.getLength(); i ++) {
                try {
                    columns[i] = String.format(valueFormat, x.get(getParent().getCalibration(), i));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            columns = new Object[1];
        }
        return new DefaultTableModel(data, columns);
    }

    private JToolBar initToolbar() {
        return (toolbar = new TableEditorToolbar(this)).getComponent();
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
        int longestString = 0;

        FontMetrics metrics = Fonts.getFontMetrics(Fonts.VALUE_FONT);

        // Find the longest string in the columns (X axis)
        Series x = table.getSeries(X);
        if (x != null) {
            for (int i = 0; i < x.getLength(); i ++) {
                float data;
                try {
                    data = x.get(getParent().getCalibration(), i);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String formattedString = String.format(valueFormat, data);
                int width = metrics.stringWidth(formattedString);
                longestString = Math.max(longestString, width);
            }
        }

        // Find the longest string in the cells (table data)
        for (int i = 0; i < table.getData().getLength(); i ++) {
            float data;
            try {
                data = table.getData().get(getParent().getCalibration(), i);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String formattedString = String.format(valueFormat, data);
            int width = metrics.stringWidth(formattedString);
            longestString = Math.max(longestString, width);
        }

        // Get the ideal string width
        int stringWidth = longestString + metrics.stringWidth("  ");

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

    private void updateRowHeaders() {
        if (table.hasAxis(Y)) {
            if (rowNumberTable == null) {
                java.util.List<String> rowHeaders = generateRowHeaders();
                rowNumberTable = new RowNumberTable(tableComponent, rowHeaders);
                rowNumberTable.getTableHeader().setFont(Fonts.VALUE_FONT);
                rowNumberTable.updateWidth();

                scrollPane.setRowHeader(new JViewport());
                scrollPane.getRowHeader().add(rowNumberTable);
            }

            // Update row headers
            rowNumberTable.updateRowNames(generateRowHeaders());
        } else if (rowNumberTable != null) {
            scrollPane.setRowHeader(new JViewport());
            rowNumberTable.setVisible(false);
            rowNumberTable = null;
        }
    }

    private void updateData() {
        Map<Axis, Integer> coordinates = new HashMap<>();
        int size = 1;

        for (Axis axis : table.getAxes().keySet()) {
            coordinates.put(axis, 0);
            size *= table.getSeries(axis).getLength();
        }

        java.util.List<Axis> orderedAxes = Arrays.stream(Axis.values())
                .filter(coordinates::containsKey).toList();
        int read = 0;
        while (read < size) {
            float value;
            try {
                value = table.getCell(getParent().getCalibration(), coordinates);
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
        if (series.getUnit() != null && (series.getName() == null || series.getName().isBlank())) {
            return series.getUnit().getText();
        } else if (series.getUnit() != null &&
                !series.getName().contains(series.getUnit().getText())) {
            return STR."\{series.getName()} (\{series.getUnit().getText()})";
        } else {
            return series.getName();
        }
    }

    private java.util.List<String> generateRowHeaders() {
        return IntStream.range(0, table.getSeries(Y).getLength())
                .mapToObj(index -> {
                    try {
                        return table.getSeries(Y).get(getParent().getCalibration(), index);
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
                    data = table.getCell(getParent().getCalibration(), selectedColumns[j], selectedRow[i]);
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
                float data = table.getData().get(getParent().getCalibration(), i);
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

    private void updateAxisNames() {
        if (x_label != null) {
            rootPanel.remove(x_label);
        }
        if (y_label != null) {
            rootPanel.remove(y_label);
        }

        Series x = table.getSeries(X);
        Series y = table.getSeries(Y);

        if (y != null) {
            y_label = new JRotateLabel(getSeriesHeaderString(y));
            y_label.setFont(y_label.getFont().deriveFont(Font.ITALIC));
            y_label.setForeground(Color.GRAY);
            rootPanel.add(y_label, BorderLayout.WEST);
        } else {
            y_label = null;
        }

        if (x != null) {
            x_label = new JLabel(getSeriesHeaderString(x));
            x_label.setFont(x_label.getFont().deriveFont(Font.ITALIC));
            x_label.setHorizontalAlignment(JLabel.LEFT);
            x_label.setForeground(Color.GRAY);
            int leftOffset;
            if (y_label != null) {
                leftOffset = (int) (y_label.getPreferredSize().width +
                        rowNumberTable.getPreferredSize().getWidth());
            } else {
                leftOffset = 5;
            }

            x_label.setBorder(BorderFactory.createEmptyBorder(
                    2,
                    leftOffset,
                    2,
                    0
            ));

            rootPanel.add(x_label, BorderLayout.NORTH);
        }

        if (y != null) {
            y_label.setBorder(BorderFactory.createEmptyBorder(
                    (int) (tableComponent.getTableHeader().getPreferredSize().getHeight()),
                    0,
                    0,
                    0
            ));
        }
    }

    /**
     * Used by the table definition editor when you change values in it
     */
    public void reload() {
        updateRowHeaders();
        updateMinMax();
        tableComponent.setModel(generateTableModel());
        updateData();
        footer.reinitialize();
        updateAxisNames();

        getComponent().getContentPane().revalidate();
        getComponent().getContentPane().repaint();
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

    public void withSelfUpdate(boolean flag, Runnable runnable) {
        boolean before = selfUpdate.get();
        selfUpdate.set(flag);
        try {
            runnable.run();
        } finally {
            selfUpdate.set(before);
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (selfUpdate.get()) {
            return;
        }

        int row = e.getFirstRow();
        int col = e.getColumn();

        Object object = tableComponent.getValueAt(
                e.getFirstRow(),
                e.getColumn()
        );

        if (object == null) {
            float value;

            try {
                value = table.getCell(getParent().getCalibration(), col, row);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            setValue(row, col, value);
            return;
        }

        float value = (Float) object;

        if (value == -0) {
            value = 0;
        }

        float newValue;
        float oldValue;

        String valueString;

        try {
            oldValue = table.getCell(getParent().getCalibration(), col, row);
            String oldString = String.format(valueFormat, oldValue);
            valueString = String.format(valueFormat, value);

            if (!valueString.equals(oldString)) {
                newValue = table.setCell(getParent().getCalibration(), value, col, row);
                getParent().setDirty(true);
            } else {
                newValue = value;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        // If we're about to reposition the value due to the Table API precision,
        // we should let the user know that the scaling was adjusted.
        String newString = String.format(valueFormat, newValue);
        if (!valueString.equals(newString)) {
            Log.ui().log(Level.FINE, STR."Entered value adjusted to \{newString} (entered as \{valueString}) at " +
                    STR."cell [\{col + 1},\{row + 1}] due to precision of table \"\{table.getName()}\".");
        }

        boolean updateMinMax = (newValue > max || newValue < min) ||
                (oldValue <= min || oldValue >= max);

        if (updateMinMax) {
            updateMinMax();
            footer.reinitialize();
        }

        setValue(row, col, newValue);

        if (String.format(valueFormat, newValue).length() != String.format(valueFormat, oldValue).length()) {
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
        withSelfUpdate(true, () -> {
            tableComponent.getModel().setValueAt(
                    value,
                    row,
                    col
            );

            tableComponent.revalidate();
        });
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

        // Highlight columns
        for (int i = 0; i < tableComponent.getColumnModel().getColumnCount(); i ++) {
            var column = tableComponent.getColumnModel().getColumn(i);
        }

        updateSelectionMinMax();

        SwingUtilities.invokeLater(() -> footer.reinitialize());
    }

    public JTable getJTable() {
        return tableComponent;
    }

    public Font getValueFont() {
        return Fonts.VALUE_FONT;
    }

    public String formatValue(float value) {
        return String.format(valueFormat, value);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        int[] selectedRows = tableComponent.getSelectedRows();
        int[] selectedColumns = tableComponent.getSelectedColumns();
        if ((selectedRows.length <= 1 && selectedColumns.length <= 1)) {
            return;
        }

        Object value = ((CellEditor)e.getSource()).getCellEditorValue();
        if (value == null) {
            return;
        }

        processSelection((x) -> (Float) value);
    }

    @Override
    public void editingCanceled(ChangeEvent e) {

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
            c.setFont(Fonts.VALUE_FONT);

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

    private class TableCellEditor extends DefaultCellEditor implements javax.swing.table.TableCellEditor, KeyListener {
        private final JTextField textField;

        private TableCellEditor(JTextField textField) {
            super(textField);
            this.textField = textField;
            this.textField.addKeyListener(this);
            this.textField.setInputVerifier(new InputVerifier() {
                @Override
                public boolean verify(JComponent input) {
                    try {
                        String text = ((JTextField)input).getText();
                        if (text.isBlank()) {
                            return true;
                        }
                        Float.parseFloat(text);
                        return true;
                    } catch (Exception ex) {
                        return false;
                    }
                }
            });
        }

        private TableCellEditor() {
            this(new JTextField());
            addCellEditorListener(TableEditor.this);
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return super.isCellEditable(anEvent);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.textField.setFont(getValueFont());
            this.textField.setText("");
            SwingUtilities.invokeLater(this.textField::grabFocus);
            return textField;
        }

        @Override
        public Object getCellEditorValue() {
            if (textField.getText().isBlank()) {
                return null;
            }

            return Float.parseFloat(textField.getText());
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }
}
