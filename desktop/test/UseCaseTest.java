package desktop.test;

import java.util.ArrayList;
import java.util.List;

import core.SessionLog;
import core.application.LogRepository;
import core.application.SessionService;
import desktop.infrastructure.CsvLogRepositoryImpl;
import desktop.ui.TreAppGui;

/**
 * Test GUI launcher
 * Run this class with: java desktop.test.UseCaseTest
 */
public class UseCaseTest {
    public static void main(String[] args) {
        String path = "C:\\Users\\mgold\\OneDrive\\Documents\\code_Projects\\Tre-app-tracker";

        // Create in-memory repository
        LogRepository repository = new CsvLogRepositoryImpl(path + "\\logs.csv");
        
        // Create session service
        SessionService sessionService = new SessionService(repository);
        
        // Launch GUI on Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            new TreAppGui(sessionService);
        });
    }
}
