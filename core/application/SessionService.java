package core.application;

import core.SessionLog;
import core.SessionAttribute;

public class SessionService {
    private LogRepository repository;

    public SessionService(LogRepository repository) {
        this.repository = repository;
    }

    public void saveSession(SessionLog log) {
        validateSession(log);
        repository.saveLog(log);
    }

    public void deleteSession(String id) {
        // Deletion rules here...
        repository.deleteLogById(id);
    }

    public void addAttributeToSession(SessionLog log, SessionAttribute attribute) {
        log.addAttribute(attribute);
        repository.deleteLogById(log.getId()); // Remove the old log
        repository.saveLog(log);
    }

    public void removeAttributeFromSession(SessionLog log, SessionAttribute attribute) {
        log.removeAttribute(attribute);
        repository.deleteLogById(log.getId()); // Remove the old log
        repository.saveLog(log);
    }

    private void validateSession(SessionLog log) {
        if (log.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0 minutes");
        }
    }
}