package desktop.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import core.AttributeType;
import core.SessionAttribute;
import core.SessionLog;
import core.application.SessionService;

public class TreAppGui extends JFrame {
    private final SessionService sessionService;
    private final Map<AttributeType, SessionAttribute> selectedAttributes;
    private final Map<AttributeType, JButton> attributeButtons;

    private JTextField durationField;
    private JTextArea notesArea;
    private JButton saveSessionButton;

    public TreAppGui(SessionService sessionService) {
        this.sessionService = sessionService;
        this.selectedAttributes = new LinkedHashMap<>();
        this.attributeButtons = new LinkedHashMap<>();

        setupFrame();

        add(createMainPanel());
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

        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        containerPanel.add(new JScrollPane(notesArea));
        return containerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        saveSessionButton = new JButton("Save Session");
        saveSessionButton.addActionListener(e -> handleSaveSession());
        buttonPanel.add(saveSessionButton);
        return buttonPanel;
    }

    private void toggleAttribute(AttributeType attributeType) {
        JButton button = attributeButtons.get(attributeType);
        if (button == null) {
            return;
        }

        if (selectedAttributes.containsKey(attributeType)) {
            removeAttribute(attributeType, button);
        } else {
            addAttribute(attributeType, button);
        }
    }

    private void addAttribute(AttributeType attributeType, JButton button) {
        selectedAttributes.put(attributeType, new SessionAttribute(attributeType));
        updateSelectedButtonStyle(button);
    }

    private void removeAttribute(AttributeType attributeType, JButton button) {
        selectedAttributes.remove(attributeType);
        updateDeselectedButtonStyle(button);
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
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving session: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private SessionLog createSessionLog() {
        int duration = parseDuration();
        String notes = notesArea.getText();
        ArrayList<SessionAttribute> attributes = new ArrayList<>(selectedAttributes.values());
        return new SessionLog(LocalDate.now(), duration, attributes, notes);
    }

    private int parseDuration() {
        String durationText = durationField.getText().trim();
        if (durationText.isEmpty()) {
            throw new IllegalArgumentException("Please enter a duration.");
        }
        return Integer.parseInt(durationText);
    }

    private void clearForm() {
        durationField.setText("");
        notesArea.setText("");
        selectedAttributes.clear();
        resetAttributeButtons();
    }

    private void resetAttributeButtons() {
        for (JButton button : attributeButtons.values()) {
            updateDeselectedButtonStyle(button);
        }
    }
}
