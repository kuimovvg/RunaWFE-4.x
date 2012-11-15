package ru.runa.gpd.editor.gef;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.EditorActionBarContributor;

public class EditorActionsContributor extends EditorActionBarContributor {
    @Override
    public void init(IActionBars bars) {
        //        bars.setGlobalActionHandler("ru.runa.gpd.save", new SaveAction(getPage().getWorkbenchWindow()));
        //        bars.setGlobalActionHandler("ru.runa.gpd.saveAll", new SaveAllAction(getPage().getWorkbenchWindow()));
        super.init(bars);
    }
}
