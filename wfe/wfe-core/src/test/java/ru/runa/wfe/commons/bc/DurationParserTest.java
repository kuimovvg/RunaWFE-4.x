package ru.runa.wfe.commons.bc;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ru.runa.wfe.commons.CalendarUtil;

public class DurationParserTest extends Assert {

    @BeforeClass
    public void setUp() {
        // code that will be invoked when this test is instantiated
    }

    @DataProvider
    public Object[][] getDurations() {
        return new Object[][] { { "1 business hours" }, { "1 hours" } };
    }

    @Test(dataProvider = "getDurations")
    public void testParseLocale(String durationString) {
        Duration duration = DurationParser.parse(durationString);
        System.out.println(durationString + "=" + duration);
        // assertEquals(actual, expected);
        Date zeroDate = CalendarUtil.getZero().getTime();
        System.out.println(Integer.MAX_VALUE);
        System.out.println(zeroDate.getTime());
        long delta = 60000L * Integer.MAX_VALUE;
        System.out.println(delta);
        Date appDate = new Date(zeroDate.getTime() + delta);
        System.out.println(appDate.getTime());
        System.out.println(zeroDate + " -> " + appDate);
    }
}
