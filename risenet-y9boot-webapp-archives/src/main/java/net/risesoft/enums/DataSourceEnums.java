package net.risesoft.enums;

public enum DataSourceEnums {
    SYSTEM_DEFAULT("systemDefault"), SYSTEM_ADD("systemAdd");

    private String value;

    DataSourceEnums(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
