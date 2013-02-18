package ru.runa.wfe.user.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.beanutils.BeanUtils;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.user.Executor;

public class ExecutorAdapter extends XmlAdapter<WfExecutor, Executor> {

    @Override
    public WfExecutor marshal(Executor executor) {
        WfExecutor wfExecutor = new WfExecutor();
        wfExecutor.setId(executor.getId());
        wfExecutor.setExecutorClassName(executor.getClass().getName());
        try {
            BeanUtils.copyProperties(wfExecutor, executor);
            // Map<String, String> map = BeanUtils.describe(executor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wfExecutor;
    }

    @Override
    public Executor unmarshal(WfExecutor executor) {
        return ApplicationContextFactory.getExecutorDAO().getExecutor(executor.getId());
    }

}
