package core;


public class SessionAttribute {
    private AttributeType attributeType;
    private int value;

    public SessionAttribute(AttributeType attributeType, int value) {
        this.attributeType = attributeType;
        this.value = value;
    }

    public SessionAttribute(AttributeType attributeType) {
        this(attributeType, 0); // Default value is 0
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}

    