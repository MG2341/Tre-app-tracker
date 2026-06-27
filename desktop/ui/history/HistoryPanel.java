package desktop.ui.history;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import core.application.SessionService;

public class HistoryPanel extends JPanel {
    private final SessionService sessionService;

    public HistoryPanel(SessionService sessionService) {
        this.sessionService = sessionService;

        add(createHistoryPanel());
    }

    
    private JPanel createHistoryPanel() {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel placeholderLabel = new JLabel("History Screen");
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Arial", Font.PLAIN, 24));

        historyPanel.add(placeholderLabel, BorderLayout.CENTER);
        return historyPanel;
    }
}
