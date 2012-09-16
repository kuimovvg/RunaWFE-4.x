package ru.runa.bpm.ui.common;

import ru.runa.bpm.ui.common.model.GraphElement;

public interface IElementTypeDefinition {

    String getEntryLabel();

    Class<? extends GraphElement> getModelClass();
}
