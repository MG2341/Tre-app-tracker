package core;

public class SessionAttribute {
    private String id;          // Unique identifier for the attribute
    private String name;        // e.g., "Fascia Release" or "Relaxation"
    private int impactScore;    // e.g., +5 or -2
    private boolean isCustom; // Helps you separate built-in vs user-created

    public SessionAttribute(String id, String name, int impactScore, boolean isCustom) {
        this.id = id;
        this.name = name;
        this.impactScore = impactScore;
        this.isCustom = isCustom;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getImpactScore() { return impactScore; }
    public boolean isCustom() { return isCustom; }
}