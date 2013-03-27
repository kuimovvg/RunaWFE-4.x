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
package ru.runa.wfe.graph.image;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.NodeLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.SubprocessEndLog;
import ru.runa.wfe.audit.SubprocessStartLog;
import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.TaskLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.graph.image.GraphImage.RenderHits;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.image.figure.TransitionFigureBase;
import ru.runa.wfe.graph.image.figure.uml.UMLFigureFactory;
import ru.runa.wfe.graph.image.model.BendpointModel;
import ru.runa.wfe.graph.image.model.DiagramModel;
import ru.runa.wfe.graph.image.model.NodeModel;
import ru.runa.wfe.graph.image.model.TransitionModel;
import ru.runa.wfe.graph.image.util.DrawProperties;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.TaskGraphElementPresentation;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Modified on 26.02.2009 by gavrusev_sergei
 */
public class GraphHistoryBuilder {
	private static final DateFormat transitionDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private static final DateFormat nodeDateFormat = new SimpleDateFormat("H:mm:ss");
	private static final int heightBetweenNode = 40;
	private static final int heightForkJoinNode = 4;
    private final List<Executor> executors;
	private final ProcessDefinition processDefinition;
    private final Map<String, NodeModel> allNodes = Maps.newHashMap();
    private final Map<String, AbstractFigure> allNodeFigures = Maps.newHashMap();
    private final Map<TransitionFigureBase, RenderHits> transitionFigureBases = Maps.newHashMap();
    private final Map<AbstractFigure, RenderHits> nodeFigures = Maps.newHashMap();
    
    private final List<ProcessLog> processLogs = Lists.newArrayList();
    private final List<TransitionLog> transitionLogs = Lists.newArrayList();
    private final List<NodeLog> nodeLogs = Lists.newArrayList();
    private final List<TaskLog> taskLogs = Lists.newArrayList();
    private final DiagramModel diagramModel;
    private final AbstractFigureFactory factory;
    
    private final List<GraphElementPresentation> logElements = new ArrayList<GraphElementPresentation>();

    public GraphHistoryBuilder(List<Executor> executors, WfTaskFactory taskObjectFactory, ProcessDefinition processDefinition, List<ProcessLog> processLogs) {
        this.executors = executors;
    	this.processDefinition = processDefinition;
        
        for(ProcessLog processLog : processLogs) {
        	if(processLog instanceof TransitionLog) {
        		transitionLogs.add((TransitionLog)processLog);
        	} else if(processLog instanceof NodeLog) {
        		nodeLogs.add((NodeLog)processLog);
        	} else if(processLog instanceof TaskLog) {
        		taskLogs.add((TaskLog)processLog);
        	}
        }
        
        this.processLogs.addAll(processLogs);        
        this.diagramModel = DiagramModel.load(processDefinition);
        this.factory = new UMLFigureFactory();
    }

