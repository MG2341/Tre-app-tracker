package core;

public enum SessionAttribute {

    FASCIA_RELEASE("fascia_release", "Fascia Release", 5),
    EMOTIONAL_RELEASE("emotional_release", "Emotional Release", 3),
    TEMPERATURE_CHANGE("temperature_change", "Temperature Change (Sweats/Chills)", 2),
    DEEP_RELAXATION("deep_relaxation", "Deep Relaxation", 4),
    MEMORIES_SURFACED("memories_surfaced", "Memories Surfaced", 1);

    private String id;          // Unique identifier for the attribute
    private String name;        // e.g., "Fascia Release" or "Relaxation"
    private int impactScore;    // e.g., +5 or -2
    

    private SessionAttribute(String id, String name, int impactScore) {
        this.id = id;
        this.name = name;
        this.impactScore = impactScore;
        
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getImpactScore() { return impactScore; }
    
}