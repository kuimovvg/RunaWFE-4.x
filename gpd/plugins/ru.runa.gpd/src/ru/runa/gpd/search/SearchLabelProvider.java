package ru.runa.gpd.search;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;

public class SearchLabelProvider extends LabelProvider {
    private final SearchPage page;

    public SearchLabelProvider(SearchPage page) {
        this.page = page;
    }

    @Override
    public String getText(Object element) {
        ElementMatch elementMatch = (ElementMatch) element;
        return elementMatch.toString((SearchResult) page.getInput());
    }

    @Override
    public Image getImage(Object element) {
        ElementMatch elementMatch = (ElementMatch) element;
        if (ElementMatch.CONTEXT_FORM.equals(elementMatch.getContext()) || ElementMatch.CONTEXT_FORM_VALIDATION.equals(elementMatch.getContext())) {
            return SharedImages.getImage("icons/show_in_file.gif");
        }
        if (ElementMatch.CONTEXT_BOT_TASK.equals(elementMatch.getContext())) {
            return SharedImages.getImage("icons/bot_task.gif");
        }
        if (ElementMatch.CONTEXT_BOT_TASK_LINK.equals(elementMatch.getContext())) {
            return SharedImages.getImage("icons/bot_task_formal.gif");
        }
        return elementMatch.getGraphElement().getEntryImage();
    }
}