    public byte[] createDiagram(Process process, List<Transition> passedTransitions) throws Exception {
    	boolean tempUseEdgingMode = DrawProperties.useEdgingOnly();
        DrawProperties.setUseEdgingMode(false);
        
        String startNodeId = null;
        Map<String, Integer> widthTokens = new HashMap<String, Integer>();
        Map<String, Integer> heightTokens = new HashMap<String, Integer>();
        Map<String, String> parentNodeInTokenForNodeMap = new HashMap<String, String>();
        Map<String, Long> nodeRepetitionCount = new HashMap<String, Long>();
        
        for (NodeLog log : nodeLogs) {
			if(log instanceof NodeLeaveLog && NodeType.StartState.toString().equals(log.getNodeType())) {
				startNodeId = log.getNodeId();
				
				calculateWidthTokensInGraph(startNodeId, log.getId(), widthTokens, parentNodeInTokenForNodeMap);
				break;
			}
		}
        
        calculateCoordinatesForNodes(widthTokens, heightTokens, parentNodeInTokenForNodeMap);        
        
        //render transition
        for (NodeLog log : nodeLogs) {
        	if(log instanceof NodeEnterLog || log instanceof NodeLeaveLog) {
        		String nodeId = log.getNodeId();
        		  		
        		for (Node node : processDefinition.getNodes()) {
        			if(node.getNodeId().equals(nodeId)) {
        				if(isNodePresentInGraph(log)) {
        					String correctNodeId = getNodeIdIncludeRepetition(nodeId, nodeRepetitionCount);
        	                NodeModel nodeModel = allNodes.get(correctNodeId);
            	            Preconditions.checkNotNull(nodeModel, "Node model not found by id " + nodeId);
        	                
        					AbstractFigure nodeFigure = allNodeFigures.get(correctNodeId);
            	            nodeFigures.put(nodeFigure, new RenderHits(DrawProperties.getBaseColor()));
        				}
        				
        				if(log instanceof NodeLeaveLog && !(log instanceof SubprocessEndLog && NodeType.MultiSubprocess.toString().equals(log.getNodeType()))) {
        					Long duplicateCount = nodeRepetitionCount.get(nodeId);
        					String correctNodeId = nodeId + ":" + duplicateCount;
        					NodeModel nodeModel = allNodes.get(correctNodeId);
            	            Preconditions.checkNotNull(nodeModel, "Node model not found by id " + nodeId);
            	            
        					AbstractFigure nodeFigure = allNodeFigures.get(correctNodeId);
            	            for (Transition transition : node.getLeavingTransitions()) {            	            	
            	            	if(nodeModel.getType() == NodeType.Decision) {
            	            		Transition desicionTransition = findTransitionDesicionByLog(log, nodeId);
            	            		if(desicionTransition != null && !desicionTransition.equals(transition)) {
            	                		continue;
            	                	}
            	            	}
            	            	
            	                TransitionModel transitionModel = nodeModel.getTransition(transition.getName());
            	                transitionModel.setName(transitionDateFormat.format(findNextTransitionLog(log, nodeId).getDate()));
            	                if (diagramModel.isShowActions()) {
            	                    transitionModel.setActionsCount(GraphImageHelper.getTransitionActionsCount(transition));
            	                }
            	                
            	                Long duplicateTransitionCount = nodeRepetitionCount.get(transition.getTo().getNodeId());
            	                if(duplicateTransitionCount == null) {
            	                	duplicateTransitionCount = new Long(0);
            	                }
            	                duplicateTransitionCount = duplicateTransitionCount + 1;
            	                
            	                AbstractFigure figureTo = allNodeFigures.get(transition.getTo().getNodeId() + ":" + duplicateTransitionCount);
            	                if(figureTo != null) {
            	                	if(nodeModel.getType() == NodeType.Fork) {
            	                		BendpointModel bendpointModel = new BendpointModel();
            	                		bendpointModel.setX(figureTo.getCoords()[0] + figureTo.getCoords()[2]/2);
            	                        bendpointModel.setY(nodeModel.getY() + nodeModel.getHeight()/2);
            	                        transitionModel.addBendpoint(bendpointModel);
            	                	} else {
            	                		transitionModel.getBendpoints().clear();
            	                	}
            	                	if(figureTo.getType() == NodeType.Join) {
            	                		BendpointModel bendpointModel = new BendpointModel();
            	                		bendpointModel.setX(nodeModel.getX() + nodeModel.getWidth()/2);
            	                        bendpointModel.setY(figureTo.getCoords()[1]);
            	                        transitionModel.addBendpoint(bendpointModel);
            	                	}
            	                	TransitionFigureBase transitionFigureBase = factory.createTransitionFigure(transitionModel, nodeFigure, figureTo);
                	                transitionFigureBase.init(transitionModel, nodeFigure, figureTo);
                	                if (Transition.TIMEOUT_TRANSITION_NAME.equals(transitionModel.getName())) {
                	                    transitionFigureBase.setTimerInfo(GraphImageHelper.getTimerInfo(node));
                	                }
                	                nodeFigure.addTransition(transition.getName(), transitionFigureBase);
                	                transitionFigureBases.put(transitionFigureBase, new RenderHits(DrawProperties.getTransitionColor()));
            	                }
            	            }
        				}
        				
        				break;
        			}
        		}        		
        	}            
        }
        
        //find max height
        int height = 0;
        for(String nodeId : heightTokens.keySet()) {
        	if(heightTokens.get(nodeId) != null && heightTokens.get(nodeId) > height) {
        		height = heightTokens.get(nodeId);
        	}
        }
        
        diagramModel.setHeight(height + 100);
        diagramModel.setWidth(widthTokens.get(startNodeId) + 20);
        GraphImage graphImage = new GraphImage(null, diagramModel, transitionFigureBases, nodeFigures);
        byte[] graphImageByteArray = graphImage.getImageBytes();
        DrawProperties.setUseEdgingMode(tempUseEdgingMode);
        return graphImageByteArray;
    }
    
