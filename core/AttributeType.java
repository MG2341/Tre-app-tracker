package core;

public enum AttributeType {

    FASCIA_RELEASE("Fascia Release"),
    EMOTIONAL_RELEASE("Emotional Release"),
    TEMPERATURE_CHANGE("Temperature Change (Sweats/Chills)"),
    DEEP_RELAXATION("Deep Relaxation"),
    MEMORIES_SURFACED("Memories Surfaced");
    
    private String name;        // e.g., "Fascia Release" or "Relaxation"
    
    private AttributeType(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    
    
}