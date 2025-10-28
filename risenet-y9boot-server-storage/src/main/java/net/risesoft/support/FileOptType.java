package net.risesoft.support;

public enum FileOptType {
    SHARE("share"),
    PUBLIC("public");

    private String value;

    FileOptType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
