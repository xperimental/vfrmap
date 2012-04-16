package net.sourcewalker.vfrmap.data;

public enum SpeedUnit {
    KPH("kph"), KNOTS("kn"), MS("ms");

    private String value;

    public String getValue() {
        return value;
    }

    private SpeedUnit(String value) {
        this.value = value;
    }

    public static SpeedUnit parseValue(String value) {
        for (SpeedUnit u : values()) {
            if (u.value.equals(value)) {
                return u;
            }
        }
        return KPH;
    }
}
