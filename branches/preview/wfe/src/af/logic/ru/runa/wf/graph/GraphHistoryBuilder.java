/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.graph;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.Node.NodeType;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.log.ProcessInstanceCreateLog;
import ru.runa.bpm.graph.log.ProcessInstanceEndLog;
import ru.runa.bpm.graph.log.ProcessStateLog;
import ru.runa.bpm.graph.log.TokenCreateLog;
import ru.runa.bpm.graph.log.TokenEndLog;
import ru.runa.bpm.graph.log.TransitionLog;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.taskmgmt.def.Swimlane;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.bpm.taskmgmt.log.TaskAssignLog;
import ru.runa.bpm.taskmgmt.log.TaskCreateLog;
import ru.runa.bpm.taskmgmt.log.TaskEndLog;
import ru.runa.bpm.taskmgmt.log.TaskLog;
import ru.runa.wf.graph.GraphImage.RenderHits;
import ru.runa.wf.graph.figure.AbstractFigure;
import ru.runa.wf.graph.figure.FigureFactory;
import ru.runa.wf.graph.figure.TransitionFigure;
import ru.runa.wf.graph.model.BendpointModel;
import ru.runa.wf.graph.model.DiagramModel;
import ru.runa.wf.graph.model.NodeModel;
import ru.runa.wf.graph.model.TransitionModel;
import ru.runa.wf.graph.util.DrawProperties;

public class GraphHistoryBuilder {
    private static final DateFormat date_format = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    private static final DateFormat dateFormat = new SimpleDateFormat("H:mm:ss");
    private final FigureFactory factory;
    private final List<Node> definitionNodes;
    private final List<ProcessLog> processLog;
    private final DiagramModel diagramModel;
    private final List<GraphElementPresentation> logElements = new ArrayList<GraphElementPresentation>();
    private int graphWidth = 1000;
    private int glYLayer = 0;

    private final Map<TransitionFigure, RenderHits> transitionFigures = new HashMap<TransitionFigure, RenderHits>();
    private final Map<AbstractFigure, RenderHits> nodeFigures = new HashMap<AbstractFigure, RenderHits>();
    private final Map<Token, Integer> tokenSubTokenCountMap = new HashMap<Token, Integer>();
    private final Map<Token, Integer> tokenMaxWidthMap = new HashMap<Token, Integer>();
    private final Map<Node, LinkedList<Token>> nodeTokensMap = new HashMap<Node, LinkedList<Token>>();

    public GraphHistoryBuilder(List<Node> definitionNodes, List<ProcessLog> processLog, DiagramModel diagramModel) {
        factory = new FigureFactory(false);
        this.definitionNodes = definitionNodes;
        this.processLog = processLog;
        this.diagramModel = diagramModel;
    }

    public void processLog() {
        Iterator<ProcessLog> logIterator = processLog.iterator();
        renderMainToken(graphWidth / 2, 50, logIterator);
    }

    public void renderMainToken(int startPointX, int startPointY, Iterator<ProcessLog> logIterator) {
        int yLayer = startPointY;
        Token rootToken = null;
        AbstractFigure lastFigure = null;

        renderSubToken(true, rootToken, startPointX, 0, yLayer, lastFigure);
    }

