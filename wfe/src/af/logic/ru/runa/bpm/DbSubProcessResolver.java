package ru.runa.bpm;

import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.node.SubProcessResolver;
import ru.runa.wf.caches.ProcessDefCacheCtrl;

public class DbSubProcessResolver implements SubProcessResolver {
    private static final long serialVersionUID = 1L;

    @Autowired
    private ProcessDefCacheCtrl processDefCacheCtrl;

    @Override
    public ExecutableProcessDefinition findSubProcess(Element subProcessElement) {
        String subProcessName = subProcessElement.attributeValue("name");
        if (subProcessName == null) {
            throw new InternalApplicationException("no sub-process name specified in process-state: " + subProcessElement.asXML());
        }
        return processDefCacheCtrl.getLatestDefinition(subProcessName);
    }
}
