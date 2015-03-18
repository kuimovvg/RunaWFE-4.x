package ru.runa.gpd.office.store;

import java.util.List;

import com.google.common.collect.Lists;

public class ConditionModel {

    private final List<ConditionItem> conditions = Lists.newArrayList();

    public ConditionModel() {
    }

    public List<ConditionItem> getConditions() {
        return conditions;
    }
}
