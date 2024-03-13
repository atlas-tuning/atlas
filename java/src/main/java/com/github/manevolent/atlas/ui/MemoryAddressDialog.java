package com.github.manevolent.atlas.ui;

import com.github.manevolent.atlas.model.MemoryAddress;
import com.github.manevolent.atlas.model.MemorySection;
import com.github.manevolent.atlas.model.MemoryType;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HexFormat;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.manevolent.atlas.ui.util.Inputs.memorySectionField;

public class MemoryAddressDialog extends JDialog {
    private final java.util.List<MemorySection> sections;
    private final Consumer<MemoryAddress> valueChanged;
    private MemorySection section;
    private long offset;
    private static final char[] VALID_HEX_CHARACTERS = new char[]
            {'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private JTextField memoryAddressField;

    private JComboBox<MemorySection> sectionField;
    private boolean canceled = false;

    public MemoryAddressDialog(java.util.List<MemorySection> sections, MemoryAddress address, Frame parent,
                               Consumer<MemoryAddress> valueChanged) {
        super(parent, "Enter Address", true);

        this.sections = sections;
        this.valueChanged = valueChanged;

        this.section = address != null ? address.getSection() : getDefaultSection();
        this.offset = address != null ? address.getOffset() : section.getBaseAddress();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                canceled = true;

                super.windowClosing(e);
            }
        });
        setType(Type.POPUP);
        initComponent();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
        setModal(true);
        setMinimumSize(new Dimension(300, getMinimumSize().height));
        memoryAddressField.grabFocus();
    }

    private MemorySection getDefaultSection() {
        return sections.getFirst();
    }

    private MemorySection getMemorySection(long offset) {
        return sections.stream()
                .filter(section -> section.getBaseAddress() <= offset &&
                section.getBaseAddress() + section.getDataLength() >= offset)
                .findFirst()
                .orElse(null);
    }

    private JTextField createMemoryAddressField(Consumer<Boolean> inputValid, Runnable enter) {
        String defaultValue = "0x" + HexFormat.of().toHexDigits((int) (offset & 0xFFFFFFFF)).toUpperCase();
        memoryAddressField = Inputs.textField(defaultValue, (newValue) -> {
            if (newValue.isBlank()) {
                inputValid.accept(false);
                return;
            }

            try {
                if (newValue.toLowerCase().startsWith("0x")) {
                    offset = HexFormat.fromHexDigits(newValue.substring(2)) & 0xFFFFFFFFL;

                    MemorySection suggested = getMemorySection(offset);
                    if (suggested != null && this.section != suggested) {
                        this.section = suggested;
                        sectionField.setSelectedItem(suggested);
                    } else if (offset < section.getBaseAddress()) {
                        inputValid.accept(false);
                        return;
                    } else if (offset > section.getBaseAddress() + section.getDataLength()) {
                        offset = section.getBaseAddress() + section.getDataLength() - 1;
                        SwingUtilities.invokeLater(() -> memoryAddressField.setText("0x" + HexFormat.of().toHexDigits(
                                (int) (offset)).toUpperCase()));
                    }
                } else {
                    try {
                        offset = Long.parseLong(newValue);
                    } catch (NumberFormatException ex) {
                        if (!newValue.isEmpty()) {
                            offset = HexFormat.fromHexDigits(newValue);
                            SwingUtilities.invokeLater(() -> memoryAddressField.setText("0x" +
                                    Integer.toHexString((int) (offset)).toUpperCase()));
                        }
                    }

                    MemorySection suggested = getMemorySection(offset);
                    if (suggested != null && this.section != suggested) {
                        this.section = suggested;
                        sectionField.setSelectedItem(suggested);
                    } else if (offset < section.getBaseAddress()) {
                        inputValid.accept(false);
                        return;
                    } else if (offset > section.getBaseAddress() + section.getDataLength()) {
                        offset = section.getBaseAddress() + section.getDataLength() - 1;
                        SwingUtilities.invokeLater(() -> memoryAddressField.setText(Long.toString(offset)));
                    }
                }

                inputValid.accept(true);
            } catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    memoryAddressField.setText("");
                });
                inputValid.accept(false);
            }
        });
        memoryAddressField.setFont(Fonts.VALUE_FONT);
        memoryAddressField.addActionListener((e) -> {
            accept();
        });
        memoryAddressField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancel();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isActionKey()) {
                    e.consume();
                    accept();
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
                    e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_LEFT) {
                    return;
                }

                if (e.getKeyChar() == 'x') {
                    if (memoryAddressField.getText().equals("0")) {
                        // This is acceptable (hex string)
                        return;
                    }
                }

                boolean selectedMultiple = memoryAddressField.getSelectionEnd()
                        - memoryAddressField.getSelectionStart() > 1;
                if (!selectedMultiple && memoryAddressField.getText().toLowerCase().startsWith("0x")
                        && memoryAddressField.getText().length() >= 10) {
                    e.consume();
                    return;
                }

                if (!memoryAddressField.getText().startsWith("0x") &&
                        Character.toString(e.getKeyChar()).matches("[a-fA-F]")) {
                    memoryAddressField.setText("0x" + memoryAddressField.getText());
                }

                char[] valid = VALID_HEX_CHARACTERS;
                for (int i = 0; i < valid.length; i ++) {
                    if (e.getKeyChar() == valid[i] ||
                        e.getKeyChar() == Character.toLowerCase(valid[i])) {
                        e.setKeyChar(Character.toUpperCase(valid[i]));
                        return;
                    }
                }

                e.consume();
            }
        });
        return memoryAddressField;
    }

    private void accept() {
        MemoryAddress address = MemoryAddress.builder().withOffset(offset).withSection(section).build();
        valueChanged.accept(address);
        dispose();
        canceled = false;
    }

    private void cancel() {
        canceled = true;
        dispose();
    }

    private void initComponent() {
        JPanel content = Inputs.createEntryPanel();
        JButton ok = Inputs.button(CarbonIcons.CHECKMARK, "OK", null, this::accept);

        JTextField addressField = createMemoryAddressField(ok::setEnabled, this::accept);
        Inputs.createEntryRow(content, 2, "Address", "The address relative to the selected region",
                addressField);

        Inputs.createEntryRow(content, 1, "Region", "The section this address resides in",
                sectionField = memorySectionField(sections, section, (newSection) -> {
                    this.section = newSection;
                    if (this.offset < newSection.getBaseAddress() ||
                            this.offset > newSection.getBaseAddress() + newSection.getDataLength()) {
                        addressField.setText("0x" + HexFormat.of().toHexDigits(
                                (int) (section.getBaseAddress() & 0xFFFFFFFF)).toUpperCase());
                    }
                }));

        JButton cancel = Inputs.button("Cancel", this::cancel);
        Inputs.createButtonRow(content, 3, cancel, ok);

        getContentPane().add(content);
        addressField.transferFocus();
    }

    public MemoryAddress getAddress() {
        if (!canceled) {
            return MemoryAddress.builder()
                    .withSection(section)
                    .withOffset(offset)
                    .build();
        } else {
            return null;
        }
    }

    public static MemoryAddress show(Frame parent, java.util.List<MemorySection> sections, MemoryAddress current) {
        MemoryAddressDialog dialog = new MemoryAddressDialog(sections, current, parent, (ignored) -> { });
        dialog.setVisible(true);
        return dialog.getAddress();
    }

    public static MemoryAddress show(Frame parent, java.util.List<MemorySection> sections, MemoryAddress current,
                            Consumer<MemoryAddress> changed) {
        MemoryAddressDialog dialog = new MemoryAddressDialog(sections, current, parent, changed);
        dialog.setVisible(true);
        return dialog.getAddress();
    }

    public static MemoryAddress show(Frame parent, java.util.List<MemorySection> sections,
                            Predicate<MemorySection> predicate,
                            MemoryAddress current,
                            Consumer<MemoryAddress> changed) {
        sections = sections.stream().filter(predicate).toList();
        return show(parent, sections.stream().filter(predicate).toList(), current, changed);
    }

    public static MemoryAddress show(Frame parent, java.util.List<MemorySection> sections,
                            boolean localOnly,
                            MemoryAddress current,
                            Consumer<MemoryAddress> changed) {
        return show(parent, sections, section -> !localOnly || section.getMemoryType() != MemoryType.RAM,
                current, changed);
    }
}
