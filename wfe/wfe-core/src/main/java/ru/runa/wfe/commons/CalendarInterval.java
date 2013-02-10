package ru.runa.wfe.commons;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.common.base.Objects;

/**
 * Contains 2 calendars.
 * 
 * @author dofs
 */
public class CalendarInterval implements Comparable<CalendarInterval> {
    private Calendar from;
    private Calendar to;

    public CalendarInterval(Calendar onDate) {
        from = CalendarUtil.getZeroTimeCalendar(onDate);
        to = CalendarUtil.getLastSecondTimeCalendar(onDate);
    }

    public CalendarInterval(Date from, Date to) {
        this(CalendarUtil.dateToCalendar(from), CalendarUtil.dateToCalendar(to), true);
    }

    public CalendarInterval(Calendar from, Calendar to) {
        this(from, to, false);
    }

    public CalendarInterval(Calendar from, Calendar to, boolean expandTimeInBounds) {
        this.from = CalendarUtil.clone(from);
        this.to = CalendarUtil.clone(to);
        if (expandTimeInBounds) {
            CalendarUtil.setZeroTimeCalendar(this.from);
            CalendarUtil.setLastSecondTimeCalendar(this.to);
        }
    }

    public Calendar getFrom() {
        return from;
    }

    public Calendar getTo() {
        return to;
    }

    public void setFrom(Calendar from) {
        this.from = from;
    }

    public void setTo(Calendar to) {
        this.to = to;
    }

    // right order, length > 0
    public boolean isValid() {
        return to.after(from);
    }

    public double getDaysBetween() {
        return CalendarUtil.daysBetween(from, to);
    }

    // by default it is inclusive
    public boolean contains(Calendar calendar) {
        return !calendar.before(from) && !calendar.after(to);
    }

    public boolean contains(Calendar calendar, boolean inclusive) {
        if (inclusive) {
            return !calendar.before(from) && !calendar.after(to);
        } else {
            return calendar.after(from) && calendar.before(to);
        }
    }

    public boolean contains(CalendarInterval interval, boolean inclusive) {
        if (inclusive) {
            return !interval.getFrom().before(from) && !interval.getTo().after(to);
        } else {
            return interval.getFrom().after(from) && interval.getTo().before(to);
        }
    }

    public boolean intersects(CalendarInterval interval) {
        return interval.getFrom().before(to) && interval.getTo().after(from);
    }

    // returns true if there's an intersection or a gap between intervals is
    // smaller than the gapInMillis
    public boolean intersectsWithGapScale(CalendarInterval interval, int gapInMillis) {
        CalendarInterval gap = this.getGapBetweenNotIntersecting(interval);
        if (gap == null) {
            return true;
        }
        if (gap.getLengthInMillis() <= gapInMillis) {
            return true;
        }
        return false;
    }

    // if the gap between the intervals is less or equal to gapInMillis they
    // merge.
    public static List<CalendarInterval> mergeIntersectingWithGapScaleIntervalsNotOrdered(List<CalendarInterval> intervals, int gapInMillis) {
        if (intervals == null || intervals.size() < 2) {
            return intervals;
        }
        List<CalendarInterval> result = new ArrayList<CalendarInterval>();
        Collections.sort(intervals);
        CalendarInterval current = intervals.get(0);
        for (int i = 1; i < intervals.size(); i++) {
            if (current.intersectsWithGapScale(intervals.get(i), gapInMillis)) {
                Calendar from = current.getFrom().after(intervals.get(i).getFrom()) ? intervals.get(i).getFrom() : current.getFrom();
                Calendar to = current.getTo().before(intervals.get(i).getTo()) ? intervals.get(i).getTo() : current.getTo();
                current = new CalendarInterval(from, to);
            } else {
                result.add(current);
                current = intervals.get(i);
            }
        }
        // add last element
        result.add(current);
        return result;
    }

    public List<CalendarInterval> cropToFitInInterval(List<CalendarInterval> list) {
        List<CalendarInterval> result = new ArrayList<CalendarInterval>();
        for (CalendarInterval interval : list) {
            if ((interval.getFrom().after(to)) || (interval.getTo().before(from))) {
                continue;
            }
            if (interval.getFrom().before(from)) {
                interval.setFrom(from);
            }
            if (interval.getTo().after(to)) {
                interval.setTo(to);
            }
            result.add(interval);
        }
        return result;
    }

