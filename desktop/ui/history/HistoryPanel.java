package desktop.ui.history;

import core.AttributeType;
import core.SessionAttribute;
import core.SessionLog;
import core.application.SessionService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryPanel extends JPanel {
    private final SessionService sessionService;
    private final DefaultListModel<SessionLog> sessionListModel = new DefaultListModel<>();
    private final JList<SessionLog> sessionList = new JList<>(sessionListModel);
    private final JTextField dateField = new JTextField(16);
    private final JTextField durationField = new JTextField(16);
    private final JTextField startTimeField = new JTextField(16);
    private final JTextField endTimeField = new JTextField(16);
    private final JTextField attributesField = new JTextField(16);
    private final JTextArea notesArea = new JTextArea(6, 20);
    private final JLabel statusLabel = new JLabel(" ");
    private SessionLog selectedLog;

    public HistoryPanel(SessionService sessionService) {
        this.sessionService = sessionService;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createSplitPane(), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        loadLogs();
    }

    private JSplitPane createSplitPane() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createMasterPanel(), createDetailPanel());
        splitPane.setResizeWeight(0.35);
        splitPane.setDividerLocation(260);
        splitPane.setBorder(null);
        return splitPane;
    }

    private JPanel createMasterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        JLabel title = new JLabel("Past Sessions");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        sessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sessionList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(formatSessionListItem(value));
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            label.setBackground(isSelected ? new Color(220, 235, 250) : Color.WHITE);
            label.setForeground(Color.BLACK);
            return label;
        });
        sessionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedSession(sessionList.getSelectedValue());
            }
        });

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(sessionList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        JLabel title = new JLabel("Session Details");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;

        addFieldRow(formPanel, constraints, 0, "Date (yyyy-MM-dd):", dateField);
        addFieldRow(formPanel, constraints, 1, "Duration (minutes):", durationField);
        addFieldRow(formPanel, constraints, 2, "Start Time (HH:mm):", startTimeField);
        addFieldRow(formPanel, constraints, 3, "End Time (HH:mm):", endTimeField);
        addFieldRow(formPanel, constraints, 4, "Attributes (name:value;...):", attributesField);

        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.weightx = 0;
        formPanel.add(new JLabel("Notes:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1.0;
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(notesArea), constraints);

        JButton updateButton = new JButton("Update Session");
        updateButton.addActionListener(e -> updateSelectedSession());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(updateButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void addFieldRow(JPanel panel, GridBagConstraints constraints, int row, String labelText, JTextField field) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        panel.add(new JLabel(labelText), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1.0;
        panel.add(field, constraints);
    }

    private void loadLogs() {
        sessionListModel.clear();
        List<SessionLog> logs = sessionService.getAllSessions();
        for (SessionLog log : logs) {
            sessionListModel.addElement(log);
        }

        if (!sessionListModel.isEmpty()) {
            sessionList.setSelectedIndex(0);
        } else {
            clearForm();
            selectedLog = null;
            statusLabel.setText("No saved sessions found.");
        }
    }

    private void showSelectedSession(SessionLog log) {
        selectedLog = log;
        if (log == null) {
            clearForm();
            return;
        }

        dateField.setText(log.getDate() == null ? "" : log.getDate().toString());
        durationField.setText(String.valueOf(log.getDurationMinutes()));
        startTimeField.setText(log.getStartTime() == null ? "" : log.getStartTime().toString());
        endTimeField.setText(log.getEndTime() == null ? "" : log.getEndTime().toString());
        attributesField.setText(formatAttributes(log.getAttributes()));
        notesArea.setText(log.getNotes() == null ? "" : log.getNotes());
        statusLabel.setText("Editing session from " + log.getDate());
    }

    private void updateSelectedSession() {
        if (selectedLog == null) {
            statusLabel.setText("Select a session first.");
            return;
        }

        try {
            SessionLog updatedLog = createUpdatedLog();
            sessionService.updateSession(updatedLog);
            loadLogs();
            selectLogById(updatedLog.getId());
            statusLabel.setText("Session updated successfully.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private SessionLog createUpdatedLog() {
        LocalDate date = LocalDate.parse(dateField.getText().trim());
        int duration = Integer.parseInt(durationField.getText().trim());
        LocalTime startTime = parseOptionalTime(startTimeField.getText().trim());
        LocalTime endTime = parseOptionalTime(endTimeField.getText().trim());
        ArrayList<SessionAttribute> attributes = parseAttributes(attributesField.getText().trim());
        String notes = notesArea.getText();

        SessionLog updatedLog;
        if (startTime != null || endTime != null) {
            updatedLog = new SessionLog(date, startTime, endTime, duration, attributes, notes, selectedLog.getId());
        } else {
            updatedLog = new SessionLog(date, duration, attributes, notes, selectedLog.getId());
        }
        updatedLog.setId(selectedLog.getId());
        return updatedLog;
    }

    private LocalTime parseOptionalTime(String rawValue) {
        if (rawValue.isEmpty()) {
            return null;
        }
        return LocalTime.parse(rawValue);
    }

    private ArrayList<SessionAttribute> parseAttributes(String rawValue) {
        ArrayList<SessionAttribute> attributes = new ArrayList<>();
        if (rawValue.isEmpty()) {
            return attributes;
        }

        String[] pairs = rawValue.split(";");
        Map<String, AttributeType> lookup = buildAttributeLookup();
        for (String pair : pairs) {
            String trimmedPair = pair.trim();
            if (trimmedPair.isEmpty()) {
                continue;
            }

            String[] parts = trimmedPair.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid attribute entry: " + trimmedPair);
            }

            AttributeType attributeType = lookup.get(parts[0].trim().toLowerCase());
            if (attributeType == null) {
                throw new IllegalArgumentException("Unknown attribute: " + parts[0].trim());
            }

            int value = Integer.parseInt(parts[1].trim());
            attributes.add(new SessionAttribute(attributeType, value));
        }

        return attributes;
    }

    private Map<String, AttributeType> buildAttributeLookup() {
        Map<String, AttributeType> lookup = new HashMap<>();
        for (AttributeType attributeType : AttributeType.values()) {
            lookup.put(attributeType.getName().toLowerCase(), attributeType);
            lookup.put(attributeType.name().toLowerCase(), attributeType);
        }
        return lookup;
    }

    private String formatAttributes(List<SessionAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < attributes.size(); i++) {
            SessionAttribute attribute = attributes.get(i);
            if (i > 0) {
                builder.append("; ");
            }
            builder.append(attribute.getAttributeType().getName()).append(":").append(attribute.getValue());
        }
        return builder.toString();
    }

    private String formatSessionListItem(SessionLog log) {
        if (log == null) {
            return "";
        }
        return log.getDate() + " - " + log.getDurationMinutes() + " min";
    }

    private void selectLogById(String id) {
        for (int i = 0; i < sessionListModel.size(); i++) {
            SessionLog log = sessionListModel.get(i);
            if (log.getId().equals(id)) {
                sessionList.setSelectedIndex(i);
                sessionList.ensureIndexIsVisible(i);
                return;
            }
        }
    }

    private void clearForm() {
        dateField.setText("");
        durationField.setText("");
        startTimeField.setText("");
        endTimeField.setText("");
        attributesField.setText("");
        notesArea.setText("");
    }
}
