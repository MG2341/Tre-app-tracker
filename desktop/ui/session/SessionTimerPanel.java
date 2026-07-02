package desktop.ui.session;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.IntSupplier;

public class SessionTimerPanel extends JPanel {
    private final IntSupplier durationSupplier;
    private final Runnable onTimerStarted;
    private final Runnable onTimerStopped;
    private final JLabel timerStatusLabel;
    private final JButton startButton;
    private final JButton resetButton;
    private Timer swingTimer;
    private LocalDateTime timerStartDateTime;
    private LocalDateTime activeSegmentStartDateTime;
    private LocalDateTime timerEndDateTime;
    private int configuredDurationMinutes;
    private long accumulatedActiveSeconds;
    private boolean timerComplete;

    public SessionTimerPanel(IntSupplier durationSupplier, Runnable onTimerStarted, Runnable onTimerStopped) {
        this.durationSupplier = durationSupplier;
        this.onTimerStarted = onTimerStarted;
        this.onTimerStopped = onTimerStopped;

        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("Timer"));

        timerStatusLabel = new JLabel("Timer not started");
        startButton = new JButton("Start");
        resetButton = new JButton("Reset");

        startButton.addActionListener(e -> toggleTimer());
        resetButton.addActionListener(e -> resetTimer());
        resetButton.setEnabled(false);

        add(timerStatusLabel);
        add(startButton);
        add(resetButton);
    }

    public boolean isRunning() {
        return swingTimer != null && swingTimer.isRunning();
    }

    public LocalTime getStartTime() {
        return timerStartDateTime == null ? null : timerStartDateTime.toLocalTime();
    }

    public LocalTime getEndTime() {
        if (timerEndDateTime != null) {
            return timerEndDateTime.toLocalTime();
        }

        if (activeSegmentStartDateTime != null) {
            return LocalDateTime.now().toLocalTime();
        }

        return null;
    }

    public int getConfiguredDurationMinutes() {
        return configuredDurationMinutes;
    }

    public int getElapsedDurationMinutes() {
        long totalSeconds = getTotalActiveSeconds();
        if (totalSeconds <= 0) {
            return 0;
        }

        return (int) ((totalSeconds + 59L) / 60L);
    }

    public void resetTimer() {
        stopSwingTimer();
        clearTimerState();
        configuredDurationMinutes = 0;
        timerStatusLabel.setText("Timer not started");
        startButton.setEnabled(true);
        resetButton.setEnabled(false);
        onTimerStopped.run();
    }

    private void clearTimerState() {
        timerStartDateTime = null;
        activeSegmentStartDateTime = null;
        timerEndDateTime = null;
        accumulatedActiveSeconds = 0L;
        timerComplete = false;
        startButton.setText("Start");
    }

    private void toggleTimer() {
        if (isRunning()) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        if (isRunning()) {
            return;
        }

        if (timerComplete) {
            clearTimerState();
        }

        if (timerStartDateTime == null) {
            int durationMinutes;
            try {
                durationMinutes = durationSupplier.getAsInt();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (durationMinutes <= 0) {
                JOptionPane.showMessageDialog(this, "Duration must be greater than 0 minutes.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            configuredDurationMinutes = durationMinutes;
            timerStartDateTime = LocalDateTime.now();
        }

        activeSegmentStartDateTime = LocalDateTime.now();
        timerEndDateTime = null;
        timerComplete = false;
        onTimerStarted.run();

        startButton.setText("Stop");
        startButton.setEnabled(true);
        resetButton.setEnabled(true);

        swingTimer = new Timer(1000, e -> updateTimerStatus());
        swingTimer.start();
        updateTimerStatus();
    }

    private void stopTimer() {
        if (!isRunning()) {
            return;
        }

        accumulateCurrentSegment();
        activeSegmentStartDateTime = null;
        timerEndDateTime = LocalDateTime.now();
        stopSwingTimer();
        startButton.setText("Continue");
        onTimerStopped.run();
        updateTimerStatus();
    }

    private void updateTimerStatus() {
        if (timerStartDateTime == null) {
            timerStatusLabel.setText("Timer not started");
            return;
        }

        long totalSeconds = configuredDurationMinutes * 60L;
        long elapsedSeconds = getTotalActiveSeconds();
        long remainingSeconds = Math.max(0, totalSeconds - elapsedSeconds);

        if (remainingSeconds == 0) {
            timerStatusLabel.setText("Timer complete");
            timerComplete = true;
            accumulatedActiveSeconds = totalSeconds;
            activeSegmentStartDateTime = null;
            timerEndDateTime = LocalDateTime.now();
            stopSwingTimer();
            startButton.setText("Start");
            startButton.setEnabled(true);
            resetButton.setEnabled(true);
            onTimerStopped.run();
            return;
        }

        if (isRunning()) {
            timerStatusLabel.setText("Remaining: " + formatSeconds(remainingSeconds));
        } else {
            timerStatusLabel.setText("Paused: " + formatSeconds(remainingSeconds));
        }
    }

    private void accumulateCurrentSegment() {
        if (activeSegmentStartDateTime == null) {
            return;
        }

        accumulatedActiveSeconds += Duration.between(activeSegmentStartDateTime, LocalDateTime.now()).getSeconds();
    }

    private long getTotalActiveSeconds() {
        long totalSeconds = accumulatedActiveSeconds;

        if (activeSegmentStartDateTime != null) {
            totalSeconds += Duration.between(activeSegmentStartDateTime, LocalDateTime.now()).getSeconds();
        }

        if (configuredDurationMinutes > 0) {
            totalSeconds = Math.min(totalSeconds, configuredDurationMinutes * 60L);
        }

        return Math.max(0L, totalSeconds);
    }

    private void stopSwingTimer() {
        if (swingTimer != null) {
            swingTimer.stop();
            swingTimer = null;
        }
    }

    private String formatSeconds(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}