    /**
     * Method calculates the width of tokens. Token is line that contains nodes and transitions. 
     * Fork node creates subtokens. Join node finishes all subtokens node and continue the main token.
     * 
     * @param startTokenNodeId - the first node in token.
     * @param logId
     * @param tokenWidth - object contains start node in token and the width of token.
     * @param parentNodeInTokenForNodeMap - object populate parent node id for all nodes.
     * @return last nodeId in graph
     */
    private String calculateWidthTokensInGraph(String startTokenNodeId, Long logId,  Map<String, Integer> tokenWidth, Map<String, String> parentNodeInTokenForNodeMap) {
    	String nodeId = startTokenNodeId;
    	
    	while(nodeId != null) {
    		parentNodeInTokenForNodeMap.put(nodeId, startTokenNodeId);
    		NodeModel nodeModel = diagramModel.getNodeNotNull(nodeId);
    		initNodeModel(nodeModel, nodeId);   		
    		
        	setCurrentTokenWidth(tokenWidth, startTokenNodeId, nodeModel.getWidth());
        	
        	if(nodeModel.getType() == NodeType.Fork) {
    			List<String> nextNodeIds = getNextNodesInGraph(logId, nodeId);
    			
    			//calculate the width of token for fork tokens
    			int resultWidth = 0;
    			String joinNodeId = null;
    			for(String nextNodeId : nextNodeIds) {
    				logId = getNodeLeaveLog(logId, nextNodeId);
    				if(logId == null) {
    					continue;
    				}    				
    				
    				String retVal = calculateWidthTokensInGraph(nextNodeId, logId, tokenWidth, parentNodeInTokenForNodeMap);
    				resultWidth += tokenWidth.get(nextNodeId);
    				if(retVal != null) {
    					joinNodeId = retVal;
    				}
    			}

    			setCurrentTokenWidth(tokenWidth, startTokenNodeId, resultWidth);
    			
    			if(joinNodeId == null) {
    				return nodeId;
    			} else {
    				nodeId = joinNodeId;
    			}
    		} else if(nodeModel.getType() == NodeType.Join) {
    			return nodeId;
    		}
        	
        	//get next node
        	logId = getNodeLeaveLog(logId, nodeId);
			if(logId == null) {
				return nodeId;
			}
			
			List<String> nextNodeIds = getNextNodesInGraph(logId, nodeId);
			
			nodeId = nextNodeIds != null && nextNodeIds.size() > 0 ? nextNodeIds.get(0) : null;
    	}
    	
    	return nodeId;
    }
    
    private void initNodeModel(NodeModel nodeModel, String nodeId) {
    	for (Node node : processDefinition.getNodes()) {
			if(node.getNodeId().equals(nodeId)) {
				GraphImageHelper.initNodeModel(node, nodeModel);
                break;
			}
		}
    }
    
    private void setCurrentTokenWidth(Map<String, Integer> tokenWidth, String nodeId, int value) {
    	Integer width = tokenWidth.get(nodeId);
    	if(width == null || (width != null && width.intValue() < (value + 10))) {
    		tokenWidth.put(nodeId, value + 10);
    	}
    }
    
    private Long getNodeLeaveLog(Long currentLogId, String nodeId) {
    	for(ProcessLog log : nodeLogs) {
			if(log.getId() > currentLogId && log instanceof NodeLeaveLog) {
				if(((NodeLog)log).getNodeId().equals(nodeId)) {
					return log.getId();
				}
			}
		}
    	
    	return currentLogId;
    }
    
    private List<String> getNextNodesInGraph(Long currentLogId, String nodeId) {
    	Set<String> returnNodes = new HashSet<String>();

    	for (TransitionLog log : transitionLogs) {
			if(log.getId() > currentLogId) {
				TransitionLog transitionLog = (TransitionLog)log;
				if(transitionLog.getFromNodeId() != null && transitionLog.getToNodeId() != null && nodeId.equals(transitionLog.getFromNodeId())) {
					returnNodes.add(transitionLog.getToNodeId());
				}
			}
		}
    	
    	return new ArrayList<String>(returnNodes);
    }
    
