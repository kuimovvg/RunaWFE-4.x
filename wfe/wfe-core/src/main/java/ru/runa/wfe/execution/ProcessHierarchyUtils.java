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

    public static String[] getProcessIdsArray(String hierarchy) {
        if (hierarchy == null) {
            return null;
        }
        return hierarchy.split("/");
    }

    public static List<Long> getProcessIds(String hierarchy) {
        String[] stringIds = getProcessIdsArray(hierarchy);
        List<Long> processIdsHierarchy = Lists.newArrayListWithExpectedSize(stringIds.length);
        for (String stringId : stringIds) {
            processIdsHierarchy.add(Long.parseLong(stringId));
        }
        return processIdsHierarchy;
    }

    public static String getParentProcessIdString(String hierarchy) {
        if (hierarchy == null) {
            return null;
        }
        String[] stringIds = hierarchy.split("/");
        return stringIds[0];
    }
}
