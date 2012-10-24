package org.jbpm.ui.editor.search;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;
import org.jbpm.ui.common.model.ProcessDefinition;
import org.jbpm.ui.resource.Messages;

public class GPDSearchQuery implements ISearchQuery {

    private final IFile definitionFile;
    private final ProcessDefinition definition;
    private final String searchText;
    private final String description;
    private GPDSearchResult result;

    public GPDSearchQuery(IFile definitionFile, ProcessDefinition definition, String searchText) {
        this.definitionFile = definitionFile;
        this.definition = definition;
        this.description = definition.getName();
        this.searchText = searchText;
    }

    public ProcessDefinition getProcessDefinition() {
        return definition;
    }

    public IFile getDefinitionFile() {
        return definitionFile;
    }

    public String getSearchText() {
        return searchText;
    }

    public boolean canRunInBackground() {
        return true;
    }

    public String getLabel() {
        return Messages.getString("Search.jobName");
    }

    public boolean canRerun() {
        return true;
    }

    public GPDSearchResult getSearchResult() {
        if (result == null) {
            result = new GPDSearchResult(this);
            new SearchResultUpdater(result);
        }
        return result;
    }

    public IStatus run(final IProgressMonitor monitor) {
        getSearchResult().removeAll();
        return new ProcessDefinitionSearchVisitor(this).search(monitor);
    }

    public String getResultLabel(int nMatches) {
        Object[] args = { searchText, description, nMatches };
        return MessageFormat.format("\"{0}\" - \"{1}\":{2}", args);
    }

    public static class SearchResultUpdater implements IQueryListener {

        private final GPDSearchResult result;

        public SearchResultUpdater(GPDSearchResult result) {
            this.result = result;
            NewSearchUI.addQueryListener(this);
        }

        public void queryAdded(ISearchQuery query) {
            // don't care
        }

        public void queryRemoved(ISearchQuery query) {
            if (result.equals(query.getSearchResult())) {
                NewSearchUI.removeQueryListener(this);
            }
        }

        public void queryStarting(ISearchQuery query) {
            // don't care
        }

        public void queryFinished(ISearchQuery query) {
            // don't care
        }
    }

}
