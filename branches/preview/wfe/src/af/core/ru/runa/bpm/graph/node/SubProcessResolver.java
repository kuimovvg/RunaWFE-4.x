package ru.runa.bpm.graph.node;

import java.io.Serializable;

import org.dom4j.Element;

import ru.runa.bpm.graph.def.ExecutableProcessDefinition;

public interface SubProcessResolver extends Serializable {

    ExecutableProcessDefinition findSubProcess(Element subProcessElement);

}
