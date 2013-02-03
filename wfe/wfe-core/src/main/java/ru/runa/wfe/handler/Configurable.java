package ru.runa.wfe.handler;

/**
 * Provides common interface for configurable artifacts.
 * 
 * @author dofs
 * @since 4.0
 */
public interface Configurable {

    /**
     * Configures bean.
     */
    public void setConfiguration(String configuration) throws Exception;

}
