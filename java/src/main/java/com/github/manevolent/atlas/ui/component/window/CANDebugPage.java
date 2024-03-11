package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.protocol.uds.UDSFrame;
import com.github.manevolent.atlas.protocol.uds.UDSRequest;
import com.github.manevolent.atlas.protocol.uds.UDSResponse;
import com.github.manevolent.atlas.protocol.uds.UDSUnknownBody;
import com.github.manevolent.atlas.protocol.uds.response.UDSNegativeResponse;
import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.util.Csv;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.component.ColumnsAutoSizer;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CANDebugPage extends JPanel implements AdjustmentListener {
    private final CANDebugWindow window;
    private JScrollPane scrollPane;

    private final List<UDSFrame> frames = new LinkedList<>();
    private boolean paused = true;

    private DefaultTableModel model;
    private JTable table;

    private String name;
    private boolean locked = true;
    private boolean adding = false;
    private boolean selfAdjusting = false;

    public CANDebugPage(CANDebugWindow window) {
        this.window = window;
        this.name = Instant.now().toString();

        initComponent();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return this.name;
    }

    public List<UDSFrame> getFrames() {
        return frames;
    }

    public void activated() {
        window.getToolbar().setPaused(paused);
    }

    public void deactivated() {

    }

    private void initFooterPanel() {

    }

    public int getTotalFrames() {
        return model.getRowCount();
    }

    public Editor getEditor() {
        return window.getParent();
    }

    public void reload() {
        initFooterPanel();
    }

    private void resizeLastColumn() {
        TableColumnModel model = table.getColumnModel();;
        int totalWidth = model.getTotalColumnWidth();
        if (model.getTotalColumnWidth() < table.getWidth()) {
            TableColumn column = model.getColumn(model.getColumnCount() - 1);
            int remainingWidth = table.getWidth() - totalWidth;
            column.setWidth(column.getWidth() + remainingWidth);
            column.setMaxWidth(column.getWidth() + remainingWidth);
            column.setPreferredWidth(column.getWidth() + remainingWidth);
        }
    }

    public void initComponent() {
        Vector<String> columnNames = new Vector<>(Arrays.asList("", "Time", "Address", "SID", "Type", "Data"));
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model) {
            // See: https://stackoverflow.com/questions/9919230/disable-user-edit-in-jtable
            public boolean isCellEditable(int row, int column) {
                return false;
            };
        };
        table.getModel().addTableModelListener(e -> {
            ColumnsAutoSizer.sizeColumnsToFit(table);
            resizeLastColumn();
        });

        table.setDefaultRenderer(String.class, new Renderer());
        table.setDefaultRenderer(Object.class, new Renderer());

        table.setRowSelectionAllowed(true);
        table.getTableHeader().setVisible(true);
        table.setColumnSelectionAllowed(false);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFont(Fonts.VALUE_FONT);
        table.getTableHeader().setFont(Fonts.VALUE_FONT);

        table.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (adding) {
                    adding = false;
                }

                if (locked) {
                    jumpToLatest();
                }
            }
        });

        table.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeLastColumn();
            }
        });

        reload();

        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(this);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }


    public void clearFrames() {
        SwingUtilities.invokeLater(() -> {
            synchronized (frames) {
                model.setRowCount(0);
                frames.clear();
                window.getToolbar().update();
            }
        });
    }

    public void addFrame(UDSFrame frame) {
        if (paused) {
            return;
        }

        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        boolean isScrollLocked = !scrollBar.isVisible() || locked;

        SwingUtilities.invokeLater(() -> {
            synchronized (frames) {
                frames.add(frame);
                adding = true;
                String serviceIdString;

                try {
                    serviceIdString = "0x" + Frame.toHexString(new byte[] { (byte) (frame.getServiceId() & 0xFF) });
                } catch (Exception ex) {
                    serviceIdString = "??";
                }

                model.addRow(new Vector<>(Arrays.asList(
                        "",
                        Instant.now().toString(),
                        String.format("0x%02X", frame.getAddress().toInt()),
                        serviceIdString,
                        frame.getBody().getClass().getSimpleName(),
                        frame.getBody().toString()
                )));
                table.revalidate();

                window.getToolbar().update();
            }
        });
    }

    public void setPaused(boolean paused, boolean override) {
        if (this.paused == paused) {
            return;
        }

        if (paused && !override) {
            if (JOptionPane.showConfirmDialog(getParent(),
                    "This action will stop the recording. Do you want to end this debug session?",
                    "Recording",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
        }

        this.paused = paused;

        if (paused && window.getRecordingPage() == this) {
            window.stopRecording();
        }

        initFooterPanel();
        window.getToolbar().setPaused(paused);
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void jumpToLatest() {
        table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getAdjustable() != scrollPane.getVerticalScrollBar()) {
            return;
        }

        if (!adding) {
            locked = e.getAdjustable().getValue() ==
                    e.getAdjustable().getMaximum() - e.getAdjustable().getVisibleAmount();
        }
    }

    public void saveSession() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter def = new FileNameExtensionFilter("Comma-separated value file (*.csv)", "csv");
        fileChooser.addChoosableFileFilter(def);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text files (*.txt)", "txt"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Log files (*.log)", "log"));
        fileChooser.setSelectedFile(new File(getName() +  ".csv"));
        fileChooser.setFileFilter(def);
        fileChooser.setDialogTitle("Export Debug Session");
        if (fileChooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();


            int columnOffset = 1;
            try (FileWriter writer = new FileWriter(file)) {
                Collection<String> columnNames = IntStream.range(columnOffset, table.getColumnCount())
                        .mapToObj(n -> table.getColumnModel().getColumn(n))
                        .map(c -> c.getHeaderValue().toString())
                        .toList();

                Csv.writeRow(writer, columnNames);

                for (int row = 0; row < model.getRowCount(); row ++) {
                    Collection<String> cells = new ArrayList<>(model.getColumnCount());
                    for (int col = columnOffset; col < model.getColumnCount(); col ++) {
                        cells.add(model.getValueAt(row, col).toString());
                    }
                    Csv.writeRow(writer, cells);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Log.ui().log(Level.INFO, "Debug session exported to " + file.getPath());
        }
    }

    private class Renderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            component.setFont(Fonts.VALUE_FONT);

            UDSFrame frame = frames.get(row);
            Color color;
            Ikon icon = null;
            if (frame.getDirection() == UDSFrame.Direction.READ) {
                color = Color.GREEN.darker();
                if (column == 0) {
                    icon = CarbonIcons.ARROW_DOWN;
                }
            } else {
                color = Color.GRAY.brighter();
                if (column == 0) {
                    icon = CarbonIcons.ARROW_UP;
                }
            }

            if (frame.getBody() instanceof UDSNegativeResponse) {
                component.setForeground(Color.RED.darker());
            } else if (frame.getBody() instanceof UDSUnknownBody) {
                component.setForeground(Color.YELLOW.darker());
            } else if (frame.getBody() instanceof UDSRequest<?>) {
                component.setForeground(Color.GRAY.brighter());
            } else if (frame.getBody() instanceof UDSResponse) {
                component.setForeground(Color.GREEN.darker());
            } else {
                component.setForeground(color);
            }

            if (icon != null) {
                ((JLabel) component).setIcon(Icons.get(icon, color));
            } else {
                ((JLabel) component).setIcon(null);
            }

            return component;
        }
    }
}