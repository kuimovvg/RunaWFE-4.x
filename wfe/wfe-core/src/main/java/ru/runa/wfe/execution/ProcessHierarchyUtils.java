package ru.runa.wfe.execution;

import java.util.List;

import com.google.common.collect.Lists;

public class ProcessHierarchyUtils {

    public static String createHierarchy(String parentHierarchy, Long processId) {
        if (parentHierarchy == null) {
            return processId.toString();
        }
        return parentHierarchy + "/" + processId;
    }

    public static List<Long> getProcessIds(String hierarchy) {
        String[] stringIds = hierarchy.split("/");
        List<Long> processIdsHierarchy = Lists.newArrayListWithExpectedSize(stringIds.length);
        for (String stringId : stringIds) {
            processIdsHierarchy.add(Long.parseLong(stringId));
        }
        return processIdsHierarchy;
    }

}
