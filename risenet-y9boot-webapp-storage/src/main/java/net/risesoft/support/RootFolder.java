package net.risesoft.support;

public enum RootFolder {
    MY("my", "我的文件"), SHARED("shared", "共享空间"), PUBLIC("public", "公共文件"), DEPT("dept", "部门文件");

    private String enName;
    private String cnName;

    RootFolder() {

    }

    RootFolder(String enName, String cnName) {
        this.enName = enName;
        this.cnName = cnName;
    }

    public String getCnName() {
        return cnName;
    }

    public String getEnName() {
        return enName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }
}
