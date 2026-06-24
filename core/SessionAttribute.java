package core;

import java.util.UUID;

public class SessionAttribute {
    private AttributeType attributeType;
    private String value;
    private UUID id;

    public SessionAttribute(AttributeType attributeType, String value) {
        this.attributeType = attributeType;
        this.value = value;
        this.id = UUID.randomUUID(); // Generate a unique ID for each attribute
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public String getValue() {
        return value;
    }

    public UUID getId() {
        return id;
    }

}

    