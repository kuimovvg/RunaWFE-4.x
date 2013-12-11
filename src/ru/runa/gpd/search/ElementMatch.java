package ru.runa.gpd.search;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.lang.model.GraphElement;

import com.google.common.base.Objects;

public class ElementMatch {
    public static String CONTEXT_SWIMLANE = "swimlane";
    public static String CONTEXT_TIMED_VARIABLE = "itimed_conf";
    public static String CONTEXT_FORM = "form";
    public static String CONTEXT_FORM_VALIDATION = "form_validation";
    public static String CONTEXT_BOT_TASK_LINK = "botTaskLink";
    public static String CONTEXT_BOT_TASK = "botTask";
    public static String CONTEXT_PROCESS_DEFINITION = "processDefinition";
    private String context;
    private IFile file;
    private GraphElement graphElement;
    private int matchesCount;
    private int potentialMatchesCount;
    private ElementMatch parent;

    public ElementMatch(GraphElement graphElement, IFile file, String context) {
        this.graphElement = graphElement;
        this.file = file;
        this.context = context;
    }

    public ElementMatch(GraphElement graphElement, IFile file) {
        this(graphElement, file, null);
    }

    public ElementMatch() {
        this(null, null);
    }

    public ElementMatch getParent() {
        return parent;
    }

    public void setParent(ElementMatch parent) {
        this.parent = parent;
    }

    public String getContext() {
        return context;
    }

    public IFile getFile() {
        return file;
    }

    public GraphElement getGraphElement() {
        return graphElement;
    }

    public int getMatchesCount() {
        return matchesCount;
    }

    public void setMatchesCount(int matchesCount) {
        this.matchesCount = matchesCount;
    }

    public int getPotentialMatchesCount() {
        return potentialMatchesCount;
    }

    public void setPotentialMatchesCount(int potentialMatchesCount) {
        this.potentialMatchesCount = potentialMatchesCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ElementMatch) {
            ElementMatch m = (ElementMatch) obj;
            return Objects.equal(graphElement, m.graphElement) && Objects.equal(context, m.context);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(graphElement, context);
    }
}
