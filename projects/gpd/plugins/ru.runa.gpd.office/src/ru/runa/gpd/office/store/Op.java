package ru.runa.gpd.office.store;

import ru.runa.gpd.office.Messages;

public enum Op {
    EQUAL("==", Messages.getString("op.eq")), NOT_EQUAL("!=", Messages.getString("op.noteq")), GREATER_THAN(">", Messages.getString("op.gt")), LESS_THAN(
            "<", Messages.getString("op.lt")), GREATER_OR_EQUAL(">=", Messages.getString("op.gteq")), LESS_OR_EQUAL("<=", Messages
            .getString("op.lteq")), LIKE("like", Messages.getString("op.like"));

    private final String symbol;
    private final String message;

    private Op(String symbol, String message) {
        this.symbol = symbol;
        this.message = message;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getMessage() {
        return message;
    }

    public static Op fromSymbol(String sym) {
        for (Op op : values()) {
            if (op.getSymbol().equals(sym)) {
                return op;
            }
        }
        throw new IllegalArgumentException();
    }

}