    public AbstractFigure renderSubToken(boolean renderRootToken, Token token, int startPointX, int part, int yLayer, AbstractFigure lastFigure) {
        int currentToken = 0;
        int currentSubCount = 0;
        Map<Token, AbstractFigure> tokensFinishFigure = new HashMap<Token, AbstractFigure>();
        Map<NodeModel, AbstractFigure> endTokens = new HashMap<NodeModel, AbstractFigure>();
        Map<TaskInstance, AbstractFigure> taskInstanceNode = new HashMap<TaskInstance, AbstractFigure>();
        TransitionLog lastTransitionLog = null;

        int centerX = startPointX + (part / 2);

        if (renderRootToken) {
            defineSubTokenCount();
            startPointX = 0;
        }

        Iterator<ProcessLog> subIterator = processLog.iterator();

        while (subIterator.hasNext()) {
            ProcessLog log = subIterator.next();

            if ((log instanceof ProcessInstanceCreateLog) && renderRootToken) {
                token = ((ProcessInstanceCreateLog) log).getToken();

                // count X
                List<Token> rootTokens = new LinkedList<Token>();
                rootTokens.add(token);

                graphWidth = widthGraph(rootTokens);

                if (graphWidth < 200) {
                    graphWidth = 200;
                }

                tokenMaxWidthMap.put(token, graphWidth);

                centerX = graphWidth / 2;

                lastFigure = processProcessInstanceCreateLog(lastFigure, centerX, yLayer);
            } else if ((log instanceof ProcessInstanceEndLog) && renderRootToken) {
                processProcessInstanceEndLog((ProcessInstanceEndLog) log, token, lastFigure, centerX, yLayer);
            } else if (log instanceof TaskCreateLog) {

                if (((TaskCreateLog) log).getTaskInstance().getTask().getNode() != null
                        && ((TaskCreateLog) log).getTaskInstance().getToken().equals(token)) {
                    lastFigure = processTaskCreateLog((TaskCreateLog) log, token, lastFigure, taskInstanceNode, centerX, yLayer);

                    yLayer = glYLayer;
                }

            } else if (log instanceof ProcessStateLog) {

                if ((((ProcessStateLog) log).getNode() != null) && log.getToken().equals(token)) {
                    lastFigure = processProcessStateLog((ProcessStateLog) log, token, lastFigure, centerX, yLayer);
                    yLayer = glYLayer;
                }

            } else if (log instanceof TaskEndLog) {
                processTaskEndLog((TaskLog) log, token, taskInstanceNode, yLayer);
            } else if ((log instanceof TokenCreateLog) && ((TokenCreateLog) log).getToken().equals(token)) {
                currentToken++;

                if (currentToken == 1) {
                    yLayer += 20;
                    glYLayer += 20;
                }

                lastFigure = processTokenCreateLog((TokenCreateLog) log, token, lastFigure, tokensFinishFigure, lastTransitionLog, currentToken,
                        centerX, startPointX, yLayer);
            } else if ((log instanceof TokenEndLog) && ((TokenEndLog) log).getToken().equals(token)) {

                currentSubCount++;

                if (tokenSubTokenCountMap.get(((TokenEndLog) log).getToken()).equals(new Integer(currentSubCount))) {
                    glYLayer += 20;
                }

                if (yLayer < glYLayer) {
                    yLayer = glYLayer;
                }

                AbstractFigure returnFigure = processTokenEndLog((TokenEndLog) log, token, lastFigure, tokensFinishFigure, endTokens,
                        currentSubCount, centerX, yLayer);

                if (returnFigure != null) {
                    lastFigure = returnFigure;
                    currentSubCount = 0;
                    endTokens = new HashMap<NodeModel, AbstractFigure>();
                }

            } else if (log instanceof TransitionLog) {
                lastTransitionLog = (TransitionLog) log;
            }
        }

        return lastFigure;
    }

    /**
     * method define count sub tokens for parent token
     */
    private void defineSubTokenCount() {
        Token rootToken = null;

        for (ProcessLog localLog : processLog) {

            if (localLog instanceof ProcessInstanceCreateLog) {
                rootToken = ((ProcessInstanceCreateLog) localLog).getToken();
                tokenSubTokenCountMap.put(rootToken, new Integer(0));
            } else if (localLog instanceof TokenCreateLog) {
                rootToken = ((TokenCreateLog) localLog).getToken();

                Integer count = tokenSubTokenCountMap.get(rootToken);
                count++;
                tokenSubTokenCountMap.put(rootToken, count);
                tokenSubTokenCountMap.put(((TokenCreateLog) localLog).getChild(), new Integer(0));
            }
        }
    }

