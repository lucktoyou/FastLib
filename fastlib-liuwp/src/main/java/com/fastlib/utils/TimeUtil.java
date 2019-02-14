package com.fastlib.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    private static final int MASK_TIME_UNIT_BASE = 1;
    public static final int MASK_TIME_UNIT_YEAR = MASK_TIME_UNIT_BASE;
    public static final int MASK_TIME_UNIT_MONTH = MASK_TIME_UNIT_BASE << 1;
    public static final int MASK_TIME_UNIT_WEEK = MASK_TIME_UNIT_BASE << 2;
    public static final int MASK_TIME_UNIT_DAY = MASK_TIME_UNIT_BASE << 3;
    public static final int MASK_TIME_UNIT_HOUR = MASK_TIME_UNIT_BASE << 4;
    public static final int MASK_TIME_UNIT_MINUTE = MASK_TIME_UNIT_BASE << 5;

    private TimeUtil() {
    }

    /**
     * 返回N天，N月，N年前
     * @param timeMillis 格林尼治标准基准时间到指定时刻的毫秒数
     * @return 格式化后字符串
     */
    public static String formatTimeLag(long timeMillis) {
        return formatTimeLag(MASK_TIME_UNIT_YEAR
                | MASK_TIME_UNIT_MONTH
                | MASK_TIME_UNIT_WEEK
                | MASK_TIME_UNIT_DAY
                | MASK_TIME_UNIT_HOUR
                | MASK_TIME_UNIT_MINUTE, timeMillis);
    }

    /**
     * 返回N天，N月，N年前
     * @param unitMask   使用指定时间单位
     * @param timeMillis 格林尼治标准基准时间(即1970年1月1日，00:00 GMT)到指定时刻的毫秒数
     * @return 格式化后字符串
     */
    public static String formatTimeLag(int unitMask, long timeMillis) {
        final int hour = 1000 * 60 * 60;
        final int minute = 1000 * 60;
        long currTime = System.currentTimeMillis();
        Calendar currCalendar = Calendar.getInstance(); //当前时间日历
        Calendar customCalendar = Calendar.getInstance(); //自定义时间日历

        customCalendar.setTime(new Date(timeMillis));
        //年，月，周，天遍历
        int currYear = currCalendar.get(Calendar.YEAR);
        int customYear = customCalendar.get(Calendar.YEAR);
        if (((MASK_TIME_UNIT_YEAR & unitMask) != 0) && currYear != customYear){
            int diffYear=currYear-customYear;
            if(diffYear==1) return "去年";
            return (currYear - customYear) + "年前";
        }
        int currMonth = currCalendar.get(Calendar.MONTH);
        int customMonth = customCalendar.get(Calendar.MONTH);
        if (((MASK_TIME_UNIT_MONTH & unitMask) != 0) && currMonth != customMonth){
            int diffMonth=currMonth-customMonth;
            if(diffMonth==1) return "上个月前";
            return (currMonth - customMonth) + "个月前";
        }
        int currWeek = currCalendar.get(Calendar.WEEK_OF_MONTH);
        int customWeek = customCalendar.get(Calendar.WEEK_OF_MONTH);
        if ((MASK_TIME_UNIT_WEEK & unitMask) != 0 && currWeek != customWeek){
            int diffWeek=currWeek-customWeek;
            if(diffWeek==1) return "上个星期";
            return diffWeek + "个星期前";
        }
        int currDay = currCalendar.get(Calendar.DAY_OF_MONTH);
        int customDay = customCalendar.get(Calendar.DAY_OF_MONTH);
        if (((MASK_TIME_UNIT_DAY & unitMask) != 0) && currDay != customDay) {
            int diffDay=currDay-customDay;
            if(diffDay==1) return "昨天";
            return  diffDay+ "天前";
        }
        //小时，分，秒遍历
        if (((MASK_TIME_UNIT_HOUR & unitMask) != 0) && currTime - timeMillis > hour)
            return ((currTime - timeMillis) / hour) + "小时前";
        if (((MASK_TIME_UNIT_MINUTE & unitMask) != 0) && currTime - timeMillis > minute)
            return ((currTime - timeMillis) / minute) + "分钟前";
        return ((currTime - timeMillis) / 1000) + "秒前";
    }

    /**
     * 日期转字符串
     * @param date   日期
     * @param format 指定格式
     * @return 格式化后字符串
     */
    public static String dateToString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 标准yyyy-MM-dd HH:mm:ss字符串格式返回
     * @param date 日期
     * @return 格式化后字符串
     */
    public static String dateToString(Date date) {
        return dateToString(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 字符串转成指定格式的date类型
     * String dateStr = "2001.12.12-08:23:21";
     * String format = "yyyy.MM.dd-HH:mm:ss";
     * TimeUtil.StringToDate(dateStr, format).toString()<==>"Wed Dec 12 08:23:21 GMT+08:00 2001"
     * GMT+08:00表示东八区
     * @param dateStr 日期字符串
     * @param format 指定格式
     * @return 日期
     */
    public static Date StringToDate(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
