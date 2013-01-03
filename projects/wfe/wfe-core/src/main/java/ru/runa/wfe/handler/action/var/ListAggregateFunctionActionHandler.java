package ru.runa.wfe.handler.action.var;

import java.util.List;

import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.handler.action.ParamBasedActionHandler;

import com.google.common.collect.Lists;

public class ListAggregateFunctionActionHandler extends ParamBasedActionHandler {

    @Override
    protected void executeAction() throws Exception {
        List<?> list = getInputParam(List.class, "list", null);
        if (list == null) {
            list = Lists.newArrayList();
        }
        String function = getInputParam(String.class, "function");
        Object result;
        if ("SUM".equals(function)) {
            result = getSum(list);
        } else if ("AVERAGE".equals(function)) {
            double sum = getSum(list).doubleValue();
            result = sum / getCount(list);
        } else if ("COUNT".equals(function)) {
            result = getCount(list);
        } else if ("MIN".equals(function)) {
            boolean doubleValue = false;
            double min = Double.MAX_VALUE;
            for (Object object : list) {
                if (object == null) {
                    continue;
                } else if (object instanceof Number) {
                    if (min > ((Number) object).doubleValue()) {
                        min = ((Number) object).doubleValue();
                    }
                    if (object instanceof Double) {
                        doubleValue = true;
                    }
                } else {
                    throw new InternalApplicationException("Function is applicable to list with numbers only, found " + object.getClass());
                }
            }
            if (doubleValue) {
                result = min;
            } else {
                result = new Long((long) min);
            }
        } else if ("MAX".equals(function)) {
            boolean doubleValue = false;
            double max = Double.MIN_VALUE;
            for (Object object : list) {
                if (object == null) {
                    continue;
                } else if (object instanceof Number) {
                    if (max < ((Number) object).doubleValue()) {
                        max = ((Number) object).doubleValue();
                    }
                    if (object instanceof Double) {
                        doubleValue = true;
                    }
                } else {
                    throw new InternalApplicationException("Function is applicable to list with numbers only, found " + object.getClass());
                }
            }
            if (doubleValue) {
                result = max;
            } else {
                result = new Long((long) max);
            }
        } else {
            throw new ConfigurationException("Unknown function '" + function + "'");
        }
        setOutputVariable("object", result);
    }

    private Number getSum(List<?> list) {
        boolean doubleValue = false;
        double sum = 0;
        for (Object object : list) {
            if (object == null) {
                continue;
            } else if (object instanceof Number) {
                sum += ((Number) object).doubleValue();
                if (object instanceof Double) {
                    doubleValue = true;
                }
            } else {
                throw new InternalApplicationException("Function is applicable to list with numbers only, found " + object.getClass());
            }
        }
        if (doubleValue) {
            return sum;
        } else {
            return new Long((long) sum);
        }
    }

    private long getCount(List<?> list) {
        long count = 0;
        for (Object object : list) {
            if (object == null) {
                continue;
            }
            count++;
        }
        return count;
    }

}