    /**
     * method define length of state corridors
     * 
     * @param rootTokens
     * @return
     */
    private int widthGraph(List<Token> rootTokens) {
        int[] widths = new int[rootTokens.size()];

        for (int j = 0; j < widths.length; j++) {
            widths[j] = 10;
        }

        int i = 0;

        for (Token childToken : rootTokens) {
            List<Token> childTokens = new LinkedList<Token>();

            Map<Node, LinkedList<Token>> localNodeTokensMap = new HashMap<Node, LinkedList<Token>>();

            TransitionLog lastTransitionLog = null;

            for (ProcessLog log : processLog) {

                if ((log instanceof TaskCreateLog) && (((TaskLog) log).getTaskInstance().getTask().getNode() != null)
                        && childToken.equals(((TaskCreateLog) log).getToken())) {
                    Node node = ((TaskLog) log).getTaskInstance().getTask().getNode();
                    NodeModel nodeModel = diagramModel.getNode(node.getName());

                    if (widths[i] < (nodeModel.getWidth() + 10)) {
                        widths[i] = nodeModel.getWidth() + 10;
                    }
                } else if ((log instanceof ProcessStateLog) && (((ProcessStateLog) log).getNode() != null)
                        && ((ProcessStateLog) log).getToken().equals(childToken)) {
                    Node node = ((ProcessStateLog) log).getNode();

                    NodeModel nodeModel = diagramModel.getNode(node.getName());

                    if (widths[i] < (nodeModel.getWidth() + 10)) {
                        widths[i] = nodeModel.getWidth() + 10;
                    }
                }

                if (log instanceof TransitionLog) {
                    lastTransitionLog = (TransitionLog) log;
                }

                if ((log instanceof TokenCreateLog) && childToken.equals(((TokenCreateLog) log).getToken())) {
                    Token token = ((TokenCreateLog) log).getChild();
                    childTokens.add(token);

                    Node node = lastTransitionLog.getTransition().getTo();

                    if ((node != null) && (localNodeTokensMap.get(node) != null)) {
                        localNodeTokensMap.get(node).add(token);
                    } else if ((node != null) && (localNodeTokensMap.get(node) == null)) {
                        localNodeTokensMap.put(node, new LinkedList<Token>());
                        localNodeTokensMap.get(node).add(token);
                    }

                    if ((node != null) && (nodeTokensMap.get(node) != null)) {
                        nodeTokensMap.get(node).add(token);
                    } else if ((node != null) && (nodeTokensMap.get(node) == null)) {
                        nodeTokensMap.put(node, new LinkedList<Token>());
                        nodeTokensMap.get(node).add(token);
                    }
                }
            }

            if (childTokens.size() > 0) {

                for (Node node : localNodeTokensMap.keySet()) {
                    List<Token> chTokens = localNodeTokensMap.get(node);

                    if (chTokens.size() > 0) {
                        int w = widthGraph(chTokens);

                        if (widths[i] < w) {
                            widths[i] = w;
                        }
                    }
                }
            }

            tokenMaxWidthMap.put(childToken, widths[i]);

            i++;
        }

        int summa = 0;

        for (int ii = 0; ii < widths.length; ii++) {
            summa += widths[ii];
        }

        return summa;
    }

    private Executor getExecutor(String executorId) {
//        try {
//            ExecutorService executorService = ru.runa.af.delegate.DelegateFactory.getInstance().getExecutorService();
//            if ((executorId == null) || executorId.equals(ru.runa.wf.logic.LogicResources.UNASSIGNED_SWIMLANE_VALUE)) {
//                return null;
//            }
//            if (executorId.charAt(0) == 'G') {
//                return executorService.getGroup(subject, Long.parseLong(executorId.substring(1)));
//            } else {
//                return executorService.getActorByCode(subject, Long.parseLong(executorId));
//            }
//        } catch (AuthorizationException e) {
//            return null;
//        } catch (AuthenticationException e) {
//            return null;
//        } catch (ExecutorOutOfDateException e) {
//            return null;
//        }
    	return null; // TODO
    }

    private AbstractFigure processProcessInstanceCreateLog(AbstractFigure lastFigure, int startPointX, int yLayer) {

        for (Node node : definitionNodes) {

            if (NodeType.StartState.equals(node.getNodeType())) {
                AbstractFigure nodeFigure = renderStartState(startPointX, yLayer, node);
                yLayer = glYLayer;
                lastFigure = nodeFigure;
            }
        }

        return lastFigure;
    }

