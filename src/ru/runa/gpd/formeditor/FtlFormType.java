package ru.runa.gpd.formeditor;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil.MyTemplateExceptionHandler;
import ru.runa.gpd.formeditor.ftl.FreemarkerUtil.ValidationHashModel;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.action.OpenToolPaletteExplorer;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FtlFormType extends BaseHtmlFormType {

    @Override
    public IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException {
        openPaletteWindow();
        return super.openForm(formFile, formNode);
    }

    @Override
    protected Map<String, FormVariableAccess> getTypeSpecificVariableNames(FormNode formNode, byte[] formBytes) throws Exception {
        Configuration cfg = new Configuration();
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        cfg.setLocalizedLookup(false);
        cfg.setTemplateExceptionHandler(new MyTemplateExceptionHandler());
        String string = new String(formBytes, "UTF-8");
        Template template = new Template("test", new StringReader(string), cfg, "UTF-8");
        StringWriter out = new StringWriter();
        ValidationHashModel validationHashModel = new ValidationHashModel(formNode.getProcessDefinition());
        template.process(validationHashModel, out);
        Map<String, FormVariableAccess> map = validationHashModel.getUsedVariables();
        map.remove("context");
        return map;
    }

    private void openPaletteWindow() throws PartInitException {
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .showView(OpenToolPaletteExplorer.VIEW_ID, null, IWorkbenchPage.VIEW_VISIBLE);
        } catch (Exception e) {
            PluginLogger.logError("error occurred when opening the palette window", e);
        }
    }
}
