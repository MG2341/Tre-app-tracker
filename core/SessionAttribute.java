package core;

public class SessionAttribute {
    private String name;        // e.g., "Fascia Release" or "Relaxation"
    private int impactScore;    // e.g., +5 or -2

    public SessionAttribute(String name, int impactScore) {
        this.name = name;
        this.impactScore = impactScore;
    }

    public String getName() { return name; }
    public int getImpactScore() { return impactScore; }
}