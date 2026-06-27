package core.application;

import java.util.List;
import core.SessionLog;

public interface LogRepository {
    void saveLog(SessionLog log);
    boolean deleteLogById(String id);
    List<SessionLog> getAllLogs();
    List<SessionLog> getLogsByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate);
}
