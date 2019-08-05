package com.example.memokeeper.Utilities;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static int dateToComparableInt(Date input) {
        Calendar temp = Calendar.getInstance();
        temp.setTime(input);
        return temp.get(Calendar.YEAR) * 10000 + temp.get(Calendar.MONTH) * 100 + temp.get(Calendar.DAY_OF_MONTH);
    }

    public static String intToString(int input) {
        int year = input / 10000;
        int month = (input % 10000) / 100;
        int day = input % 100;
        return String.valueOf(day) + "/" + String.valueOf(month) + "/" + String.valueOf(year);
    }
}
