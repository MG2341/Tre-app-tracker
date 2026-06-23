package core.application;

import core.SessionLog;
import core.SessionAttribute;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvLogRepositoryImpl implements LogRepository {
    private static final String CSV_HEADER = "Date,DurationMinutes,StartTime,EndTime,Attributes,Notes";
    private static final String ATTRIBUTE_DELIMITER = ";";
    private static final String ATTRIBUTE_PAIR_DELIMITER = ":";
    private final String filePath;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

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
            try (FileWriter writer = new FileWriter(filePath);
                 BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                bufferedWriter.write(CSV_HEADER);
                bufferedWriter.newLine();
                for (SessionLog log : logs) {
                    String csvLine = logToCsvLine(log);
                    bufferedWriter.write(csvLine);
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException("Failed to update CSV after deletion: " + filePath, e);
            }
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
        
        // Date
        sb.append(log.getDate().format(dateFormatter));
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

    /**
     * Convert a CSV line to a SessionLog object
     */
    private SessionLog csvLineToLog(String line) {
        try {
            String[] parts = parseCsvLine(line);
            if (parts.length < 6) {
                return null;
            }
            
            LocalDate date = LocalDate.parse(parts[0].trim(), dateFormatter);
            int durationMinutes = Integer.parseInt(parts[1].trim());
            LocalTime startTime = parts[2].trim().isEmpty() ? null : LocalTime.parse(parts[2].trim(), timeFormatter);
            LocalTime endTime = parts[3].trim().isEmpty() ? null : LocalTime.parse(parts[3].trim(), timeFormatter);
            ArrayList<SessionAttribute> attributes = stringToAttributes(parts[4].trim());
            String notes = parts[5].trim();
            if (notes.startsWith("\"") && notes.endsWith("\"")) {
                notes = notes.substring(1, notes.length() - 1).replace("\"\"", "\"");
            }
            
            return new SessionLog(date, startTime, endTime, durationMinutes, attributes, notes);
        } catch (Exception e) {
            System.err.println("Failed to parse CSV line: " + line + " - " + e.getMessage());
            return null;
        }
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
            sb.append(attr.getName()).append(ATTRIBUTE_PAIR_DELIMITER).append(attr.getImpactScore());
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
            String[] parts = pair.split(ATTRIBUTE_PAIR_DELIMITER);
            if (parts.length == 2) {
                String name = parts[0].trim();
                int score = Integer.parseInt(parts[1].trim());
                attributes.add(new SessionAttribute(name, score));
            }
        }
        return attributes;
    }
}
