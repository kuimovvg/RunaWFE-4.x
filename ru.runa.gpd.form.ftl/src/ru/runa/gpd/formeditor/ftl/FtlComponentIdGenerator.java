package ru.runa.gpd.formeditor.ftl;

public final class FtlComponentIdGenerator {
    private static int maxComponentId = 0;

    public static synchronized int generate() {
        return maxComponentId++;
    }

}
