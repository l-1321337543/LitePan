package com.litepan.enums;


public enum DateTimePattenEnum {

    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"), YYYY_MM_DD("yyyy-MM-dd");

    private String patten;

    DateTimePattenEnum(String patten) {
        this.patten = patten;
    }

    public String getPatten() {
        return patten;
    }
}
