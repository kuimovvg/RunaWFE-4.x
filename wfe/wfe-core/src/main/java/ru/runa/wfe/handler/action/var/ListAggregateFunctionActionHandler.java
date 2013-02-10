package ru.runa.wfe.handler.action.var;

import java.util.List;

import ru.runa.wfe.WfException;
import ru.runa.wfe.handler.CommonParamBasedHandler;
import ru.runa.wfe.handler.HandlerData;

import com.google.common.collect.Lists;

public class ListAggregateFunctionActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List<?> list = handlerData.getInputParam(List.class, "list", null);
        if (list == null) {
            list = Lists.newArrayList();
        }
        String function = handlerData.getInputParam(String.class, "function");
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
                    throw new WfException("Function is applicable to list with numbers only, found " + object.getClass());
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
                    throw new WfException("Function is applicable to list with numbers only, found " + object.getClass());
                }
            }
            if (doubleValue) {
                result = max;
            } else {
                result = new Long((long) max);
            }
        } else {
            throw new Exception("Unknown function '" + function + "'");
        }
        handlerData.setOutputVariable("object", result);
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
                throw new WfException("Function is applicable to list with numbers only, found " + object.getClass());
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
