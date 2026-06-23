package desktop.test;

import java.util.ArrayList;
import java.util.List;

import core.SessionLog;
import core.application.LogRepository;
import core.application.SessionService;
import desktop.infrastructure.CsvLogRepositoryImpl;
import desktop.ui.TreAppGui;


/**
 * Mock implementation of LogRepository for testing
 */
class InMemoryLogRepository implements LogRepository {
    private List<SessionLog> logs = new ArrayList<>();

    @Override
    public void saveLog(SessionLog log) {
        logs.add(log);
    }

    @Override
    public boolean deleteLogById(String id) {
        return logs.removeIf(log -> log.getId().equals(id));
    }

    @Override
    public List<SessionLog> getAllLogs() {
        return new ArrayList<>(logs);
    }
}

/**
 * Test GUI launcher
 * Run this class with: java core.test.UseCaseTest
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
