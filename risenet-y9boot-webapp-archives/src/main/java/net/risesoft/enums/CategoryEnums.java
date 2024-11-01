package net.risesoft.enums;

public enum CategoryEnums {
    DOCUMENT("document", "文书"), IMAGE("image", "图片"), VIDEO("video", "视频"), AUDIO("audio", "音频");

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
