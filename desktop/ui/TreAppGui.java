package desktop.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import core.SessionLog;
import core.SessionAttribute;
import core.application.SessionService;

public class TreAppGui extends JFrame {
    private SessionService sessionService;

    private JTextField durationField;
    private JTextField moodField;
    private JTextArea notesArea;
    private JButton saveSessionButton;
    private Set<SessionAttribute> selectedAttributes;

    public TreAppGui(SessionService sessionService) {
        this.sessionService = sessionService;
        this.selectedAttributes = new HashSet<>();

        // Frame setup
        setTitle("Tre App - Session Logger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        // Create main panel with BoxLayout for vertical stacking
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Duration section
        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        durationPanel.add(new JLabel("Duration (minutes):"));
        durationField = new JTextField(10);
        durationPanel.add(durationField);
        mainPanel.add(durationPanel);

        // Mood section
        JPanel moodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        moodPanel.add(new JLabel("Mood (1-10):"));
        moodField = new JTextField(10);
        moodPanel.add(moodField);
        mainPanel.add(moodPanel);

        // Attributes section
        mainPanel.add(new JLabel("Attributes:"));
        JPanel attributesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        for (SessionAttribute attr : SessionAttribute.values()) {
            JButton attrButton = new JButton(attr.getName());
            attrButton.addActionListener(e -> toggleAttribute(attr, attrButton));
            attributesPanel.add(attrButton);
        }
        mainPanel.add(attributesPanel);

        // Notes section
        mainPanel.add(new JLabel("Notes:"));
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(notesArea);
        mainPanel.add(scrollPane);

        // Save button
        JPanel buttonPanel = new JPanel();
        saveSessionButton = new JButton("Save Session");
        saveSessionButton.addActionListener(e -> handleSaveSession());
        buttonPanel.add(saveSessionButton);
        mainPanel.add(buttonPanel);

        // Add main panel to frame
        add(mainPanel);
        setVisible(true);
    }

    private void toggleAttribute(SessionAttribute attr, JButton button) {
        if (selectedAttributes.contains(attr)) {
            selectedAttributes.remove(attr);
            button.setBackground(null);
            button.setOpaque(false);
        } else {
            selectedAttributes.add(attr);
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
            moodField.setText("");
            notesArea.setText("");
            selectedAttributes.clear();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for duration and mood.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving session: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