    private void processProcessInstanceEndLog(ProcessInstanceEndLog log, Token rootToken, AbstractFigure lastFigure, int startPointX, int yLayer) {
        glYLayer += 50;

        for (Node node : definitionNodes) {

            if (NodeType.EndState.equals(node.getNodeType())) {
                AbstractFigure nodeFigure = renderEndState(startPointX, yLayer, node);

                if ((lastFigure != null) && !lastFigure.equals(nodeFigure)) {
                    renderTransition(log, lastFigure, nodeFigure, rootToken);
                }
            }
        }
    }

    private AbstractFigure processTaskCreateLog(TaskCreateLog log, Token rootToken, AbstractFigure lastFigure,
            Map<TaskInstance, AbstractFigure> taskInstanceNode, int startPointX, int yLayer) {

        Node node = log.getTaskInstance().getTask().getNode();
        yLayer += 50;

        AbstractFigure nodeFigure = renderState(startPointX, yLayer, node);

        yLayer += nodeFigure.getCoords()[3];
        yLayer += 20;

        if (yLayer > glYLayer) {
            glYLayer = yLayer;
        }

        taskInstanceNode.put(log.getTaskInstance(), nodeFigure);

        if ((lastFigure != null) && !lastFigure.equals(nodeFigure)) {
            renderTransition(log, lastFigure, nodeFigure, rootToken);

            lastFigure = nodeFigure;
        }

        return lastFigure;
    }

    private AbstractFigure processProcessStateLog(ProcessStateLog log, Token rootToken, AbstractFigure lastFigure, int startPointX, int yLayer) {

        Node node = log.getNode();
        yLayer += 50;

        AbstractFigure nodeFigure = renderState(startPointX, yLayer, node);

        yLayer += nodeFigure.getCoords()[3];
        yLayer += 20;

        if (yLayer > glYLayer) {
            glYLayer = yLayer;
        }

        renderProcessStateTooltip(log, nodeFigure);

        if ((lastFigure != null) && !lastFigure.equals(nodeFigure)) {
            renderTransition(log, lastFigure, nodeFigure, rootToken);

            lastFigure = nodeFigure;
        }

        return lastFigure;
    }

    private void processTaskEndLog(TaskLog log, Token rootToken, Map<TaskInstance, AbstractFigure> taskInstanceNode, int yLayer) {

        if (log.getTaskInstance().getTask().getNode() != null && log.getTaskInstance().getToken().equals(rootToken)) {
            AbstractFigure figure = taskInstanceNode.get(log.getTaskInstance());
            if (figure != null) {
                renderTaskTooltip(log, figure, rootToken);
            }
        }
    }

    private AbstractFigure processTokenCreateLog(TokenCreateLog log, Token rootToken, AbstractFigure lastFigure,
            Map<Token, AbstractFigure> tokensFinishFigure, TransitionLog lastTransitionLog, int currentToken, int startPointX,
            int newStartPointXParam, int yLayer) {
        int newStartPointX = newStartPointXParam;
        int part = 0;
        int fullLine = tokenMaxWidthMap.get(rootToken);

        part = tokenMaxWidthMap.get(log.getChild());

        Node node = lastTransitionLog.getTransition().getTo();
        List<Token> chTokens = nodeTokensMap.get(node);

        if ((chTokens.size() == 0) && (newStartPointXParam == 0)) {
            newStartPointX += tokenMaxWidthMap.get(log.getChild());
        }

        for (Token lcToken : chTokens) {

            if (log.getChild().equals(lcToken)) {
                part = tokenMaxWidthMap.get(log.getChild());

                break;
            }

            newStartPointX += tokenMaxWidthMap.get(lcToken);
        }

        AbstractFigure joinNodeFigure = renderFork(startPointX, yLayer, fullLine);

        if (yLayer > glYLayer) {
            glYLayer = yLayer;
        }

        if ((lastFigure != null) && !lastFigure.equals(joinNodeFigure)) {
            renderTransition(log, lastFigure, joinNodeFigure, rootToken);

            if (tokenSubTokenCountMap.get(rootToken) == currentToken) {
                lastFigure = joinNodeFigure;
            }
        }

        AbstractFigure finishFigure = renderSubToken(false, log.getChild(), newStartPointX, part, yLayer, joinNodeFigure);
        tokensFinishFigure.put(log.getChild(), finishFigure);

        return lastFigure;
    }

