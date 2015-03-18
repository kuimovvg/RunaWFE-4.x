package ru.runa.wfe.office.storage;

public enum Op {
    /**
     * Equal
     */
    EQUAL("=="),
    /**
     * Not equal
     */
    NOT_EQUAL("!="),
    /**
     * Greater than
     */
    GREATER_THAN(">"),
    /**
     * Less than
     */
    LESS_THAN("<"),
    /**
     * Greater or equal
     */
    GREATER_OR_EQUAL(">="),
    /**
     * Less or equal
     */
    LESS_OR_EQUAL("<="),
    /**
     * Like condition
     */
    LIKE("'%s'.indexOf('%s') != -1"),
    /**
     * Insensitive Like condition (ignore case)
     */
    ILIKE("'%s'.toUpperCase().indexOf('%s'.toUpperCase()) != -1");

    private final String symbol;

    private Op(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
