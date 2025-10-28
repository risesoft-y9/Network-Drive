package net.risesoft.support;

public enum FileNodeType {
    FOLDER(0),
    PICTURE(1),
    DOCUMENT(2),
    VIDEO(3),
    AUDIO(4),
    PAKEAGE(5),
    OTHERS(6);

    private Integer value;

    FileNodeType(Integer value) {
        this.value = value;
    }

    public static FileNodeType getByValue(Integer value) {
        for (FileNodeType authorityEnum : FileNodeType.values()) {
            if (authorityEnum.getValue().equals(value)) {
                return authorityEnum;
            }
        }
        throw new IllegalArgumentException("value is invalid");
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
