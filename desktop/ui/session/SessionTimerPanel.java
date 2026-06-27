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
    private int configuredDurationMinutes;

    public SessionTimerPanel(IntSupplier durationSupplier, Runnable onTimerStarted, Runnable onTimerStopped) {
        this.durationSupplier = durationSupplier;
        this.onTimerStarted = onTimerStarted;
        this.onTimerStopped = onTimerStopped;

        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("Timer"));

        timerStatusLabel = new JLabel("Timer not started");
        startButton = new JButton("Start");
        resetButton = new JButton("Reset");

        startButton.addActionListener(e -> startTimer());
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

    public int getConfiguredDurationMinutes() {
        return configuredDurationMinutes;
    }

    public void resetTimer() {
        stopSwingTimer();
        timerStartDateTime = null;
        configuredDurationMinutes = 0;
        timerStatusLabel.setText("Timer not started");
        startButton.setEnabled(true);
        resetButton.setEnabled(false);
        onTimerStopped.run();
    }

    private void startTimer() {
        if (isRunning()) {
            return;
        }

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
        onTimerStarted.run();

        startButton.setEnabled(false);
        resetButton.setEnabled(true);

        swingTimer = new Timer(1000, e -> updateTimerStatus());
        swingTimer.start();
        updateTimerStatus();
    }

    private void updateTimerStatus() {
        if (timerStartDateTime == null) {
            timerStatusLabel.setText("Timer not started");
            return;
        }

        long totalSeconds = configuredDurationMinutes * 60L;
        long elapsedSeconds = Duration.between(timerStartDateTime, LocalDateTime.now()).getSeconds();
        long remainingSeconds = Math.max(0, totalSeconds - elapsedSeconds);

        if (remainingSeconds == 0) {
            timerStatusLabel.setText("Timer complete");
            stopSwingTimer();
            startButton.setEnabled(true);
            resetButton.setEnabled(true);
            onTimerStopped.run();
            return;
        }

        timerStatusLabel.setText("Remaining: " + formatSeconds(remainingSeconds));
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