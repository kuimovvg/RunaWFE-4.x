package ru.runa.gpd.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ITimed;

public class TimerDuration {
    public static final String EMPTY = "0 minutes";
    private static Pattern PATTERN_VAR = Pattern.compile("#\\{(.*)}");
    private static final List<Unit> units = new ArrayList<Unit>();
    static {
        units.add(new Unit("minutes"));
        units.add(new Unit("business minutes"));
        units.add(new Unit("hours"));
        units.add(new Unit("business hours"));
        units.add(new Unit("days"));
        units.add(new Unit("business days"));
        units.add(new Unit("weeks"));
        units.add(new Unit("business weeks"));
        units.add(new Unit("months"));
        units.add(new Unit("business months"));
        units.add(new Unit("years"));
        units.add(new Unit("business years"));
        units.add(new Unit("seconds"));
    }

    private String delay;
    private Unit unit;
    private String variableName;

    public TimerDuration(String duration) {
        if (duration == null) {
            throw new NullPointerException("duration is null");
        }
        setDuration(duration);
    }
    
    public void setDuration(String duration) {
        Matcher matcher = PATTERN_VAR.matcher(duration);
        String sign = "";
        if (matcher.find()) {
            variableName = matcher.group(1);
            duration = duration.substring(matcher.end()).trim();
            sign = duration.substring(0, 1);
            duration = duration.substring(1).trim();
        }
        int backspaceIndex = duration.indexOf(" ");
        delay = sign + duration.substring(0, backspaceIndex);
        String unitValue = duration.substring(backspaceIndex + 1);
        for (Unit unit : units) {
            if (unit.value.equals(unitValue)) {
                setUnit(unit);
                break;
            }
        }
    }

    public String getDuration() {
        delay = delay.trim();
        String duration = "";
        if (variableName != null) {
            duration = "#{" + variableName + "} ";
            if (delay.charAt(0) != '-' && delay.charAt(0) != '+') {
                delay = "+ " + delay;
            } else if (delay.charAt(1) != ' ') {
                delay = delay.substring(0, 1) + " " + delay.substring(1);
            }
        } else {
            delay = delay.replaceAll(" ", "");
        }
        duration += delay + " " + unit.value;
        return duration;
    }

    public String getDurationLabel() {
        delay = delay.trim();
        String duration = "";
        if (variableName != null) {
            duration = "#{" + variableName + "} ";
            if (delay.charAt(0) != '-' && delay.charAt(0) != '+') {
                delay = "+ " + delay;
            } else if (delay.charAt(1) != ' ') {
                delay = delay.substring(0, 1) + " " + delay.substring(1);
            }
        } else {
            delay = delay.replaceAll(" ", "");
        }
        PhraseDecliner aaa = PhraseDecliner.getDecliner();
        if (aaa != null) {
            duration += aaa.declineDuration(delay, unit.label);
        } else {
            duration += delay + " " + unit.label;
        }
        return duration;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        if (ITimed.CURRENT_DATE_MESSAGE.equals(variableName)) {
            variableName = null;
        }
        this.variableName = variableName;
    }

    public static List<Unit> getUnits() {
        return units;
    }
    
    @Override
    public String toString() {
        return getDurationLabel();
    }
    
    public boolean hasDuration() {
        return !"0".equals(delay);
    }

    public static class Unit {
        private final String value;
        private final String label;
        public Unit(String value) {
            this.value = value;
            this.label = Localization.getString("unit." + value.replaceAll(" ", ""));
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
}
