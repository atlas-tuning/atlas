package com.github.manevolent.atlas.ui.component;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/*
 *	Use a JTable as a renderer for row numbers of a given main table.
 *  This table must be added to the row header of the scrollpane that
 *  contains the main table.
 */
public class RowNumberTable extends JTable
        implements ChangeListener, PropertyChangeListener, TableModelListener {
    private JTable main;
    private java.util.List<String> rowNames;

    public RowNumberTable(JTable table, java.util.List<String> listRowNames) {
        rowNames = listRowNames;
        main = table;
        main.addPropertyChangeListener(this);
        main.getModel().addTableModelListener(this);

        setFocusable(false);
        setAutoCreateColumnsFromModel(false);
        setSelectionModel(main.getSelectionModel());


        TableColumn column = new TableColumn();
        column.setHeaderValue(" ");
        addColumn(column);
        column.setCellRenderer(new RowNumberRenderer());

        updateWidth();
    }

    public void updateWidth() {
        int minimumWidth = rowNames.stream()
                .mapToInt(text -> {
                    return getFontMetrics(tableHeader.getFont()).stringWidth(text);
                })
                .max().orElse(50);

        minimumWidth += 10;
        minimumWidth += getColumnModel().getColumnMargin();

        getColumnModel().getColumn(0).setPreferredWidth(minimumWidth);
        getColumnModel().getColumn(0).setMinWidth(minimumWidth);

        getMinimumSize().setSize(minimumWidth, getMinimumSize().getHeight());
        getPreferredSize().setSize(minimumWidth, getPreferredSize().getHeight());
        setPreferredScrollableViewportSize(getPreferredSize());
    }

    public void updateRowNames(java.util.List<String> rowNames) {
        this.rowNames = rowNames;
        updateWidth();
        revalidate();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        Component c = getParent();

        //  Keep scrolling of the row table in sync with the main table.

        if (c instanceof JViewport) {
            JViewport viewport = (JViewport) c;
            viewport.addChangeListener(this);
        }
    }

    /*
     *  Delegate method to main table
     */
    @Override
    public int getRowCount() {
        return main.getRowCount();
    }

    @Override
    public int getRowHeight(int row) {
        int rowHeight = main.getRowHeight(row);

        if (rowHeight != super.getRowHeight(row)) {
            super.setRowHeight(row, rowHeight);
        }

        return rowHeight;
    }

    /*
     *  No model is being used for this table so just use the row number
     *  as the value of the cell.
     */
    @Override
    public Object getValueAt(int row, int column) {
        return rowNames.get(row);
        // return Integer.toString(row + 1);
    }

    /*
     *  Don't edit data in the main TableModel by mistake
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    /*
     *  Do nothing since the table ignores the model
     */
    @Override
    public void setValueAt(Object value, int row, int column) {
    }

    public void stateChanged(ChangeEvent e) {
        //  Keep the scrolling of the row table in sync with main table

        JViewport viewport = (JViewport) e.getSource();
        JScrollPane scrollPane = (JScrollPane) viewport.getParent();
        scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
    }

    public void propertyChange(PropertyChangeEvent e) {
        //  Keep the row table in sync with the main table

        if ("selectionModel".equals(e.getPropertyName())) {
            setSelectionModel(main.getSelectionModel());
        }

        if ("rowHeight".equals(e.getPropertyName())) {
            repaint();
        }

        if ("model".equals(e.getPropertyName())) {
            main.getModel().addTableModelListener(this);
            revalidate();
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        revalidate();
    }

    /*
     *  Attempt to mimic the table header renderer
     */
    private static class RowNumberRenderer extends DefaultTableCellRenderer {
        public RowNumberRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
        }

        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (table != null) {
                JTableHeader header = table.getTableHeader();

                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            setText((value == null) ? "" : value.toString());
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));

            return this;
        }
    }
}