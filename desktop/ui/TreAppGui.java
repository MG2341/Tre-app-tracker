package desktop.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;

import core.SessionLog;
import core.SessionAttribute;
import core.application.SessionService;

public class TreAppGui extends JFrame {
    private SessionService sessionService;
    
    private JTextField durationField;
    private JTextField moodField;
    private JTextArea notesArea;
    private JButton saveSessionButton;

    public TreAppGui(SessionService sessionService) {
        this.sessionService = sessionService;
        
        // Frame setup
        setTitle("Tre App - Session Logger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Set size and center the window
        setSize(400, 350);
        setLocationRelativeTo(null);
        
        // Create main panel with GridLayout
        // Use GridLayout with 5 rows and 2 columns, with gaps
        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 50, 10));
        // Add padding around the panel
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
         
        // Duration label and field
        mainPanel.add(new JLabel("Duration (minutes):"));
        durationField = new JTextField();
        mainPanel.add(durationField);
        
        // Mood label and field
        mainPanel.add(new JLabel("Mood (1-10):"));
        moodField = new JTextField();
        mainPanel.add(moodField);
        
        // Notes label and area
        mainPanel.add(new JLabel("Notes:"));
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(notesArea);
        mainPanel.add(scrollPane);
        
        // Save button
        mainPanel.add(new JLabel()); // Empty space
        saveSessionButton = new JButton("Save Session");
        mainPanel.add(saveSessionButton);
        
        // Add action listener to Save button
        saveSessionButton.addActionListener(e -> handleSaveSession());
        
        // Add main panel to frame
        add(mainPanel);
        setVisible(true);
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
            
            // Parse mood
            String moodText = moodField.getText().trim();
            if (moodText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a mood value.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int mood = Integer.parseInt(moodText);
            
            // Get notes
            String notes = notesArea.getText();
            
            // Create SessionAttribute for Mood
            ArrayList<SessionAttribute> attributes = new ArrayList<>();
            attributes.add(new SessionAttribute("Mood", "Mood", mood, false));
            
            // Create SessionLog
            SessionLog log = new SessionLog(LocalDate.now(), duration, attributes, notes);
            
            // Execute use case
            sessionService.saveSession(log);
            
            // Show success message
            JOptionPane.showMessageDialog(this, "Session saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear fields
            durationField.setText("");
            moodField.setText("");
            notesArea.setText("");
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for duration and mood.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving session: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
