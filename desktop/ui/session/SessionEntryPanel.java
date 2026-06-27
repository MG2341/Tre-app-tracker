package desktop.ui.session;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import core.AttributeType;
import core.SessionAttribute;
import core.SessionLog;
import core.application.SessionService;

public class SessionEntryPanel extends JPanel {
    private final SessionService sessionService;
    private final Map<AttributeType, SessionAttribute> selectedAttributes = new LinkedHashMap<>();
    private final Map<AttributeType, JButton> attributeButtons = new LinkedHashMap<>();
    private final JRadioButton manualModeButton = new JRadioButton("Manual");
    private final JRadioButton timerModeButton = new JRadioButton("Timer", true);
    private final JTextField durationField = new JTextField(10);
    private final JTextField dateField = new JTextField(10);
    private final JTextArea notesArea = new JTextArea(4, 20);
    private final JCheckBox addStartTimeCheckBox = new JCheckBox("Add start time");
    private final JSpinner startTimeSpinner = createStartTimeSpinner();
    private final SessionTimerPanel sessionTimerPanel;
    private final JPanel manualStartTimePanel;

    public SessionEntryPanel(SessionService sessionService) {
        this.sessionService = sessionService;
        this.sessionTimerPanel = new SessionTimerPanel(this::parseDuration, this::onTimerStarted, this::onTimerStopped);
        this.manualStartTimePanel = createManualStartTimePanel();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(createDatePanel());
        add(createSessionModePanel());
        add(createDurationPanel());
        add(manualStartTimePanel);
        add(sessionTimerPanel);
        add(createAttributesPanel());
        add(createNotesPanel());
        add(createSaveButtonPanel());
        add(Box.createVerticalGlue());

        updateModeVisibility();
        updateManualStartTimeControls();
    }

    private JPanel createDatePanel() {
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Date:"));
        datePanel.add(dateField);
        dateField.setText(LocalDate.now().toString());
        dateField.setEditable(true);

        return datePanel;
    }

    private JPanel createSessionModePanel() {
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(new JLabel("Session mode:"));

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(manualModeButton);
        modeGroup.add(timerModeButton);

        manualModeButton.addActionListener(e -> updateModeVisibility());
        timerModeButton.addActionListener(e -> updateModeVisibility());

        modePanel.add(manualModeButton);
        modePanel.add(timerModeButton);
        return modePanel;
    }

    private JPanel createDurationPanel() {
        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        durationPanel.add(new JLabel("Duration (minutes):"));
        durationPanel.add(durationField);
        return durationPanel;
    }

    private JPanel createManualStartTimePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(addStartTimeCheckBox);
        panel.add(new JLabel("Start time:"));
        panel.add(startTimeSpinner);

