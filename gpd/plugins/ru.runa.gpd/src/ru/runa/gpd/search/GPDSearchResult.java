package ru.runa.gpd.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;

import ru.runa.gpd.SharedImages;

public class GPDSearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {
    private GPDSearchQuery query;

    public GPDSearchResult(GPDSearchQuery query) {
        this.query = query;
    }

    public ImageDescriptor getImageDescriptor() {
        return SharedImages.getImageDescriptor("/images/search.gif");
    }

    public String getLabel() {
        return query.getResultLabel(getMatchCount());
    }

    public String getTooltip() {
        return getLabel();
    }

    public ISearchQuery getQuery() {
        return query;
    }

    @Override
    public IEditorMatchAdapter getEditorMatchAdapter() {
        return this;
    }

    public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
        return new Match[0];
    }

    public boolean isShownInEditor(Match match, IEditorPart editor) {
        return false;
    }

    @Override
    public IFileMatchAdapter getFileMatchAdapter() {
        return this;
    }

    public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
        return new Match[0];
    }

    private ElementMatch getElementMatch(Object element) {
        Match[] matches = getMatches(element);
        if (matches.length == 0)
            return new ElementMatch(null);
        return (ElementMatch) matches[0].getElement();
    }

    @Override
    public int getMatchCount(Object element) {
        ElementMatch elementMatch = getElementMatch(element);
        return elementMatch.getMatchesCount() + elementMatch.getPotentialMatchesCount();
    }

    public int getPotentialMatchCount(Object element) {
        return getElementMatch(element).getPotentialMatchesCount();
    }

    public int getStrictMatchCount(Object element) {
        return getElementMatch(element).getMatchesCount();
    }

    public IFile getFile(Object element) {
        return ((ElementMatch) element).getFile();
    }
    
}
