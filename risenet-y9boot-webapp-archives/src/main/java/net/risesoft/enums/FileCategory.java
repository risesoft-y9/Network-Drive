package net.risesoft.enums;

public enum FileCategory {
    WENJIAN("wenjian"), ANJIAN("anjian");

    private String value;

    FileCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
