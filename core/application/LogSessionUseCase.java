package core.application;

import core.SessionLog;

public class LogSessionUseCase {
    private LogRepository logRepository;

    public LogSessionUseCase(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void execute(SessionLog log) {
        if (log.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0 minutes");
        }

        logRepository.saveLog(log);
    }
}
