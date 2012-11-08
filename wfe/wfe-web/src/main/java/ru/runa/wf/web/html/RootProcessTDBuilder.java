package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;

import ru.runa.common.web.html.TDBuilder;
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
        WfProcess pd = (WfProcess) object;
        return pd.getHierarchySubProcess().split("/");
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        WfProcess pd = (WfProcess) object;
        return pd.getHierarchySubProcess().split("/").length;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfProcess pd = (WfProcess) object;
        String[] ids = pd.getHierarchySubProcess().split("/");
        String result = ids[ids.length - 1];
        if (result == null) {
            result = "";
        }
        return result;
    }

}
