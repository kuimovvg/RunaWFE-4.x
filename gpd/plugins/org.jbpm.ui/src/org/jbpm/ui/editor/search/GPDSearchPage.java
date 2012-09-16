package ru.runa.bpm.ui.editor.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.internal.ui.text.IFileSearchContentProvider;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.common.action.OpenFormValidationDelegate;
import ru.runa.bpm.ui.common.action.SubprocessDelegate;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.common.model.Subprocess;
import ru.runa.bpm.ui.custom.CustomizationRegistry;
import ru.runa.bpm.ui.custom.DelegableProvider;
import ru.runa.bpm.ui.editor.DesignerEditor;
import ru.runa.bpm.ui.forms.FormTypeProvider;

public class GPDSearchPage extends AbstractTextSearchViewPage {
    private IFileSearchContentProvider contentProvider;

    public GPDSearchPage() {
        super(FLAG_LAYOUT_TREE);
    }

    @Override
    public void setActionBars(IActionBars actionBars) {
        super.setActionBars(actionBars);
        getViewer().getControl().getMenu().dispose();
        SubToolBarManager toolBarManager = (SubToolBarManager) actionBars.getToolBarManager();
        IContributionItem[] items = toolBarManager.getParent().getItems();
        for (IContributionItem contributionItem : items) {
            if (contributionItem instanceof ActionContributionItem) {
                ActionContributionItem aci = (ActionContributionItem) contributionItem;
                if ("SearchHistoryDropDownAction".equals(aci.getAction().getClass().getSimpleName())) {
                    toolBarManager.getParent().remove(contributionItem);
                }
                if ("PinSearchViewAction".equals(aci.getAction().getClass().getSimpleName())) {
                    toolBarManager.getParent().remove(contributionItem);
                }
            }
        }
    }

    @Override
    protected void configureTableViewer(TableViewer viewer) {
        throw new UnsupportedOperationException("Only tree view supported");
    }

    @Override
    protected void configureTreeViewer(TreeViewer viewer) {
        viewer.setUseHashlookup(true);
        GPDTreeLabelProvider innerLabelProvider = new GPDTreeLabelProvider(this);
        viewer.setLabelProvider(new DecoratingLabelProvider(innerLabelProvider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
        viewer.setContentProvider(new GPDTreeContentProvider(viewer));
        contentProvider = (IFileSearchContentProvider) viewer.getContentProvider();
    }

    @Override
    protected void showMatch(Match match, int offset, int length, boolean activate) throws PartInitException {
        ElementMatch elementMatch = (ElementMatch) match.getElement();
        IFile file = elementMatch.getFile();

        IEditorPart editor = null;
        if (ElementMatch.CONTEXT_FORM.equals(elementMatch.getContext())) {
            try {
                FormNode formNode = (FormNode) elementMatch.getGraphElement();
                editor = FormTypeProvider.getFormType(formNode.getFormType()).openForm(file, formNode);
            } catch (CoreException e) {
                DesignerLogger.logError(e);
            }
        } else if (ElementMatch.CONTEXT_FORM_VALIDATION.equals(elementMatch.getContext())) {
            FormNode formNode = (FormNode) elementMatch.getGraphElement();
            OpenFormValidationDelegate delegate = new OpenFormValidationDelegate();
            delegate.openValidationFile(formNode, elementMatch.getFile());
        } else if (elementMatch.getGraphElement() instanceof Delegable) {
            Delegable delegable = (Delegable) elementMatch.getGraphElement();
            DelegableProvider provider = CustomizationRegistry.getProvider(delegable.getDelegationClassName());
            String newConfig = provider.showConfigurationDialog(delegable);
            if (newConfig != null) {
                delegable.setDelegationConfiguration(newConfig);
            }
        } else if (elementMatch.getGraphElement() instanceof Subprocess) {
            SubprocessDelegate delegate = new SubprocessDelegate();
            delegate.openDetails((Subprocess) elementMatch.getGraphElement());
        } else if (elementMatch.getGraphElement() != null) {
            DesignerEditor designerEditor = (DesignerEditor) IDE.openEditor(getSite().getPage(), elementMatch.getFile());
            designerEditor.select(elementMatch.getGraphElement());
        }
        if (editor == null) {
            return;
        }
        if (offset != 0 && length != 0) {
            ITextEditor textEditor = null;
            if (editor instanceof ITextEditor) {
                textEditor = (ITextEditor) editor;
            }
            if (textEditor == null && editor.getAdapter(ITextEditor.class) != null) {
                textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
            }
            if (textEditor != null) {
                textEditor.selectAndReveal(offset, length);
            }
        }
    }

    @Override
    protected void elementsChanged(Object[] objects) {
        if (contentProvider != null)
            contentProvider.elementsChanged(objects);
    }

    @Override
    protected void clear() {
        if (contentProvider != null)
            contentProvider.clear();
    }
}
