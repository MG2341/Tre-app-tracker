package core;

import java.util.UUID;

public class SessionAttribute {
    private AttributeType attributeType;
    private int value;
    private UUID id;

    public SessionAttribute(AttributeType attributeType, int value) {
        this.attributeType = attributeType;
        this.value = value;
        this.id = UUID.randomUUID(); // Generate a unique ID for each attribute
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public int getValue() {
        return value;
    }

    public UUID getId() {
        return id;
    }

}

    