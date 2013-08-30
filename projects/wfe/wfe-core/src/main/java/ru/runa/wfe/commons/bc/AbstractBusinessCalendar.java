package ru.runa.wfe.commons.bc;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.CalendarInterval;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SafeIndefiniteLoop;

public abstract class AbstractBusinessCalendar implements BusinessCalendar {
    protected final Log log = LogFactory.getLog(getClass());

    protected abstract BusinessDay getBusinessDay(Calendar calendar);

    @Override
    public Date add(Date date, String durationString) {
        Duration duration = DurationParser.parse(durationString);
        if (duration.getAmount() == 0) {
            return date;
        }
        final Calendar calendar = CalendarUtil.dateToCalendar(date);
        if (!duration.isBusinessTime()) {
            calendar.add(duration.getCalendarField(), duration.getAmount());
            return calendar.getTime();
        }
        // business time expressed in minutes
        int minutesAmount = duration.getAmount();

        if (minutesAmount > 0) {
            new SafeBusinessDayIterator(minutesAmount) {

                @Override
                protected void doOp() {
                    BusinessDay businessDay = getBusinessDay(calendar);
                    List<CalendarInterval> workingIntervals = businessDay.getWorkingIntervals();
                    for (CalendarInterval interval : workingIntervals) {
                        if (CalendarUtil.compareTime(calendar, interval.getFrom()) <= 0) {
                            int intervalMinutesLength = interval.getLengthInMinutes();
                            if (minutesAmount <= intervalMinutesLength) {
                                CalendarUtil.setTimeFromCalendar(calendar, interval.getFrom());
                                calendar.add(Calendar.MINUTE, minutesAmount);
                                minutesAmount = 0;
                                break;
                            } else {
                                minutesAmount -= intervalMinutesLength;
                            }
                        } else if (CalendarUtil.compareTime(calendar, interval.getTo()) <= 0) {
                            int remainderMinutesLength = getRemainderMinutes(calendar, interval.getTo());
                            if (minutesAmount <= remainderMinutesLength) {
                                calendar.add(Calendar.MINUTE, minutesAmount);
                                minutesAmount = 0;
                                break;
                            } else {
                                minutesAmount -= remainderMinutesLength;
                            }
                        } else {
                            log.debug("Ignored " + interval + " for " + CalendarUtil.formatDateTime(calendar));
                        }
                    }
                    if (minutesAmount != 0) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        if (resetTime) {
                            CalendarUtil.setZeroTimeCalendar(calendar);
                            resetTime = false;
                        }
                    }
                }

            }.doLoop();
        } else {
            new SafeBusinessDayIterator(minutesAmount) {

                @Override
                protected void doOp() {
                    BusinessDay businessDay = getBusinessDay(calendar);
                    List<CalendarInterval> workingIntervals = businessDay.getWorkingIntervals();
                    Collections.reverse(workingIntervals);
                    for (CalendarInterval interval : workingIntervals) {
                        if (CalendarUtil.compareTime(calendar, interval.getTo()) > 0) {
                            int intervalMinutesLength = interval.getLengthInMinutes();
                            if (Math.abs(minutesAmount) <= intervalMinutesLength) {
                                CalendarUtil.setTimeFromCalendar(calendar, interval.getTo());
                                calendar.add(Calendar.MINUTE, minutesAmount);
                                minutesAmount = 0;
                                break;
                            } else {
                                minutesAmount += intervalMinutesLength;
                            }
                        } else if (CalendarUtil.compareTime(calendar, interval.getFrom()) > 0) {
                            int remainderMinutesLength = getRemainderMinutes(interval.getFrom(), calendar);
                            if (Math.abs(minutesAmount) <= remainderMinutesLength) {
                                calendar.add(Calendar.MINUTE, minutesAmount);
                                minutesAmount = 0;
                                break;
                            } else {
                                minutesAmount += remainderMinutesLength;
                            }
                        } else {
                            log.debug("Ignored " + interval + " for " + CalendarUtil.formatDateTime(calendar));
                        }
                    }
                    if (minutesAmount != 0) {
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                        if (resetTime) {
                            CalendarUtil.setLastSecondTimeCalendar(calendar);
                            resetTime = false;
                        }
                    }
                }

            }.doLoop();
        }
        return calendar.getTime();
    }

    private int getRemainderMinutes(Calendar time1, Calendar time2) {
        time1 = CalendarUtil.clone(time1);
        time2 = CalendarUtil.clone(time2);
        CalendarUtil.setDateFromCalendar(time1, time2);
        return new CalendarInterval(time1, time2).getLengthInMinutes();
    }

    @Override
    public boolean isHoliday(Calendar calendar) {
        BusinessDay businessDay = getBusinessDay(calendar);
        return businessDay.isHoliday();
    }

    private abstract static class SafeBusinessDayIterator extends SafeIndefiniteLoop {
        protected int minutesAmount;
        protected boolean resetTime = true;

        public SafeBusinessDayIterator(int minutesAmount) {
            super(36500);
            this.minutesAmount = minutesAmount;
        }

        @Override
        protected boolean continueLoop() {
            return minutesAmount != 0;
        }

    }
}
