package ru.runa.bpm.ui;

import ru.runa.bpm.ui.common.model.ProcessDefinition;

public interface JpdlModelConverter {

	ProcessDefinition convert(ProcessDefinition previous, String newVersion) throws Exception;

}
