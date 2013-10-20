package ru.runa.gpd.formeditor.wysiwyg;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import ru.runa.gpd.formeditor.ftl.bean.FtlComponent;
import ru.runa.gpd.help.IHelpToticProvider;

public class FtlComponentHelpProvider implements IHelpToticProvider {

    @Override
    public String getHelpTopicUrl(ISelection selection) {
        if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
            return null;

        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
        FtlComponent selectedComponent = (FtlComponent) structuredSelection.getFirstElement();

        return selectedComponent.getType().helpPage;
    }

    @Override
    public String getHelpTopicUrl() {
        return null;
    }

}
