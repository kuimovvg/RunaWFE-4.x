package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;

import ru.runa.common.web.html.TDBuilder;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.execution.dto.WfProcess;

public class RootProcessTDBuilder implements TDBuilder {

    public RootProcessTDBuilder() {
    }

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getValue(object, env));
        return td;
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        WfProcess process = (WfProcess) object;
        return ProcessHierarchyUtils.getProcessIdsArray(process.getHierarchySubProcess());
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        WfProcess process = (WfProcess) object;
        return ProcessHierarchyUtils.getProcessIdsArray(process.getHierarchySubProcess()).length;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfProcess process = (WfProcess) object;
        return ProcessHierarchyUtils.getRootProcessIdString(process.getHierarchySubProcess());
    }

}