    private AbstractFigure processTokenEndLog(TokenEndLog log, Token rootToken, AbstractFigure lastFigure,
            Map<Token, AbstractFigure> tokensFinishFigure, Map<NodeModel, AbstractFigure> endTokens, int currentSubCount, int startPointX, int yLayer) {
        int fullLine = tokenMaxWidthMap.get(rootToken);

        NodeModel joinNodeModel = new NodeModel();
        joinNodeModel.setType(NodeModel.FORK_JOIN);
        joinNodeModel.setName("join " + log.getChild().getName());
        joinNodeModel.setWidth(fullLine);
        joinNodeModel.setHeight(4);
        joinNodeModel.setX(startPointX - (fullLine / 2));
        joinNodeModel.setY(yLayer);

        Node node = null;
        TransitionLog localTransitionLog = null;

        for (ProcessLog llog : processLog) {

            if ((llog instanceof TokenCreateLog) && ((TokenCreateLog) llog).getChild().equals(log.getChild())) {
                node = localTransitionLog.getTransition().getTo();

                break;
            } else if (llog instanceof TransitionLog) {
                localTransitionLog = (TransitionLog) llog;
            }
        }

        if (nodeTokensMap.get(node).size() == currentSubCount) {

            // find max token
            endTokens.put(joinNodeModel, tokensFinishFigure.get(log.getChild()));

            int y = 0;

            for (NodeModel nodeModel : endTokens.keySet()) {

                if (nodeModel.getY() > y) {
                    y = nodeModel.getY();
                }
            }

            y += 20;

            if (glYLayer < y) {
                glYLayer = y;
            }

            int currToken = 1;

            for (NodeModel nodeModel : endTokens.keySet()) {
                nodeModel.setY(y);

                AbstractFigure joinNodeFigure = renderJoinTransitions(log, lastFigure, nodeModel, node, currToken, tokensFinishFigure, endTokens);

                lastFigure = joinNodeFigure;
                currToken++;
            }

            if (yLayer > glYLayer) {
                glYLayer = yLayer;
            }

            glYLayer += 20;

            return lastFigure;
        } else {
            endTokens.put(joinNodeModel, tokensFinishFigure.get(log.getChild()));

            return null;
        }
    }

