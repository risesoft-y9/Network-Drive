package net.risesoft.support;

public enum FileListType {
    MY("my"),
    SHARED("shared"),
    PUBLIC("public"),
    DEPT("dept"),
    REPORT("report"),
    REPORTMANAGE("reportManage"),
    COLLECT("collect");

    private String value;

    FileListType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
