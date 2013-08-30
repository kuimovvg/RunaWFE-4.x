package ru.runa.wfe.commons.bc;

import java.util.Date;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ru.runa.wfe.commons.CalendarUtil;

public class DefaultBusinessCalendarTest {
    private DefaultBusinessCalendar businessCalendar = new DefaultBusinessCalendar();

    @DataProvider
    public Object[][] getDurations() {
        return new Object[][] { { "-1 business hours" }, { "-1 business months" }, { "-1 business weeks" }, { "-1 business years" },
                { "-1 business minutes" }, { "1 business hours" }, { "1 business months" }, { "1 business weeks" }, { "1 business years" },
                { "1 business minutes" } };
    }

    @Test(dataProvider = "getDurations")
    public void testBusinessTime(String durationString) {
        Date date = new Date();
        Date date2 = businessCalendar.apply(date, durationString);
        System.out.println(durationString + ": " + CalendarUtil.formatDateTime(date) + " -> " + CalendarUtil.formatDateTime(date2));
    }

    @Test
    public void testBusinessTime2() {
        Date date = new Date();
        Date date2 = businessCalendar.apply(date, "1 business hours");
        System.out.println(CalendarUtil.formatDateTime(date) + " -> " + CalendarUtil.formatDateTime(date2));
    }
}
