package com.litepan.enums;


public enum FileDelFlagEnums {
    DEL(0, "删除"),
    RECYCLE (1, "回收站"),
    NORMAL(2, "正常");

    private final Integer flag;
    private final String desc;

    FileDelFlagEnums(Integer flag, String desc) {
        this.flag = flag;
        this.desc = desc;
    }

    public Integer getFlag() {
        return flag;
    }

    public String getDesc() {
        return desc;
    }
}
