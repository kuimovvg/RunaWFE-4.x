package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;

import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.swimlane.SwimlaneInitializer;
import ru.runa.gpd.swimlane.SwimlaneInitializerParser;
import ru.runa.wfe.extension.assign.DefaultAssignmentHandler;
import ru.runa.wfe.var.format.ExecutorFormat;

public class Swimlane extends Variable implements Delegable {
    private static final String DELEGATION_CLASS_NAME = DefaultAssignmentHandler.class.getName();

    public Swimlane() {
        super(ExecutorFormat.class.getName(), false, null);
        setDelegationClassName(DELEGATION_CLASS_NAME);
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.ASSIGNMENT;
    }

    @Override
    public void setName(String name) {
        if (getProcessDefinition().getSwimlaneByName(name) != null) {
            return;
        }
        super.setName(name);
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        try {
            SwimlaneInitializer swimlaneInitializer = SwimlaneInitializerParser.parse(getDelegationConfiguration());
            if (swimlaneInitializer != null) {
                swimlaneInitializer.validate(this, errors);
            }
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith(OrgFunctionDefinition.MISSED_DEFINITION)) {
                errors.add(ValidationError.createLocalizedWarning(this, "orgfunction.missed"));
            } else {
                errors.add(ValidationError.createLocalizedError(this, "orgfunction.broken"));
            }
        }
    }

    @Override
    public Image getEntryImage() {
        return SharedImages.getImage("icons/obj/swimlane.gif");
    }
}