    public CalendarInterval intersect(CalendarInterval interval) {
        if (from.before(interval.getFrom())) {
            from.setTimeInMillis(interval.getFrom().getTimeInMillis());
        }
        if (to.after(interval.getTo())) {
            to.setTimeInMillis(interval.getTo().getTimeInMillis());
        }
        return this;
    }

    public CalendarInterval getIntersection(CalendarInterval interval) {
        CalendarInterval target = CalendarUtil.clone(this);
        target.intersect(interval);
        return target;
    }

    public CalendarInterval merge(CalendarInterval interval) {
        if (from.after(interval.getFrom())) {
            from.setTimeInMillis(interval.getFrom().getTimeInMillis());
        }
        if (to.before(interval.getTo())) {
            to.setTimeInMillis(interval.getTo().getTimeInMillis());
        }
        return this;
    }

    public void subtractIntersecting(CalendarInterval interval) {
        if (from.before(interval.getFrom())) {
            to.setTimeInMillis(interval.getFrom().getTimeInMillis());
            to.add(Calendar.DAY_OF_YEAR, -1);
            CalendarUtil.setLastSecondTimeCalendar(to);
        }
        if (to.after(interval.getTo())) {
            from.setTimeInMillis(interval.getTo().getTimeInMillis());
            from.add(Calendar.DAY_OF_YEAR, 1);
            CalendarUtil.setZeroTimeCalendar(from);
        }
    }

    // subtracts interval from this.
    public List<CalendarInterval> subtract(CalendarInterval interval) {
        List<CalendarInterval> result = new ArrayList<CalendarInterval>();
        if (!(this.intersects(interval))) {
            result.add(this);
            return result;
        }
        if (from.before(interval.getFrom()) && (to.after(interval.getTo()))) {
            result.add(new CalendarInterval(from, interval.getFrom()));
            result.add(new CalendarInterval(interval.getTo(), to));
            return result;
        }
        if (from.after(interval.getFrom()) && (to.after(interval.getTo()))) {
            result.add(new CalendarInterval(interval.getTo(), to));
            return result;
        }
        if (from.before(interval.getFrom()) && (to.before(interval.getTo()))) {
            result.add(new CalendarInterval(from, interval.getFrom()));
            return result;
        }
        return result;
    }

    public CalendarInterval getGapBetweenNotIntersecting(CalendarInterval interval) {
        if (this.intersects(interval)) {
            return null;
        }
        Calendar gapFrom = Calendar.getInstance();
        Calendar gapTo = Calendar.getInstance();
        if (from.before(interval.getFrom())) {
            gapFrom.setTime(to.getTime());
            gapTo.setTime(interval.getFrom().getTime());
        } else {
            gapFrom.setTime(interval.getTo().getTime());
            gapTo.setTime(from.getTime());
        }
        return new CalendarInterval(gapFrom, gapTo);
    }

    public long getLengthInMillis() {
        if (from != null && to != null) {
            return to.getTimeInMillis() - from.getTimeInMillis();
        }
        return 0;
    }

    public int getLengthInMinutes() {
        long millis = getLengthInMillis();
        return CalendarUtil.countMinutesFromMillis(millis);
    }

    @Override
    public String toString() {
        return CalendarUtil.format(from, CalendarUtil.HOURS_MINUTES_SECONDS_FORMAT) + "-"
                + CalendarUtil.format(to, CalendarUtil.HOURS_MINUTES_SECONDS_FORMAT);
    }

    public String toDateRangeString() {
        return CalendarUtil.formatDate(from) + "-" + CalendarUtil.formatDate(to);
    }

    public boolean hasEqualDates(CalendarInterval o) {
        if (from != null && o.getFrom() != null) {
            if (!CalendarUtil.areCalendarsEqualIgnoringTime(from, o.from)) {
                return false;
            }
        } else if (from == null && o.getFrom() != null) {
            return false;
        } else if (o.getFrom() == null && from != null) {
            return false;
        }

        if (to != null && o.getTo() != null) {
            if (!CalendarUtil.areCalendarsEqualIgnoringTime(to, o.to)) {
                return false;
            }
        } else if (to == null && o.getTo() != null) {
            return false;
        } else if (o.getTo() == null && to != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(from, to);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CalendarInterval)) {
            return false;
        }
        CalendarInterval o = (CalendarInterval) obj;
        return Objects.equal(from, o.from) && Objects.equal(to, o.to);
    }

    @Override
    public int compareTo(CalendarInterval interval) {
        int res = from.compareTo(interval.getFrom());
        if (res == 0) {
            res = to.compareTo(interval.getTo());
        }
        return res;
    }

}
