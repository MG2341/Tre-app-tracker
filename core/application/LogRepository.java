package core.application;

import java.util.List;
import core.SessionLog;

public interface LogRepository {
    void saveLog(SessionLog log);
    List<SessionLog> getAllLogs();
}
