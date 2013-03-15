package ru.runa.wfe.commons;

import java.util.Calendar;

public abstract class CalendarIterator {
    private final Calendar c;
    private final Calendar to;

    public CalendarIterator(Calendar from, Calendar to) {
        this.c = CalendarUtil.getZeroTimeCalendar(from);
        this.to = to;
    }

    public CalendarIterator(CalendarInterval interval) {
        this.c = CalendarUtil.getZeroTimeCalendar(interval.getFrom());
        this.to = interval.getTo();
    }

    protected abstract void iteration(Calendar current);

    public final void iterate() {
        while (!c.after(to)) {
            iteration(c);
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

}
