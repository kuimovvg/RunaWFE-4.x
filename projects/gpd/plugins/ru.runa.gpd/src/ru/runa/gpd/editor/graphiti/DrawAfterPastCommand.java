package ru.runa.gpd.editor.graphiti;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;

/**
 * This is second part of Paste method. But EMF need to do it on other transaction/command
 */
public class DrawAfterPastCommand extends Command {
	
	private List<NamedGraphElement> elementList;
	private ProcessDefinition processDefinition;
	private DiagramEditorPage diagramPage;
	
	public DrawAfterPastCommand( List<NamedGraphElement> elementList, ProcessDefinition processDefinition, DiagramEditorPage diagramPage) {
		this.elementList = elementList;
		this.processDefinition = processDefinition;
		this.diagramPage = diagramPage;
	}
	
	@Override
	public boolean canExecute() {
		return  Language.BPMN.equals(processDefinition.getLanguage());
	}
	
	@Override
	public void execute() {
		diagramPage.drawElements(diagramPage.getDiagramTypeProvider().getDiagram(), elementList);
		List<Transition> transitions = new ArrayList<Transition>();
		for (NamedGraphElement element : elementList) {
			transitions.addAll(element.getChildrenRecursive(Transition.class));
		}
		diagramPage.drawTransitions(transitions);
	}
	
	@Override
	public void undo() {
		super.undo();
		// TODO How to remove graphic presentation of element(nodes)?
	}

}
