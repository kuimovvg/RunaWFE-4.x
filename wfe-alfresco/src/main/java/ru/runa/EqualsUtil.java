package ru.runa;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import ru.runa.alfresco.ISynchronizable;
import ru.runa.wfe.commons.CalendarUtil;

/**
 * Helper for comparison.
 * 
 * @author dofs
 */
public class EqualsUtil {

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if ((o1 == null && o2 != null) || (o1 != null && o2 == null)) {
            return false;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            return Arrays.equals((Object[]) o1, (Object[]) o2);
        }
        return o1.equals(o2);
    }

    public static boolean equals(Calendar c1, Calendar c2, boolean compareWithTime) {
        if (compareWithTime) {
            return equals(c1, c2);
        }
        return CalendarUtil.compareOnlyDate(c1, c2) == 0;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static boolean equals(List o1, List o2) {
        if (o1.size() != o2.size()) {
            return false;
        }
        Collections.sort(o1);
        Collections.sort(o2);
        return Arrays.equals(o1.toArray(new Object[o1.size()]), o2.toArray(new Object[o1.size()]));
    }

    public static String getDiff(String ref, Object o1, Object o2) {
        if (o1 instanceof Calendar) {
            o1 = CalendarUtil.formatDateTime((Calendar) o1);
        }
        if (o2 instanceof Calendar) {
            o2 = CalendarUtil.formatDateTime((Calendar) o2);
        }
        return ref + ": " + o1 + "/" + o2;
    }

    public static <T extends Object> boolean hasBusinessDataDiff(ISynchronizable<T> o1, T o2) {
        return o1.getBusinessDataDiff(o2) != null;
    }

}
