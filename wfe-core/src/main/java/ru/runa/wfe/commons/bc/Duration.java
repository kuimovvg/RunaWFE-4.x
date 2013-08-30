package ru.runa.wfe.commons.bc;

import org.testng.collections.Objects;

/**
 * Represents business duration.
 */
public class Duration {
    private final int calendarField;
    private final int amount;
    private final boolean businessTime;

    public Duration(int calendarField, int amount, boolean businessTime) {
        this.calendarField = calendarField;
        this.amount = amount;
        this.businessTime = businessTime;
    }

    public int getCalendarField() {
        return calendarField;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isBusinessTime() {
        return businessTime;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("amount", amount).add("field", calendarField).add("businessTime", businessTime).toString();
    }
}