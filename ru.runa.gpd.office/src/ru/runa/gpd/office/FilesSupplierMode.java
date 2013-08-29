package ru.runa.gpd.office;

public enum FilesSupplierMode {
    IN, OUT, BOTH;

    public boolean isInSupported() {
        return this == IN || this == BOTH;
    }

    public boolean isOutSupported() {
        return this == OUT || this == BOTH;
    }

}