    private AbstractFigure renderStartState(int startPointX, int yLayer, Node node) {
        NodeModel nodeModel = new NodeModel();
        nodeModel.setType(NodeModel.START_STATE);
        nodeModel.setName("start state");
        nodeModel.setX(startPointX - 20);
        nodeModel.setY(yLayer);
        nodeModel.setWidth(50);
        nodeModel.setHeight(10);
        nodeModel.setActionsCount(GraphHelper.getNodeActionsCount(node));

        yLayer += nodeModel.getHeight();
        yLayer += 20;

        if (yLayer > glYLayer) {
            glYLayer = yLayer;
        }

        AbstractFigure nodeFigure = factory.createFigure(nodeModel);

        nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));

        return nodeFigure;
    }

    private AbstractFigure renderEndState(int startPointX, int yLayer, Node node) {
        NodeModel nodeModel = new NodeModel();
        nodeModel.setType(NodeModel.END_STATE);
        nodeModel.setName("end state");
        nodeModel.setX(startPointX - 20);
        nodeModel.setY(glYLayer);
        nodeModel.setWidth(50);
        nodeModel.setHeight(10);
        nodeModel.setActionsCount(GraphHelper.getNodeActionsCount(node));

        AbstractFigure nodeFigure = factory.createFigure(nodeModel);

        nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));

        return nodeFigure;
    }

    private AbstractFigure renderState(int startPointX, int yLayer, Node node) {
        NodeModel nodeModel = diagramModel.getNode(node.getName());
        GraphHelper.setTypeToNode(node, nodeModel);
        GraphHelper.setSwimlaneToNode(node, nodeModel);
        nodeModel.setX(startPointX - (nodeModel.getWidth() / 2));
        nodeModel.setY(yLayer + 20);
        nodeModel.setActionsCount(GraphHelper.getNodeActionsCount(node));

        AbstractFigure nodeFigure = factory.createFigure(nodeModel);

        nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));

        return nodeFigure;
    }

    public AbstractFigure renderFork(int startPointX, int yLayer, int fullLine) {
        yLayer += 40;
        // glYLayer += 40;

        NodeModel joinNodeModel = new NodeModel();
        joinNodeModel.setType(NodeModel.FORK_JOIN);
        joinNodeModel.setWidth(fullLine);
        joinNodeModel.setHeight(4);
        joinNodeModel.setX(startPointX - (fullLine / 2));
        joinNodeModel.setY(yLayer);

        AbstractFigure joinNodeFigure = factory.createFigure(joinNodeModel);

        nodeFigures.put(joinNodeFigure, new RenderHits(DrawProperties.getBaseColor()));

        if (yLayer > glYLayer) {
            glYLayer = yLayer;
        }

        return joinNodeFigure;
    }

    private void renderTransition(ProcessLog log, AbstractFigure lastFigure, AbstractFigure nodeFigure, Token rootToken) {
        TransitionModel transitionModel = new TransitionModel();
        TransitionLog transitionLog = null;
        Iterator<ProcessLog> transIterator = processLog.iterator();

        while (transIterator.hasNext()) {
            ProcessLog trLog = transIterator.next();

            if ((trLog instanceof TransitionLog) && ((TransitionLog) trLog).getToken().equals(rootToken)) {
                transitionLog = ((TransitionLog) trLog);
            }

            if (trLog.equals(log)) {
                break;
            }
        }

        if (transitionLog != null) {
            transitionModel.setName(date_format.format(transitionLog.getDate()));
            transitionModel.setActionsCount(GraphHelper.getTransitionActionsCount(transitionLog.getTransition()));

            if (DrawProperties.TIMEOUT_TRANSITION.equals(transitionLog.getTransition().getName())) {
                BendpointModel bendpointModel = new BendpointModel();
                bendpointModel.setX(nodeFigure.getCoords()[0] + (nodeFigure.getCoords()[2] / 2) + 5);
                bendpointModel.setY(lastFigure.getCoords()[1] + lastFigure.getCoords()[3] + 30);
                transitionModel.setName(DrawProperties.TIMEOUT_TRANSITION);
                transitionModel.addBendpoint(bendpointModel);
            }
        }

        TransitionFigure transitionFigure = factory.createTransitionFigure(transitionModel, lastFigure, nodeFigure);

        if (DrawProperties.TIMEOUT_TRANSITION.equals(transitionLog.getTransition().getName())) {
            transitionFigure.setTimerInfo(date_format.format(transitionLog.getDate()));
        }

        // check if exist no add
        boolean found = false;

        for (TransitionFigure existTransitionFigure : transitionFigures.keySet()) {

            if (existTransitionFigure.getFigureFrom().equals(transitionFigure.getFigureFrom())
                    && existTransitionFigure.getFigureTo().equals(transitionFigure.getFigureTo())) {
                found = true;

                break;
            }
        }

        if (!found) {
            transitionFigures.put(transitionFigure, new RenderHits(DrawProperties.getTransitionColor()));
        }
    }

    private AbstractFigure renderJoinTransitions(ProcessLog log, AbstractFigure lastFigure, NodeModel nodeModel, Node node, int currToken,
            Map<Token, AbstractFigure> tokensFinishFigure, Map<NodeModel, AbstractFigure> endTokens) {
        AbstractFigure joinNodeFigure = factory.createFigure(nodeModel);
        nodeFigures.put(joinNodeFigure, new RenderHits(DrawProperties.getBaseColor()));

        TransitionModel transitionModel = new TransitionModel();
        TransitionLog transitionLog = null;

        AbstractFigure figure = endTokens.get(nodeModel);
        Token childToken = null;

        for (Token finishToken : tokensFinishFigure.keySet()) {

            if (tokensFinishFigure.get(finishToken).equals(figure)) {
                childToken = finishToken;

                break;
            }
        }

        Iterator<ProcessLog> transIterator = processLog.iterator();

        while (transIterator.hasNext()) {
            ProcessLog trLog = transIterator.next();

            if ((trLog instanceof TransitionLog) && ((TransitionLog) trLog).getToken().equals(childToken)) {
                transitionLog = ((TransitionLog) trLog);
            }

            if (trLog.equals(log)) {
                break;
            }
        }

        if (transitionLog != null) {
            transitionModel.setName(date_format.format(transitionLog.getDate()));
            transitionModel.setActionsCount(GraphHelper.getTransitionActionsCount(transitionLog.getTransition()));

            if (DrawProperties.TIMEOUT_TRANSITION.equals(transitionLog.getTransition().getName())) {
                BendpointModel bendpointModel = new BendpointModel();
                bendpointModel.setX(joinNodeFigure.getCoords()[0] + (joinNodeFigure.getCoords()[2] / 2) + 5);
                bendpointModel.setY(lastFigure.getCoords()[1] + lastFigure.getCoords()[3] + 30);
                transitionModel.setName(DrawProperties.TIMEOUT_TRANSITION);
                transitionModel.addBendpoint(bendpointModel);
            }
        }

        if ((endTokens.get(nodeModel).getType() == NodeModel.FORK_JOIN) && (joinNodeFigure.getType() == NodeModel.FORK_JOIN)) {
            BendpointModel bendpointModel = new BendpointModel();

            int newStartPointX = 0;
            List<Token> chTokens = nodeTokensMap.get(node);
            int c = 1;

            for (Token lcToken : chTokens) {

                if (c == currToken) {
                    break;
                }

                newStartPointX += tokenMaxWidthMap.get(lcToken);
                c++;
            }

            if ((newStartPointX == 0)
                    || (transitionLog.getTransition().getFrom().getNodeType().equals(NodeType.Join) && transitionLog.getTransition().getTo()
                            .getNodeType().equals(NodeType.Join))) {
                newStartPointX = 0;

                for (Token lcToken : chTokens) {

                    if (lcToken.equals(childToken)) {
                        break;
                    }

                    newStartPointX += tokenMaxWidthMap.get(lcToken);
                    c++;
                }

                newStartPointX = newStartPointX + (tokenMaxWidthMap.get(childToken) / 2);
            }

            bendpointModel.setX(newStartPointX);
            bendpointModel.setY(glYLayer);
            transitionModel.addBendpoint(bendpointModel);
        }

        TransitionFigure transitionFigure = factory.createTransitionFigure(transitionModel, endTokens.get(nodeModel), joinNodeFigure);

        if (DrawProperties.TIMEOUT_TRANSITION.equals(transitionLog.getTransition().getName())) {
            transitionFigure.setTimerInfo(date_format.format(transitionLog.getDate()));
        }

        // check if exist no add
        boolean found = false;

        for (TransitionFigure existTransitionFigure : transitionFigures.keySet()) {

            if (existTransitionFigure.getFigureFrom().equals(transitionFigure.getFigureFrom())
                    && existTransitionFigure.getFigureTo().equals(transitionFigure.getFigureTo())) {
                found = true;

                break;
            }
        }

        if (!found) {
            transitionFigures.put(transitionFigure, new RenderHits(DrawProperties.getTransitionColor()));
        }

        return joinNodeFigure;
    }

    private void renderTaskTooltip(TaskLog log, AbstractFigure figure, Token rootToken) {
        String actor = log.getTaskInstance().getAssignedActorId();
        int[] coord = new int[4];
        coord[0] = figure.getCoords()[0];
        coord[1] = figure.getCoords()[1];
        coord[2] = figure.getCoords()[2] + figure.getCoords()[0];
        coord[3] = figure.getCoords()[3] + figure.getCoords()[1];

        TaskAssignLog prev = null;

        for (ProcessLog tempLog : processLog) {
            if ((tempLog instanceof TaskAssignLog) && ((TaskLog) tempLog).getTaskInstance() != null
                    && ((TaskLog) tempLog).getTaskInstance().getToken().equals(log.getTaskInstance().getToken())) {
                prev = (TaskAssignLog) tempLog;
            } else if (tempLog.equals(log)) {
                break;
            }
        }

        if ((prev != null) && prev.getToken().equals(log.getToken())) {

            if ((prev.getTaskOldActorId() != null) && (prev.getTaskNewActorId() != null)
                    && !prev.getTaskOldActorId().equals(prev.getTaskNewActorId()) && prev.getTaskNewActorId().equals(actor)) {
                actor = prev.getTaskOldActorId();
            }
        }

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(log.getTaskInstance().getCreateDate());

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(log.getTaskInstance().getEndDate());

        Long period = endCal.getTimeInMillis() - startCal.getTimeInMillis();

        Calendar periodCal = Calendar.getInstance();
        periodCal.setTimeInMillis(period);

        String date = getPeriodDateString(startCal, endCal);

        Executor executor = getExecutor(actor);

        List<String> logs = new ArrayList<String>();

        if (executor != null) {

            if ((executor instanceof Actor) && (((Actor) executor).getFullName() != null)) {
                logs.add("Full Name is " + ((Actor) executor).getFullName() + ".");
            }

            logs.add("Login is " + executor.getName() + ".");
        }

        logs.add("Time period is " + date + ".");

        String[] str = logs.toArray(new String[0]);
        Swimlane swimlane = null;
        if (log.getTaskInstance().getSwimlaneInstance() != null) {
            swimlane = log.getTaskInstance().getSwimlaneInstance().getSwimlane();
        }
        logElements.add(new TaskGraphElementPresentation(figure.getName(), coord, swimlane, diagramModel.getNode(figure.getName()).isMinimizedView(),
                str));
    }

    private void renderProcessStateTooltip(ProcessStateLog log, AbstractFigure figure) {

        if ((log.getEnter() == null) || (log.getLeave() == null)) {
            return;
        }

        int[] coord = new int[4];
        coord[0] = figure.getCoords()[0];
        coord[1] = figure.getCoords()[1];
        coord[2] = figure.getCoords()[2] + figure.getCoords()[0];
        coord[3] = figure.getCoords()[3] + figure.getCoords()[1];

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(log.getEnter());
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(log.getLeave());
        Long period = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        Calendar periodCal = Calendar.getInstance();
        periodCal.setTimeInMillis(period);
        String date = getPeriodDateString(startCal, endCal);
        if (log.getNode().getNodeType().equals(NodeType.MultiInstance)) {
            logElements.add(new MultiinstanceGraphElementPresentation(figure.getName(), log.getSubProcessInstance().getProcessDefinition().getName(),
                    coord, new String[] { "Time period is " + date }));
        } else {
            logElements.add(new SubprocessGraphElementPresentation(figure.getName(), log.getSubProcessInstance().getProcessDefinition().getName(),
                    coord, new String[] { "Time period is " + date }));
        }
    }

    private String getPeriodDateString(Calendar startCal, Calendar endCal) {
        long period = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        Calendar periodCal = Calendar.getInstance();
        periodCal.setTimeInMillis(period);
        periodCal.setTimeInMillis(period - periodCal.getTimeZone().getOffset(period));

        String result = "";
        long days = period / (24 * 60 * 60 * 1000);

        if (days > 0) {
            result = (days == 1) ? "1 day " : (String.valueOf(days) + " days ");
        }

        result = result + dateFormat.format(periodCal.getTime());

        return result;
    }

    public Map<TransitionFigure, RenderHits> getTransitionFigures() {
        return transitionFigures;
    }

    public Map<AbstractFigure, RenderHits> getNodeFigures() {
        return nodeFigures;
    }

    public List<GraphElementPresentation> getLogElements() {
        return logElements;
    }

    public int getGraphWidth() {
        return graphWidth;
    }

    public int getGlYLayer() {
        return glYLayer;
    }
}
