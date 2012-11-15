package ru.runa.gpd.search;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;

public class GPDTreeLabelProvider extends LabelProvider {
    private final GPDSearchPage page;

    public GPDTreeLabelProvider(GPDSearchPage page) {
        this.page = page;
    }

    @Override
    public String getText(Object element) {
        ElementMatch elementMatch = (ElementMatch) element;
        GraphElement graphElement = elementMatch.getGraphElement();
        String text;
        if (ElementMatch.CONTEXT_FORM.equals(elementMatch.getContext())) {
            text = Localization.getString("Search.formNode.form");
        } else if (ElementMatch.CONTEXT_FORM_VALIDATION.equals(elementMatch.getContext())) {
            text = Localization.getString("Search.formNode.validation");
        } else if (graphElement instanceof NamedGraphElement) {
            text = ((NamedGraphElement) graphElement).getName();
        } else {
            text = graphElement.toString();
        }

        if (ElementMatch.CONTEXT_TIMED_VARIABLE.equals(elementMatch.getContext())) {
            text += " [" + Localization.getString("Timer.baseDate") + "]";
        }
        if (ElementMatch.CONTEXT_SWIMLANE.equals(elementMatch.getContext()) && elementMatch.getMatchesCount() > 0) {
            text += " [" + Localization.getString("default.swimlane.name") + "]";
        }

        int strictMatchCount = 0;
        int potentialMatchCount = 0;
        GPDSearchResult result = (GPDSearchResult) page.getInput();
        if (result != null) {
            strictMatchCount = result.getStrictMatchCount(elementMatch);
            potentialMatchCount = result.getPotentialMatchCount(elementMatch) - strictMatchCount;
        }
        if (strictMatchCount == 0 && potentialMatchCount == 0) {
            return text;
        }
        String format;
        if (strictMatchCount > 0 && potentialMatchCount > 0) {
            format = "{0} (" + Localization.getString("Search.matches") + ": {1}, " + Localization.getString("Search.potentialMatches") + ": {2})";
            return MessageFormat.format(format, new Object[] { text, strictMatchCount, potentialMatchCount });
        } else if (strictMatchCount == 0) {
            format = "{0} (" + Localization.getString("Search.potentialMatches") + ": {1})";
            return MessageFormat.format(format, new Object[] { text, potentialMatchCount });
        } else {
            format = "{0} (" + Localization.getString("Search.matches") + ": {1})";
            return MessageFormat.format(format, new Object[] { text, strictMatchCount });
        }
    }

    @Override
    public Image getImage(Object element) {
        ElementMatch elementMatch = (ElementMatch) element;
        if (ElementMatch.CONTEXT_FORM.equals(elementMatch.getContext()) || ElementMatch.CONTEXT_FORM_VALIDATION.equals(elementMatch.getContext())) {
            return SharedImages.getImage("icons/show_in_file.gif");
        }
        return elementMatch.getGraphElement().getEntryImage();
    }
}
