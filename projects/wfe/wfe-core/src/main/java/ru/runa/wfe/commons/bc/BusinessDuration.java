package ru.runa.wfe.commons.bc;

import com.google.common.base.Objects;

/**
 * Represents business duration.
 */
public class BusinessDuration {
    private final int calendarField;
    private final int amount;
    private final boolean businessTime;

    public BusinessDuration(int calendarField, int amount, boolean businessTime) {
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
    public boolean equals(Object obj) {
        if (!(obj instanceof BusinessDuration)) {
            return false;
        }
        BusinessDuration d = (BusinessDuration) obj;
        return calendarField == d.calendarField && amount == d.amount && businessTime == d.businessTime;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(calendarField, amount, businessTime);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("amount", amount).add("field", calendarField).add("businessTime", businessTime).toString();
    }
}