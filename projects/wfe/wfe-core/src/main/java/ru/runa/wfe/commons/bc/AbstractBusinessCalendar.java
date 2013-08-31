package ru.runa.wfe.commons.bc;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarInterval;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SafeIndefiniteLoop;

public abstract class AbstractBusinessCalendar implements BusinessCalendar {
    protected final Log log = LogFactory.getLog(getClass());

    protected abstract BusinessDay getBusinessDay(Calendar calendar);

    @Override
    public boolean isHoliday(Calendar calendar) {
        BusinessDay businessDay = getBusinessDay(calendar);
        return businessDay.isHoliday();
    }

    @Override
    public Date apply(Date date, String durationString) {
        BusinessDuration businessDuration = BusinessDurationParser.parse(durationString);
        if (businessDuration.getAmount() == 0) {
            return date;
        }
        Calendar calendar = CalendarUtil.dateToCalendar(date);
        if (!businessDuration.isBusinessTime()) {
            calendar.add(businessDuration.getCalendarField(), businessDuration.getAmount());
            return calendar.getTime();
        }
        if (businessDuration.getCalendarField() == Calendar.MINUTE) {
            applyUsingMinutes(calendar, businessDuration.getAmount());
        } else if (businessDuration.getCalendarField() == Calendar.DAY_OF_YEAR) {
            applyUsingDays(calendar, businessDuration.getAmount());
        } else {
            throw new InternalApplicationException("Business duration expressed in unexpected unit: " + businessDuration);
        }
        return calendar.getTime();
    }

    private void applyUsingMinutes(final Calendar calendar, int minutesAmount) {
        if (minutesAmount > 0) {
            new SafeBusinessDayIterator(minutesAmount) {

                @Override
                protected void doOp() {
                    BusinessDay businessDay = getBusinessDay(calendar);
                    for (CalendarInterval interval : businessDay.getWorkingIntervals()) {
                        if (CalendarUtil.compareTime(calendar, interval.getFrom()) <= 0) {
                            int intervalMinutesLength = interval.getLengthInMinutes();
                            if (amount <= intervalMinutesLength) {
                                CalendarUtil.setTimeFromCalendar(calendar, interval.getFrom());
                                calendar.add(Calendar.MINUTE, amount);
                                amount = 0;
                                break;
                            } else {
                                amount -= intervalMinutesLength;
                            }
                        } else if (CalendarUtil.compareTime(calendar, interval.getTo()) <= 0) {
                            int remainderMinutesLength = getRemainderMinutes(calendar, interval.getTo());
                            if (amount <= remainderMinutesLength) {
                                calendar.add(Calendar.MINUTE, amount);
                                amount = 0;
                                break;
                            } else {
                                amount -= remainderMinutesLength;
                            }
                        } else {
                            log.debug("Ignored " + interval + " for " + CalendarUtil.formatDateTime(calendar));
                        }
                    }
                    if (amount != 0) {
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
                            if (Math.abs(amount) <= intervalMinutesLength) {
                                CalendarUtil.setTimeFromCalendar(calendar, interval.getTo());
                                calendar.add(Calendar.MINUTE, amount);
                                amount = 0;
                                break;
                            } else {
                                amount += intervalMinutesLength;
                            }
                        } else if (CalendarUtil.compareTime(calendar, interval.getFrom()) > 0) {
                            int remainderMinutesLength = getRemainderMinutes(interval.getFrom(), calendar);
                            if (Math.abs(amount) <= remainderMinutesLength) {
                                calendar.add(Calendar.MINUTE, amount);
                                amount = 0;
                                break;
                            } else {
                                amount += remainderMinutesLength;
                            }
                        } else {
                            log.debug("Ignored " + interval + " for " + CalendarUtil.formatDateTime(calendar));
                        }
                    }
                    if (amount != 0) {
                        calendar.add(Calendar.DAY_OF_YEAR, -1);
                        if (resetTime) {
                            CalendarUtil.setLastSecondTimeCalendar(calendar);
                            resetTime = false;
                        }
                    }
                }

            }.doLoop();
        }
    }

    private int getRemainderMinutes(Calendar time1, Calendar time2) {
        time1 = CalendarUtil.clone(time1);
        time2 = CalendarUtil.clone(time2);
        CalendarUtil.setDateFromCalendar(time1, time2);
        return new CalendarInterval(time1, time2).getLengthInMinutes();
    }

    private void applyUsingDays(final Calendar calendar, int daysAmount) {
        if (daysAmount > 0) {
            new SafeBusinessDayIterator(daysAmount) {

                @Override
                protected void doOp() {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    BusinessDay businessDay = getBusinessDay(calendar);
                    if (!businessDay.isHoliday()) {
                        amount--;
                        if (resetTime) {
                            boolean adjustTimeInNextDay = true;
                            Calendar closestCalendar = null;
                            for (CalendarInterval interval : businessDay.getWorkingIntervals()) {
                                if (CalendarUtil.compareTime(interval.getFrom(), calendar) <= 0
                                        && CalendarUtil.compareTime(calendar, interval.getTo()) <= 0) {
                                    closestCalendar = null;
                                    adjustTimeInNextDay = false;
                                    break;
                                }
                                if (closestCalendar == null && CalendarUtil.compareTime(calendar, interval.getFrom()) < 0) {
                                    closestCalendar = interval.getFrom();
                                }
                            }
                            if (closestCalendar != null) {
                                CalendarUtil.setTimeFromCalendar(calendar, closestCalendar);
                                adjustTimeInNextDay = false;
                            }
                            if (adjustTimeInNextDay) {
                                amount++;
                                CalendarUtil.setZeroTimeCalendar(calendar);
                            } else {
                                resetTime = false;
                            }
                        }
                    }
                }

            }.doLoop();
        } else {
            new SafeBusinessDayIterator(daysAmount) {

                @Override
                protected void doOp() {
                    calendar.add(Calendar.DAY_OF_YEAR, -1);
                    BusinessDay businessDay = getBusinessDay(calendar);
                    if (!businessDay.isHoliday()) {
                        amount++;
                        if (resetTime) {
                            boolean adjustTimeInPreviousDay = true;
                            Calendar closestCalendar = null;
                            List<CalendarInterval> workingIntervals = businessDay.getWorkingIntervals();
                            Collections.reverse(workingIntervals);
                            for (CalendarInterval interval : workingIntervals) {
                                if (CalendarUtil.compareTime(interval.getFrom(), calendar) <= 0
                                        && CalendarUtil.compareTime(calendar, interval.getTo()) <= 0) {
                                    closestCalendar = null;
                                    adjustTimeInPreviousDay = false;
                                    break;
                                }
                                if (closestCalendar == null && CalendarUtil.compareTime(interval.getTo(), calendar) < 0) {
                                    closestCalendar = interval.getTo();
                                }
                            }
                            if (closestCalendar != null) {
                                CalendarUtil.setTimeFromCalendar(calendar, closestCalendar);
                                adjustTimeInPreviousDay = false;
                            }
                            if (adjustTimeInPreviousDay) {
                                amount--;
                                CalendarUtil.setLastSecondTimeCalendar(calendar);
                            } else {
                                resetTime = false;
                            }
                        }
                    }
                }

            }.doLoop();
        }
    }

    private abstract static class SafeBusinessDayIterator extends SafeIndefiniteLoop {
        protected int amount;
        protected boolean resetTime = true;

        public SafeBusinessDayIterator(int amount) {
            super(36500);
            this.amount = amount;
        }

        @Override
        protected boolean continueLoop() {
            return amount != 0;
        }

    }
}