        addStartTimeCheckBox.addActionListener(e -> updateManualStartTimeControls());
        return panel;
    }

    private JPanel createAttributesPanel() {
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.add(new JLabel("Attributes:"));

        JPanel attributesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        for (AttributeType attributeType : AttributeType.values()) {
            JButton attributeButton = createAttributeButton(attributeType);
            attributeButtons.put(attributeType, attributeButton);
            attributesPanel.add(attributeButton);
        }

        containerPanel.add(attributesPanel);
        return containerPanel;
    }

    private JButton createAttributeButton(AttributeType attributeType) {
        JButton button = new JButton(attributeType.getName());
        button.addActionListener(e -> toggleAttribute(attributeType));
        return button;
    }

    private JPanel createNotesPanel() {
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.add(new JLabel("Notes:"));

        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        containerPanel.add(new JScrollPane(notesArea));
        return containerPanel;
    }

    private JPanel createSaveButtonPanel() {
        JPanel buttonPanel = new JPanel();
        JButton saveSessionButton = new JButton("Save Session");
        saveSessionButton.addActionListener(e -> handleSaveSession());
        buttonPanel.add(saveSessionButton);
        return buttonPanel;
    }

    private JSpinner createStartTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
        spinner.setEnabled(false);
        return spinner;
    }

    private void updateModeVisibility() {
        boolean timerModeSelected = timerModeButton.isSelected();
        manualStartTimePanel.setVisible(!timerModeSelected);
        sessionTimerPanel.setVisible(timerModeSelected);
        revalidate();
        repaint();
    }

    private void updateManualStartTimeControls() {
        startTimeSpinner.setEnabled(addStartTimeCheckBox.isSelected() && manualModeButton.isSelected());
    }

    private void onTimerStarted() {
        durationField.setEditable(false);
        addStartTimeCheckBox.setEnabled(false);
        startTimeSpinner.setEnabled(false);
    }

    private void onTimerStopped() {
        durationField.setEditable(true);
        addStartTimeCheckBox.setEnabled(true);
        updateManualStartTimeControls();
    }

    private void toggleAttribute(AttributeType attributeType) {
        JButton button = attributeButtons.get(attributeType);
        if (button == null) {
            return;
        }

        if (selectedAttributes.containsKey(attributeType)) {
            selectedAttributes.remove(attributeType);
            updateDeselectedButtonStyle(button);
        } else {
            selectedAttributes.put(attributeType, new SessionAttribute(attributeType));
            updateSelectedButtonStyle(button);
        }
    }

    private void updateSelectedButtonStyle(JButton button) {
        button.setBackground(new Color(173, 216, 230));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }

    private void updateDeselectedButtonStyle(JButton button) {
        button.setBackground(UIManager.getColor("Button.background"));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }

    private void handleSaveSession() {
        try {
            SessionLog log = createSessionLog();
            sessionService.saveSession(log);
            JOptionPane.showMessageDialog(this, "Session saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid duration.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving session: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private SessionLog createSessionLog() {
        int duration = parseDuration();
        LocalDate date = parseDate();
        String notes = notesArea.getText();
        ArrayList<SessionAttribute> attributes = new ArrayList<>(selectedAttributes.values());

        if (timerModeButton.isSelected()) {
            LocalTime timerStartTime = sessionTimerPanel.getStartTime();
            if (timerStartTime == null) {
                throw new IllegalArgumentException("Press Start before saving a timer session.");
            }

            int configuredDuration = sessionTimerPanel.getConfiguredDurationMinutes();
            return new SessionLog(date, timerStartTime, timerStartTime.plusMinutes(configuredDuration), configuredDuration, attributes, notes);
        }

        if (addStartTimeCheckBox.isSelected()) {
            LocalTime startTime = parseManualStartTime();
            return new SessionLog(date, startTime, startTime.plusMinutes(duration), duration, attributes, notes);
        }

        return new SessionLog(date, duration, attributes, notes);
    }

    private int parseDuration() {
        String durationText = durationField.getText().trim();
        if (durationText.isEmpty()) {
            throw new IllegalArgumentException("Please enter a duration.");
        }
        return Integer.parseInt(durationText);
    }

    private LocalTime parseManualStartTime() {
        if (!addStartTimeCheckBox.isSelected()) {
            return null;
        }

        Date selectedDate = (Date) startTimeSpinner.getValue();
        return LocalDateTime.ofInstant(selectedDate.toInstant(), ZoneId.systemDefault()).toLocalTime();
    }

    private LocalDate parseDate() {
        String dateText = dateField.getText().trim();
        if (dateText.isEmpty()) {
            throw new IllegalArgumentException("Please enter a date.");
        }

        return LocalDate.parse(dateText);
    }

    private void clearForm() {
        durationField.setText("");
        notesArea.setText("");
        selectedAttributes.clear();
        resetAttributeButtons();
        addStartTimeCheckBox.setSelected(false);
        startTimeSpinner.setValue(new Date());
        timerModeButton.setSelected(true);
        sessionTimerPanel.resetTimer();
        updateManualStartTimeControls();
        updateModeVisibility();
    }

    private void resetAttributeButtons() {
        for (JButton button : attributeButtons.values()) {
            updateDeselectedButtonStyle(button);
        }
    }
}