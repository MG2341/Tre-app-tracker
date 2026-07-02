package desktop.infrastructure;

import core.SessionLog;
import core.application.LogRepository;
import core.SessionAttribute;
import core.AttributeType;


import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvLogRepositoryImpl implements LogRepository {
    private static final String CSV_HEADER = "Id,Date,DurationMinutes,StartTime,EndTime,Attributes,Notes";
    private static final String ATTRIBUTE_DELIMITER = ";";
    private static final String ATTRIBUTE_PAIR_DELIMITER = ":";
    private static final String TEXT_DATE_PREFIX = "'";
    private final String filePath;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter legacyDateFormatter = DateTimeFormatter.ofPattern("d/M/uuuu");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public CsvLogRepositoryImpl(String filePath) {
        this.filePath = filePath;
        initializeFile();
    }

    /**
     * Initialize the CSV file with headers if it doesn't exist
     */
    private void initializeFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(CSV_HEADER + "\n");
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize CSV file: " + filePath, e);
            }
        }
    }

    @Override
    public boolean deleteLogById(String id) {
        List<SessionLog> logs = getAllLogs();
        boolean removed = logs.removeIf(log -> log.getId().equals(id));
        if (removed) {
            writeAllLogs(logs);
        }
        return removed;
    }

    @Override
    public void saveLog(SessionLog log) {
        try (FileWriter writer = new FileWriter(filePath, true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

            String csvLine = logToCsvLine(log);
            bufferedWriter.write(csvLine);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save log to CSV: " + filePath, e);
        }
    }

    @Override
    public void updateLog(SessionLog log) {
        List<SessionLog> logs = getAllLogs();
        boolean updated = false;
        for (int i = 0; i < logs.size(); i++) {
            if (logs.get(i).getId().equals(log.getId())) {
                logs.set(i, log);
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new IllegalArgumentException("No log found with id: " + log.getId());
        }

        writeAllLogs(logs);
    }

    @Override
    public List<SessionLog> getLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<SessionLog> logs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                SessionLog log = csvLineToLog(line);
                if (log != null && !log.getDate().isBefore(startDate) && !log.getDate().isAfter(endDate)) {
                    logs.add(log);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read logs from CSV: " + filePath, e);
        }
        return logs;
    }

    @Override
    public List<SessionLog> getAllLogs() {
        List<SessionLog> logs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                SessionLog log = csvLineToLog(line);
                if (log != null) {
                    logs.add(log);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read logs from CSV: " + filePath, e);
        }
        return logs;
    }

    /**
     * Convert a SessionLog object to a CSV line
     */
    private String logToCsvLine(SessionLog log) {
        StringBuilder sb = new StringBuilder();

        sb.append(log.getId());
        sb.append(",");
        
        // Date
        sb.append(TEXT_DATE_PREFIX).append(log.getDate().format(dateFormatter));
        sb.append(",");
        
        // Duration minutes
        sb.append(log.getDurationMinutes());
        sb.append(",");
        
        // Start time
        if (log.getStartTime() != null) {
            sb.append(log.getStartTime().format(timeFormatter));
        }
        sb.append(",");
        
        // End time
        if (log.getEndTime() != null) {
            sb.append(log.getEndTime().format(timeFormatter));
        }
        sb.append(",");
        
        // Attributes
        sb.append(attributesToString(log.getAttributes()));
        sb.append(",");
        
        // Notes (escape quotes for CSV)
        String notes = log.getNotes();
        if (notes != null && !notes.isEmpty()) {
            sb.append("\"").append(notes.replace("\"", "\"\"")).append("\"");
        }
        
        return sb.toString();
    }

    private void writeAllLogs(List<SessionLog> logs) {
        try (FileWriter writer = new FileWriter(filePath);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.write(CSV_HEADER);
            bufferedWriter.newLine();
            for (SessionLog log : logs) {
                bufferedWriter.write(logToCsvLine(log));
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to update CSV file: " + filePath, e);
        }
    }

    /**
     * Convert a CSV line to a SessionLog object
     */
    private SessionLog csvLineToLog(String line) {
        try {
            String[] parts = parseCsvLine(line);
            if (parts.length < 6) {
                return null;
            }

            String id;
            int offset;
            if (parts.length >= 7) {
                id = parts[0].trim();
                offset = 1;
            } else {
                id = java.util.UUID.randomUUID().toString();
                offset = 0;
            }

            LocalDate date = parseDate(parts[0 + offset].trim());
            int durationMinutes = Integer.parseInt(parts[1 + offset].trim());
            LocalTime startTime = parseTime(parts[2 + offset].trim());
            LocalTime endTime = parseTime(parts[3 + offset].trim());
            ArrayList<SessionAttribute> attributes = stringToAttributes(parts[4 + offset].trim());
            String notes = parts[5 + offset].trim();
            if (notes.startsWith("\"") && notes.endsWith("\"")) {
                notes = notes.substring(1, notes.length() - 1).replace("\"\"", "\"");
            }

            return new SessionLog(date, startTime, endTime, durationMinutes, attributes, notes, id);
        } catch (Exception e) {
            System.err.println("Failed to parse CSV line: " + line + " - " + e.getMessage());
            return null;
        }
    }

    private LocalDate parseDate(String rawDate) {
        String dateValue = rawDate.trim();
        if (dateValue.startsWith(TEXT_DATE_PREFIX)) {
            dateValue = dateValue.substring(1);
        }

        try {
            return LocalDate.parse(dateValue, dateFormatter);
        } catch (DateTimeParseException ex) {
            return LocalDate.parse(dateValue, legacyDateFormatter);
        }
    }

    private LocalTime parseTime(String rawTime) {
        String timeValue = rawTime.trim();
        if (timeValue.isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(timeValue, timeFormatter);
        } catch (DateTimeParseException ex) {
            try {
                return LocalTime.parse(timeValue, DateTimeFormatter.ISO_LOCAL_TIME);
            } catch (DateTimeParseException ignored) {
                return parseLegacyMinuteSecondTime(timeValue);
            }
        }
    }

    private LocalTime parseLegacyMinuteSecondTime(String timeValue) {
        String[] parts = timeValue.split(":", 2);
        if (parts.length != 2) {
            throw new DateTimeParseException("Unrecognized time format", timeValue, 0);
        }

        int minutes = Integer.parseInt(parts[0]);
        double secondsValue = Double.parseDouble(parts[1]);
        int seconds = (int) secondsValue;
        int nanos = (int) Math.round((secondsValue - seconds) * 1_000_000_000d);
        if (nanos == 1_000_000_000) {
            seconds += 1;
            nanos = 0;
        }

        return LocalTime.of(0, minutes, seconds, nanos);
    }

    /**
     * Parse CSV line handling quoted fields
     */
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }

    /**
     * Convert SessionAttribute list to string format: "name1:score1;name2:score2"
     */
    private String attributesToString(ArrayList<SessionAttribute> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attributes.size(); i++) {
            SessionAttribute attr = attributes.get(i);
            sb.append(attr.getAttributeType().getName()).append(ATTRIBUTE_PAIR_DELIMITER).append(attr.getValue());
            if (i < attributes.size() - 1) {
                sb.append(ATTRIBUTE_DELIMITER);
            }
        }
        return sb.toString();
    }

    /**
     * Convert string format to SessionAttribute list
     */
    private ArrayList<SessionAttribute> stringToAttributes(String attributeString) {
        ArrayList<SessionAttribute> attributes = new ArrayList<>();
        if (attributeString == null || attributeString.isEmpty()) {
            return attributes;
        }
        
        String[] pairs = attributeString.split(ATTRIBUTE_DELIMITER);
        for (String pair : pairs) {
            String[] parts = pair.split(ATTRIBUTE_PAIR_DELIMITER, 2);
            if (parts.length == 2) {
                String name = parts[0].trim();
                int score = Integer.parseInt(parts[1].trim());
                attributes.add(new SessionAttribute(resolveAttributeType(name), score));
            }
        }
        return attributes;
    }

    private AttributeType resolveAttributeType(String rawName) {
        String normalizedName = rawName.trim();

        for (AttributeType attributeType : AttributeType.values()) {
            if (attributeType.getName().equalsIgnoreCase(normalizedName) || attributeType.name().equalsIgnoreCase(normalizedName)) {
                return attributeType;
            }
        }

        String enumStyleName = normalizedName.toUpperCase().replaceAll("[^A-Z0-9]+", "_");
        return AttributeType.valueOf(enumStyleName);
    }
}
