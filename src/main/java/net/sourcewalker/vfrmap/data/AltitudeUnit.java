package net.sourcewalker.vfrmap.data;

public enum AltitudeUnit {
    FEET("ft"), METER("m");

    private String value;

    public String getValue() {
        return value;
    }

    private AltitudeUnit(String value) {
        this.value = value;
    }

    public static AltitudeUnit parseValue(String value) {
        for (AltitudeUnit u : values()) {
            if (u.value.equals(value)) {
                return u;
            }
        }
        return FEET;
    }
}
