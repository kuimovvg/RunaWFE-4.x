package ru.runa.wfe.security;

public abstract class IdentifiableBase implements Identifiable {
    private static final long serialVersionUID = 1L;

    public abstract Long getId();

    @Override
    public Long getIdentifiableId() {
        return getId();
    }

}