    private boolean isNodePresentInGraph(NodeLog log) {
    	return !((log instanceof SubprocessStartLog || log instanceof SubprocessEndLog) && NodeType.MultiSubprocess.toString().equals(log.getNodeType())) && ((log instanceof NodeEnterLog && !NodeType.Join.toString().equals(log.getNodeType())) || 
    			(log instanceof NodeLeaveLog && (NodeType.StartState.toString().equals(log.getNodeType()) ||
    			NodeType.Join.toString().equals(log.getNodeType()))));
    }
    
    private String getNodeIdIncludeRepetition(String nodeId, Map<String, Long> nodeRepetitionCount) {
    	Long duplicateCount = nodeRepetitionCount.get(nodeId);
        if(duplicateCount == null) {
        	duplicateCount = new Long(0);        	
        }
        
        duplicateCount = duplicateCount + 1; 
        nodeRepetitionCount.put(nodeId, duplicateCount);
        return nodeId + ":" + duplicateCount;
    }
    
    /**
     * Method calculates X and Y coordinates for figures which present nodes in the graph. 
     * @param widthTokens - object contains start node in token and the width of token.
     * @param heightTokens - object contains start node in token and the height of token
     * @param rootNodeForNodeMap  - object contains parent node id for all nodes.
     */
    private void calculateCoordinatesForNodes(Map<String, Integer> widthTokens, Map<String, Integer> heightTokens, Map<String, String> rootNodeForNodeMap) {
    	int startY = 10;
        Map<String, List<String>> forkNodes = new HashMap<String, List<String>>();        
        Map<String, Long> nodeRepetitionCount = new HashMap<String, Long>();
        
        for(NodeLog log : nodeLogs) {
        	if(isNodePresentInGraph(log)) {
        		String nodeId = log.getNodeId();
        		String rootNodeId = rootNodeForNodeMap.get(nodeId);
        		Integer height = heightTokens.get(rootNodeId);
        		int x = 0;
            	if(height == null) {
            		height = startY;
            		
            		for(String forkRootNodeId : forkNodes.keySet()) {
        				List<String> nodes = forkNodes.get(forkRootNodeId);
        				if(nodes != null && nodes.contains(rootNodeId)) {        					
        					height = heightTokens.get(forkRootNodeId);
        				}
        			}
            	}            	
        		
        		for (Node node : processDefinition.getNodes()) {
        			if(node.getNodeId().equals(nodeId)) {
        				NodeModel nodeModel = diagramModel.getNodeNotNull(node.getNodeId());
                        if (diagramModel.isShowActions()) {
                            nodeModel.setActionsCount(GraphImageHelper.getNodeActionsCount(node));
                        }
                        GraphImageHelper.initNodeModel(node, nodeModel);                        
                        
            			Integer width = widthTokens.get(rootNodeId);

            			x = addedLeftTokenWidthIfExist(width/2, forkNodes, widthTokens, rootNodeId);
                			
                		if(NodeType.Fork.toString().equals(log.getNodeType())) {
                			List<String> nodes = getNextNodesInGraph(log.getId(), nodeId);
                			forkNodes.put(rootNodeId, nodes);
                			
                			nodeModel.setWidth(width);
                			nodeModel.setHeight(heightForkJoinNode);
                		}
                		
                		if(NodeType.Join.toString().equals(log.getNodeType())) {
                			for(String forkRootNodeId : forkNodes.keySet()) {
                				List<String> nodes = forkNodes.get(forkRootNodeId);
                				if(nodes != null && nodes.contains(rootNodeId)) {
                					x = widthTokens.get(forkRootNodeId)/2;

                					nodeModel.setWidth(widthTokens.get(forkRootNodeId));
                					nodeModel.setHeight(heightForkJoinNode);
                				}
                			}
                			
                			height = updateAllRootTokenHeight(height, nodeModel, forkNodes, heightTokens, rootNodeId);
                		}                		
                		
                        nodeModel.setY(height);
                        nodeModel.setX(x - nodeModel.getWidth() / 2);
                        
                        if(!NodeType.Join.toString().equals(log.getNodeType())) {
                        	if(NodeType.Subprocess.toString().equals(log.getNodeType())) {
                        		if(isLastSubprocessLog(log)) {
                            		height += (nodeModel.getHeight() + heightBetweenNode);
                                	heightTokens.put(rootNodeId, height);
                            	}
                        	} else {
                        		height += (nodeModel.getHeight() + heightBetweenNode);
                            	heightTokens.put(rootNodeId, height);
                        	}                   	
                        }
                        
                        String correctNodeId = getNodeIdIncludeRepetition(nodeId, nodeRepetitionCount);
                        allNodes.put(correctNodeId, nodeModel);
                        AbstractFigure nodeFigure = factory.createFigure(nodeModel);
                        allNodeFigures.put(correctNodeId, nodeFigure);
                        if(log instanceof NodeEnterLog) {
                        	addedTooltipOnGraph(node, nodeFigure, nodeModel, (NodeEnterLog) log);
                        }
                        
                        break;
        			}
        		}        		
        	}            
        }
    }
    
