package desktop.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import core.SessionLog;
import core.AttributeType;
import core.application.SessionService;
import core.SessionAttribute;

public class TreAppGui extends JFrame {
    private static final int DEFAULT_VALUE = 0; // Assuming a default value for attributes
    private SessionService sessionService;
    private Set<SessionAttribute> selectedAttributes;

    private JTextField durationField;
    private JTextArea notesArea;
    private JButton saveSessionButton;

    public TreAppGui(SessionService sessionService) {
        this.sessionService = sessionService;
        this.selectedAttributes = new HashSet<>();

        setupFrame();
        
        JPanel mainPanel = createMainPanel();
        add(mainPanel);
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("Tre App - Session Logger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainPanel.add(createDurationPanel());
        mainPanel.add(createAttributesPanel());
        mainPanel.add(createNotesPanel());
        mainPanel.add(createButtonPanel());

        return mainPanel;
    }

    private JPanel createDurationPanel() {
        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        durationPanel.add(new JLabel("Duration (minutes):"));
        durationField = new JTextField(10);
        durationPanel.add(durationField);
        return durationPanel;
    }

    private JPanel createAttributesPanel() {
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.add(new JLabel("Attributes:"));
        
        JPanel attributesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        for (AttributeType attr : AttributeType.values()) {
            JButton attrButton = new JButton(attr.getName());
            attrButton.addActionListener(e -> toggleAttribute(attr, attrButton));
            attributesPanel.add(attrButton);
        }
        containerPanel.add(attributesPanel);
        return containerPanel;
    }

    private JPanel createNotesPanel() {
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.add(new JLabel("Notes:"));
        
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(notesArea);
        containerPanel.add(scrollPane);
        return containerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        saveSessionButton = new JButton("Save Session");
        saveSessionButton.addActionListener(e -> handleSaveSession());
        buttonPanel.add(saveSessionButton);
        return buttonPanel;
    }

    private void toggleAttribute(AttributeType attr, JButton button) {
        SessionAttribute sessionAttr = new SessionAttribute(attr, DEFAULT_VALUE); 
        if (selectedAttributes.contains(sessionAttr)) {
            selectedAttributes.remove(sessionAttr);
            button.setBackground(null);
            button.setOpaque(false);
        } else {
            selectedAttributes.add(sessionAttr);
            button.setBackground(new Color(173, 216, 230));
            button.setOpaque(true);
        }
    }
    private void handleSaveSession() {
        try {
            // Parse duration
            String durationText = durationField.getText().trim();
            if (durationText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a duration.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int duration = Integer.parseInt(durationText);

            // Get notes
            String notes = notesArea.getText();

            // Create SessionLog with selected attributes
            ArrayList<SessionAttribute> attributes = new ArrayList<>(selectedAttributes);
            SessionLog log = new SessionLog(LocalDate.now(), duration, attributes, notes);

            // Execute use case
            sessionService.saveSession(log);

            // Show success message
            JOptionPane.showMessageDialog(this, "Session saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear fields
            durationField.setText("");
            notesArea.setText("");
            selectedAttributes.clear();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for duration.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving session: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
