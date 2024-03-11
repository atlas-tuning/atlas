package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.component.window.CANDebugPage;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.component.window.CANDebugWindow;
import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;

public class CANDebugToolbar extends Toolbar<CANDebugWindow> {
    private JButton pauseButton;
    private JButton downButton;
    private JButton clearButton;
    private JButton saveButton;
    private JButton authButton;
    private JButton clearDTCButton;
    private JButton readDTCButton;
    private JButton readMemoryButton;

    private JLabel statusLabel;

    public CANDebugToolbar(CANDebugWindow editor) {
        super(editor);
    }

    @Override
    protected void preInitComponent(JToolBar toolbar) {
        super.preInitComponent(toolbar);

        toolbar.setOrientation(JToolBar.HORIZONTAL);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY.darker()));
    }

    @Override
    protected void initComponent(JToolBar toolbar) {
        toolbar.add(pauseButton = makeButton(CarbonIcons.RECORDING_FILLED, "record", "Record CAN session", (e) -> {
            getParent().toggleRecording();
        }));

        toolbar.add(saveButton = makeButton(FontAwesomeSolid.SAVE, "save", "Export session...", (e) -> {
            getParent().saveSession();
        }));

        toolbar.addSeparator();

        toolbar.add(downButton = makeButton(FontAwesomeSolid.ANGLE_DOUBLE_DOWN, "down", "Jump to latest", (e) -> {
            getParent().jumpToLatest();
        }));

        toolbar.add(clearButton = makeButton(FontAwesomeSolid.TRASH, "down", "Clear CAN frames", (e) -> {
            getParent().clearFrames();
        }));

        toolbar.addSeparator();

        toolbar.add(authButton = makeButton(FontAwesomeSolid.HANDSHAKE, "mode", "Change mode...", (e) -> {
            getParent().changeMode();
        }));

        toolbar.add(clearDTCButton = makeButton(FontAwesomeSolid.ERASER, "clearDTC", "Clear DTC...", (e) -> {
            getParent().clearDTC();
        }));

        toolbar.add(readDTCButton = makeButton(FontAwesomeSolid.GLASSES, "readDTC", "Read DTC", (e) -> {
            getParent().readDTC();
        }));

        toolbar.add(readMemoryButton = makeButton(FontAwesomeSolid.MICROCHIP, "readMemory", "Read Memory...", (e) -> {
            getParent().readMemory();
        }));

        toolbar.addSeparator();

        toolbar.add(Box.createHorizontalGlue());

        toolbar.add(statusLabel = new JLabel());
        statusLabel.setForeground(Fonts.getTextColor().darker());

        toolbar.add(Box.createHorizontalStrut(5));

        update();
    }

    public void setPaused(boolean paused) {
        boolean recording = getParent().isRecording();
        if (recording) {
            pauseButton.setIcon(Icons.get(CarbonIcons.STOP_FILLED, BUTTON_ICON_SIZE));
        } else {
            pauseButton.setIcon(Icons.get(CarbonIcons.RECORDING_FILLED, BUTTON_ICON_SIZE));
        }

        if (paused) {
            pauseButton.setToolTipText("Record CAN session");
        } else {
            pauseButton.setToolTipText("Stop recording");
        }
    }

    public void update() {
        CANDebugPage activePage = getParent().getActivePage(),
                recordingPage = getParent().getRecordingPage();
        saveButton.setEnabled(activePage != null);
        downButton.setEnabled(activePage != null);

        boolean isRecordingPageFocused = activePage != null && recordingPage == activePage;
        clearButton.setEnabled(isRecordingPageFocused);
        authButton.setEnabled(isRecordingPageFocused);
        clearDTCButton.setEnabled(isRecordingPageFocused);
        readDTCButton.setEnabled(isRecordingPageFocused);
        readMemoryButton.setEnabled(isRecordingPageFocused);

        if (isRecordingPageFocused) {
            statusLabel.setVisible(true);
            statusLabel.setText("Captured " + recordingPage.getTotalFrames() + " frames");
        } else if (activePage != null) {
            statusLabel.setVisible(true);
            statusLabel.setText("Displaying " + activePage.getTotalFrames() + " frames");
        } else {
            statusLabel.setVisible(false);
        }
    }
}
