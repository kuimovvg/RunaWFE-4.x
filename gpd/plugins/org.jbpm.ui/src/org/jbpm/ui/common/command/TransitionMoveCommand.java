package ru.runa.bpm.ui.common.command;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import ru.runa.bpm.ui.common.figure.GEFConstants;
import ru.runa.bpm.ui.common.model.Bendpoint;
import ru.runa.bpm.ui.common.model.Transition;

public class TransitionMoveCommand extends Command {

    private final Transition transition;
    private final ChangeBoundsRequest request;
    private List<Bendpoint> oldBendpoints;

    public TransitionMoveCommand(Transition transition, ChangeBoundsRequest request) {
        this.transition = transition;
        this.request = request;
    }

    @Override
    public void execute() {
        if (oldBendpoints == null) {
            oldBendpoints = new ArrayList<Bendpoint>(transition.getBendpoints());
        }
        List<Bendpoint> newBendpoints = new ArrayList<Bendpoint>(transition.getBendpoints().size());
        for (Bendpoint oldBendpoint : oldBendpoints) {
            int xCount = (int) Math.round((double) request.getMoveDelta().x / GEFConstants.GRID_SIZE);
            int x = oldBendpoint.getX() + xCount * GEFConstants.GRID_SIZE;
            int yCount = (int) Math.round((double) request.getMoveDelta().y / GEFConstants.GRID_SIZE);
            int y = oldBendpoint.getY() + yCount * GEFConstants.GRID_SIZE;
            newBendpoints.add(new Bendpoint(x, y));
        }
        transition.setBendpoints(newBendpoints);
    }

    @Override
    public void undo() {
        transition.setBendpoints(oldBendpoints);
    }

}
