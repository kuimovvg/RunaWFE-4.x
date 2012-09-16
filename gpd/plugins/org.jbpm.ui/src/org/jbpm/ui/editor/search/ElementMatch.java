package ru.runa.bpm.ui.editor.search;

import org.eclipse.core.resources.IFile;
import ru.runa.bpm.ui.common.model.GraphElement;

public class ElementMatch {
    public static String CONTEXT_SWIMLANE = "swimlane";
    public static String CONTEXT_TIMED_VARIABLE = "itimed_conf";
    public static String CONTEXT_FORM = "form";
    public static String CONTEXT_FORM_VALIDATION = "form_validation";
    
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

    public ElementMatch(GraphElement graphElement) {
        this(graphElement, null);
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
        if (obj == null)
            return false;
        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = graphElement.hashCode();
        if (context != null) {
            hash += 37*context.hashCode();
        }
        return hash;
    }
    
}
