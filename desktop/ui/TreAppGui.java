package desktop.ui;

import javax.swing.*;
import java.awt.*;
import core.application.SessionService;
import desktop.ui.session.SessionEntryPanel;
import desktop.ui.history.HistoryPanel;

/**
 * Main application window using CardLayout to manage multiple screens.
 * Implements Clean Architecture by separating GUI concerns into helper functions.
 */
public class TreAppGui extends JFrame {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final String ADD_SESSION_CARD = "AddSession";
    private static final String HISTORY_CARD = "History";

    private final SessionService sessionService;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final SessionEntryPanel sessionEntryPanel;
    private final HistoryPanel historyPanel;

    public TreAppGui(SessionService sessionService) {
        this.sessionService = sessionService;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);
        this.sessionEntryPanel = new SessionEntryPanel(sessionService);
        this.historyPanel = new HistoryPanel(sessionService);

        setupFrame();
        setupCardPanel();
        setupLayout();

        setVisible(true);
    }

    private void setupFrame() {
        setTitle("Tre App - Session Logger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
    }

    private void setupCardPanel() {
        cardPanel.add(sessionEntryPanel, ADD_SESSION_CARD);
        cardPanel.add(historyPanel, HISTORY_CARD);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createSidebarMenu(), BorderLayout.WEST);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createSidebarMenu() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sidebar.setPreferredSize(new Dimension(150, 0));

        JButton addSessionBtn = new JButton("Add Session");
        addSessionBtn.setMaximumSize(new Dimension(130, 40));
        addSessionBtn.addActionListener(e -> switchToAddSession());

        JButton viewHistoryBtn = new JButton("View History");
        viewHistoryBtn.setMaximumSize(new Dimension(130, 40));
        viewHistoryBtn.addActionListener(e -> switchToHistory());

        sidebar.add(addSessionBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(viewHistoryBtn);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private void switchToAddSession() {
        cardLayout.show(cardPanel, ADD_SESSION_CARD);
    }

    private void switchToHistory() {
        cardLayout.show(cardPanel, HISTORY_CARD);
    }
}
