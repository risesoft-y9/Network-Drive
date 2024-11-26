package net.risesoft.enums;

public enum CategoryEnums {
    DOCUMENT("WS", "文书"), IMAGE("ZP", "图片"), VIDEO("LX", "视频"), AUDIO("LY", "音频");

    private String enName;
    private String cnName;

    CategoryEnums() {

    }

    CategoryEnums(String enName, String cnName) {
        this.enName = enName;
        this.cnName = cnName;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }
}
