package ru.runa.wfe.history.layout;

/**
 * History graph layout common properties.
 */
public class HistoryGraphLayoutProperties {

    /**
     * Maximum node height + height between nodes. If node exceed this height, it will overlaps with
     * other nodes.
     */
    public static final int maxCellHeight = 150;

    /**
     * Maximum node height. If node exceed this height, it will overlaps with
     * other nodes.
     */
    public static final int maxNodeHeight = 130;

    /**
     * Width between 2 nodes.
     */
    public static final int widthBetweenNodes = 20;

    /**
     * Height for join element.
     */
    public static final int joinHeight = 4;
}
