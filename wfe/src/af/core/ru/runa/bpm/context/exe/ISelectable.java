package ru.runa.bpm.context.exe;

/**
 * Option for multi-choice. toString must return value of the option.
 * 
 * @author dofs
 */
public interface ISelectable {

    String getDisplayName();

    String getValue();
}
