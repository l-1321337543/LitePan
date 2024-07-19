package com.litepan.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtil {

    private static final Object lockObj = new Object();
    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    private static SimpleDateFormat getSdf(final String patten){
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(patten);
        if (tl == null) {
            synchronized (lockObj){
                if (tl == null) {
                    tl = new ThreadLocal<SimpleDateFormat>(){
                        @Override
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(patten);
                        }
                    };
                    sdfMap.put(patten,tl);
                }
            }

        }
        return tl.get();
    }

    public static String format(Date date,String patten){
        return getSdf(patten).format(date);
    }

    public static Date parse(String dateStr,String patten){
        try {
            return getSdf(patten).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
