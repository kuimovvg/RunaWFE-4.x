package ru.runa.gpd.search;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.search.internal.core.text.FileCharSequenceProvider;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.Match;

import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableMapping;

public class ProcessDefinitionSearchVisitor {
    private final GPDSearchQuery query;
    private IProgressMonitor progressMonitor;
    private int numberOfScannedElements;
    private int numberOfElementsToScan;
    private GraphElement currentElement = null;
    private final MultiStatus status;
    private final FileCharSequenceProvider fileCharSequenceProvider;
    private final Matcher matcher;
    private final Matcher matcherWithBrackets;

    public ProcessDefinitionSearchVisitor(GPDSearchQuery query) {
        this.query = query;
        this.status = new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, SearchMessages.TextSearchEngine_statusMessage, null);
        this.fileCharSequenceProvider = new FileCharSequenceProvider();
        this.matcher = Pattern.compile(Pattern.quote(query.getSearchText())).matcher("");
        this.matcherWithBrackets = Pattern.compile(Pattern.quote("\"" + query.getSearchText() + "\"")).matcher("");
    }

    public IStatus search(GPDSearchResult searchResult, IProgressMonitor monitor) {
        progressMonitor = monitor == null ? new NullProgressMonitor() : monitor;
        numberOfScannedElements = 0;
        numberOfElementsToScan = query.getProcessDefinition().getChildrenRecursive(GraphElement.class).size();
        Job monitorUpdateJob = new Job(SearchMessages.TextSearchVisitor_progress_updating_job) {
            private int lastNumberOfScannedElements = 0;

            @Override
            public IStatus run(IProgressMonitor inner) {
                while (!inner.isCanceled()) {
                    if (currentElement != null && currentElement instanceof NamedGraphElement) {
                        String name = ((NamedGraphElement) currentElement).getName();
                        Object[] args = { name, numberOfScannedElements, numberOfElementsToScan };
                        progressMonitor.subTask(MessageFormat.format(SearchMessages.TextSearchVisitor_scanning, args));
                        int steps = numberOfScannedElements - lastNumberOfScannedElements;
                        progressMonitor.worked(steps);
                        lastNumberOfScannedElements += steps;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return Status.OK_STATUS;
                    }
                }
                return Status.OK_STATUS;
            }
        };
        try {
            String taskName = SearchMessages.TextSearchVisitor_filesearch_task_label;
            progressMonitor.beginTask(taskName, numberOfElementsToScan);
            monitorUpdateJob.setSystem(true);
            monitorUpdateJob.schedule();
            try {
                List<GraphElement> children = query.getProcessDefinition().getChildrenRecursive(GraphElement.class);
                for (GraphElement child : children) {
                    if (!processNode(query.getDefinitionFile(), child)) {
                        break;
                    }
                }
                return status;
            } finally {
                monitorUpdateJob.cancel();
            }
        } finally {
            progressMonitor.done();
        }
    }

    public IStatus search(IProgressMonitor monitor) {
        return search(query.getSearchResult(), monitor);
    }

    private boolean processNode(IFile definitionFile, GraphElement graphElement) {
        try {
            currentElement = graphElement;
            if (graphElement instanceof FormNode) {
                processFormNode(definitionFile, (FormNode) graphElement);
            }
            if (graphElement instanceof Action) {
                processDelegableNode(definitionFile, (Delegable) graphElement);
            }
            if (graphElement instanceof ITimed) {
                processTimedNode(definitionFile, (ITimed) graphElement);
            }
            if (graphElement instanceof Decision) {
                processDelegableNode(definitionFile, (Delegable) graphElement);
            }
            if (graphElement instanceof Subprocess) {
                processSubprocessNode(definitionFile, (Subprocess) graphElement);
            }
        } catch (Exception e) {
            status.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
            return false;
        } finally {
            numberOfScannedElements++;
        }
        if (progressMonitor.isCanceled()) {
            throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled);
        }
        return true;
    }

    private void processDelegableNode(IFile definitionFile, Delegable delegable) throws Exception {
        String conf = delegable.getDelegationConfiguration();
        ElementMatch elementMatch = new ElementMatch((GraphElement) delegable, definitionFile);
        List<Match> matches = findInString(elementMatch, conf, matcher);
        elementMatch.setPotentialMatchesCount(matches.size());
        for (Match match : matches) {
            query.getSearchResult().addMatch(match);
        }
    }

    private void processTimedNode(IFile definitionFile, ITimed timedNode) throws Exception {
        Timer timer = timedNode.getTimer();
        if (timer == null) {
            return;
        }
        if (query.getSearchText().equals(timer.getDelay().getVariableName())) {
            ElementMatch elementMatch = new ElementMatch((GraphElement) timedNode, definitionFile, ElementMatch.CONTEXT_TIMED_VARIABLE);
            elementMatch.setMatchesCount(1);
            query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
        }
    }

    private void processSubprocessNode(IFile definitionFile, Subprocess subprocessNode) throws Exception {
        List<VariableMapping> mappings = subprocessNode.getVariablesList();
        int matchesCount = 0;
        for (VariableMapping mapping : mappings) {
            if (mapping.getProcessVariable().equals(query.getSearchText())) {
                matchesCount++;
            }
        }
        if (matchesCount > 0) {
            ElementMatch elementMatch = new ElementMatch(subprocessNode);
            elementMatch.setMatchesCount(matchesCount);
            query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
        }
    }

    private void processFormNode(IFile definitionFile, FormNode formNode) throws Exception {
        ElementMatch formElementMatch = new ElementMatch(formNode, definitionFile, ElementMatch.CONTEXT_SWIMLANE);
        if (formNode.hasForm()) {
            IFile file = IOUtils.getAdjacentFile(definitionFile, formNode.getFormFileName());
            Map<String, Integer> formVariables = formNode.getFormVariables((IFolder) definitionFile.getParent());
            ElementMatch elementMatch = new ElementMatch(formNode, file, ElementMatch.CONTEXT_FORM);
            elementMatch.setParent(formElementMatch);
            int matchesCount = 0;
            if (formVariables.keySet().contains(query.getSearchText())) {
                matchesCount++;
            }
            elementMatch.setMatchesCount(matchesCount);
            List<Match> matches = findInFile(elementMatch, file, matcher);
            elementMatch.setPotentialMatchesCount(matches.size() - matchesCount);
            for (Match match : matches) {
                query.getSearchResult().addMatch(match);
            }
        }
        if (formNode.hasFormValidation()) {
            IFile file = IOUtils.getAdjacentFile(definitionFile, formNode.getValidationFileName());
            Set<String> validationVariables = formNode.getValidationVariables((IFolder) definitionFile.getParent());
            ElementMatch elementMatch = new ElementMatch(formNode, file, ElementMatch.CONTEXT_FORM_VALIDATION);
            elementMatch.setParent(formElementMatch);
            int matchesCount = 0;
            if (validationVariables.contains(query.getSearchText())) {
                matchesCount++;
            }
            elementMatch.setMatchesCount(matchesCount);
            List<Match> matches = findInFile(elementMatch, file, matcherWithBrackets);
            elementMatch.setPotentialMatchesCount(matches.size() - matchesCount);
            for (Match match : matches) {
                query.getSearchResult().addMatch(match);
            }
        }
        String swimlaneName = ((SwimlanedNode) formNode).getSwimlaneName();
        if (query.getSearchText().equals(swimlaneName)) {
            formElementMatch.setMatchesCount(1);
            query.getSearchResult().addMatch(new Match(formElementMatch, 0, 0));
        }
    }

    private List<Match> findInFile(ElementMatch elementMatch, IFile file, Matcher matcher) throws CoreException, IOException {
        CharSequence searchInput = null;
        try {
            // FIXME set read to UTF8
            searchInput = fileCharSequenceProvider.newCharSequence(file);
            return findInString(elementMatch, searchInput, matcher);
        } finally {
            if (searchInput != null) {
                fileCharSequenceProvider.releaseCharSequence(searchInput);
            }
        }
    }

    private List<Match> findInString(ElementMatch elementMatch, CharSequence searchInput, Matcher matcher) throws CoreException, IOException {
        List<Match> matches = new ArrayList<Match>();
        matcher.reset(searchInput);
        int k = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (end != start) {
                matches.add(new Match(elementMatch, start, end - start));
            }
            if (k++ == 20) {
                if (progressMonitor.isCanceled()) {
                    throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled);
                }
                k = 0;
            }
        }
        return matches;
    }
}