    private int addedLeftTokenWidthIfExist(int x, Map<String, List<String>> forkNodes, Map<String, Integer> tokenWidths, String rootNodeId) {
		for(String forkRootNodeId : forkNodes.keySet()) {
			List<String> nodes = forkNodes.get(forkRootNodeId);
			if(nodes != null && nodes.contains(rootNodeId)) {
				int leftTokenWidth = 0;
				for(String forkNode : nodes) {
					if(forkNode.equals(rootNodeId)) {
						break;            							
					} 
					
					leftTokenWidth += tokenWidths.get(forkNode);
				}
				
				x += leftTokenWidth;
			}
		}
		
		return x;
    }
    
    private int updateAllRootTokenHeight(int height, NodeModel nodeModel, Map<String, List<String>> forkNodes, Map<String, Integer> tokenHieght, String rootNodeId) {
    	for(String tempForkRootNodeId : forkNodes.keySet()) {
			List<String> nodes = forkNodes.get(tempForkRootNodeId);
			if(nodes != null && nodes.contains(rootNodeId)) {
				for(String forkNode : nodes) {
					if(tokenHieght.get(forkNode) > tokenHieght.get(tempForkRootNodeId)) {
						height = tokenHieght.get(forkNode);
						tokenHieght.put(tempForkRootNodeId, height + nodeModel.getHeight() + heightBetweenNode);
					}
				}		        					
			}
		}
    	
    	return height;
    }
    
    private Transition findTransitionDesicionByLog(NodeLog log, String nodeId) {
    	Transition desicionTransition = null;
		for(TransitionLog tempLog : transitionLogs) {
			if(tempLog.getId() > log.getId() && tempLog.getFromNodeId().equals(nodeId) ) {
				desicionTransition = tempLog.getTransition(processDefinition);
				break;
			}
		}
		
		return desicionTransition;
    }
    
    private TransitionLog findNextTransitionLog(NodeLog log, String nodeId) {
		for(TransitionLog tempLog : transitionLogs) {
			if(tempLog.getId() > log.getId() && tempLog.getFromNodeId().equals(nodeId) ) {
				return tempLog;
			}
		}
		
		return null;
    }
    
    private boolean isLastSubprocessLog(NodeLog log) {
    	for(ProcessLog processLog : processLogs) {
    		if(processLog.getId() > log.getId()) {
    			if(processLog instanceof SubprocessStartLog) {
    				return false;
    			} else {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    private void addedTooltipOnGraph(Node node, AbstractFigure figure, NodeModel nodeModel, NodeEnterLog nodeEnterlog) {
    	//find node leave log and taskEnterLog
    	NodeLeaveLog nodeLeaveLog = null;
    	for(NodeLog nodeLog : nodeLogs) {
    		if(nodeLog.getId() > nodeEnterlog.getId() && nodeLog instanceof NodeLeaveLog && nodeEnterlog.getNodeId().equals(((NodeLeaveLog)nodeLog).getNodeId())) {
    			nodeLeaveLog = (NodeLeaveLog)nodeLog;
    			break;
    		}
    	}
    	
    	if(nodeLeaveLog == null) {
    		return;
    	}
    	
    	GraphElementPresentation presentation;
        switch (nodeModel.getType()) {
        case Subprocess:
            presentation = new SubprocessGraphElementPresentation();
            ((SubprocessGraphElementPresentation)presentation).setReadPermission(true);
            for(NodeLog nodeLog:  nodeLogs) {
            	if(nodeLog instanceof SubprocessStartLog && nodeEnterlog.getNodeId().equals(((SubprocessStartLog)nodeLog).getNodeId())) {
            		((SubprocessGraphElementPresentation)presentation).setSubprocessId(((SubprocessStartLog)nodeLog).getSubprocessId());
            		break;
            	}
            }
            
            break;
        case MultiSubprocess:
            presentation = new MultiinstanceGraphElementPresentation();
            ((MultiinstanceGraphElementPresentation)presentation).setReadPermission(true);
            Iterator<ProcessLog> logIterator = processLogs.iterator();
            while(logIterator.hasNext()) {
            	ProcessLog processLog = logIterator.next();
            	if(processLog.getId() > nodeEnterlog.getId() && processLog instanceof SubprocessStartLog) {
            		((MultiinstanceGraphElementPresentation)presentation).addSubprocessId(((SubprocessStartLog) processLog).getSubprocessId());
            	} else if(processLog.getId() > nodeEnterlog.getId() && !(processLog instanceof SubprocessStartLog)) {
            		break;
            	}
            }
            break;
        case TaskNode:
            presentation = new TaskGraphElementPresentation();
            break;
        default:
            presentation = new GraphElementPresentation();
        }
        presentation.initialize(node, nodeModel);
        presentation.setGraphConstraints(nodeModel.getConstraints());
        
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(nodeEnterlog.getDate());
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(nodeLeaveLog.getDate());
        Long period = endCal.getTimeInMillis() - startCal.getTimeInMillis();
        Calendar periodCal = Calendar.getInstance();
        periodCal.setTimeInMillis(period);
        String date = getPeriodDateString(startCal, endCal);
        
        if(nodeModel.getType().equals(NodeType.Subprocess) || nodeModel.getType().equals(NodeType.MultiSubprocess)) {
        	presentation.setData("Time period is " + date);
        } else if(nodeModel.getType().equals(NodeType.TaskNode)) {        	
        	StringBuffer str = new StringBuffer();
            
            TaskCreateLog taskCreateLog = null;
            TaskEndLog taskEndLog = null;
            for(ProcessLog processLog : processLogs) {
        		if(processLog.getId() > nodeEnterlog.getId()) {
        			if(processLog instanceof TaskCreateLog) {
        				taskCreateLog = (TaskCreateLog)processLog;
            			continue;
        			} else if(processLog instanceof TaskEndLog && taskCreateLog != null && taskCreateLog.getTaskName().equals(((TaskEndLog)processLog).getTaskName())) {
        				taskEndLog = (TaskEndLog)processLog;
        				break;
        			}        			
        		}
        	}
            
            if(taskEndLog != null) {
            	String actor = taskEndLog.getActorName();

            	TaskAssignLog prev = null;
    	        for (TaskLog tempLog : taskLogs) {
    	            if (tempLog instanceof TaskAssignLog) {
    	                prev = (TaskAssignLog) tempLog;
    	            } else if (tempLog.equals(taskEndLog)) {
    	                break;
    	            }
    	        }

    	        if (prev != null) {
    	            if (prev.getOldExecutorName() != null && !prev.getOldExecutorName().equals(actor)) {
    	                actor = prev.getOldExecutorName();
    	            }
    	        }

    	        for(Executor executor : executors) {
    	        	if(executor.getName().equals(actor)) {
    	        		if (executor instanceof Actor && ((Actor) executor).getFullName() != null) {
	    	            	str.append("Full Name is " + ((Actor) executor).getFullName() + ".</br>");
	    	            }
    	        		
	    	            str.append("Login is " + executor.getName() + ".</br>");
    	        	}
    	        }
            }
            
            str.append("Time period is " + date + ".");
            presentation.setData(str.toString());
        }       
        
        logElements.add(presentation);
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

        result = result + nodeDateFormat.format(periodCal.getTime());

        return result;
    }
    
    public List<GraphElementPresentation> getLogElements() {
        return logElements;
    }
}
