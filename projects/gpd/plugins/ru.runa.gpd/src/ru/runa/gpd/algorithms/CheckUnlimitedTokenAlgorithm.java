package ru.runa.gpd.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.Fork;
import ru.runa.gpd.lang.model.Join;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ParallelGateway;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Transition;

public class CheckUnlimitedTokenAlgorithm {
	List<Transition> transitions;
	List<Node> nodes;
	List<Vector> vVectorList = new ArrayList<Vector>();
	List<Vector> graphList = new ArrayList<Vector>();
	ListUnprocessedStates listUnprocessedStates = new ListUnprocessedStates();
	List<TransitionVector> transitionVectors = new ArrayList<TransitionVector>();
	
	public CheckUnlimitedTokenAlgorithm(List<Transition> transitions, List<Node> nodes) {
		this.transitions = transitions;
		this.nodes = nodes;
		init();
	}
	
	public Transition startAlgorithm() {
		List<Vector> startVectorList = new ArrayList<Vector>();
		Vector vector = new Vector(transitions.size() + 1);
		vector.setElementValue(0, 1);
		startVectorList.add(vector);
		listUnprocessedStates.addInList(startVectorList);
		
		while(listUnprocessedStates.isFirstObjExist()) {			
			Vector uVector = listUnprocessedStates.getFirstObj();
			
			List<Vector> listIntermediateVectors = new ArrayList<Vector>();
			for(Vector vVector : vVectorList) {
				Vector tempVector = uVector.getVectorsSum(vVector);
				if(!tempVector.isNegativeNumberExist() && !tempVector.isNullValueVector()) {
					listIntermediateVectors.add(tempVector);
				}
			}
			
			List<Vector> equalVectors = new ArrayList<Vector>();
 			for(Vector intermediateVector : listIntermediateVectors) {
				for(Vector graphVector : graphList) {
					if(Arrays.equals(intermediateVector.getElements(), graphVector.getElements())) {
						equalVectors.add(intermediateVector);
						transitionVectors.add(new TransitionVector(uVector, graphVector));
					}
				}
			}
 			
 			listIntermediateVectors.removeAll(equalVectors);
 			
 			for(Vector intermediateVector : listIntermediateVectors) {
 				transitionVectors.add(new TransitionVector(uVector, intermediateVector));
 			}
 			
 			graphList.addAll(listIntermediateVectors);
 			listUnprocessedStates.addInList(listIntermediateVectors);
 			listIntermediateVectors.clear();
 			
 			listUnprocessedStates.removeFirst();
 			
 			for(Vector unprocessedVector : listUnprocessedStates.getList()) {
 				List<Vector> attainableVectorList = getAttainableVectorList(unprocessedVector);
 				for(Vector attainableVector : attainableVectorList) {
 					int stongminusindex = 0;
 					int strongminus = 0;
					int minusequal = 0;
 					for(int i = 0; i < unprocessedVector.getElements().length; i++) {
 						if(attainableVector.getElements()[i] < unprocessedVector.getElements()[i]) {
							strongminus++;
							stongminusindex = i;
							continue;
						}
						if(attainableVector.getElements()[i] <= unprocessedVector.getElements()[i]) {
							minusequal++;
						}
 					}
 					if(strongminus == 1 && minusequal == unprocessedVector.getElements().length - 1) {						
						return transitions.get(stongminusindex - 1);
					}
 				}
 			}
		}		
		
		return null;
	}
	
	private void init() {
		populateVvectorList();		
		Vector vector = new Vector(transitions.size() + 1);
		vector.setElementValue(0, 1);
		graphList.add(vector);
	}
	
	private void populateVvectorList() {
		for(Node node : nodes) {
			if(node instanceof StartState) {
				for(Transition transition : transitions) {					
					if(transition.getSource().equals(node)) {	
						Vector v = new Vector(transitions.size() + 1);
						v.setElementValue(0, -1);
						v.setElementValue(transitions.indexOf(transition) + 1, 1);
						vVectorList.add(v);
					}
				}
			}
		}
		
		for(Node node : nodes) {
			if(node instanceof Join || node instanceof Fork || node instanceof ParallelGateway) {
				Vector v = new Vector(transitions.size() + 1);
				for(Transition transition : transitions) {
					if(transition.getSource().equals(node)) {
						v.setElementValue(transitions.indexOf(transition) + 1, 1);
					}
					if(transition.getTarget().equals(node)) {
						v.setElementValue(transitions.indexOf(transition) + 1, -1);
					}
				}
				
				vVectorList.add(v);
			}
		}
		
		for(Node node : nodes) {
			if(!(node instanceof Join || node instanceof Fork || node instanceof ParallelGateway || node instanceof StartState || node instanceof EndState)) {
				for(Transition transition : transitions) {					
					if(transition.getTarget().equals(node)) {						
						List<Transition> addedVectors = new ArrayList<Transition>();						
						for(Transition transition1 : transitions) {							
							if(transition1.getSource().equals(node) && !addedVectors.contains(transition1)) {
								Vector v = new Vector(transitions.size() + 1);
								v.setElementValue(transitions.indexOf(transition) + 1, -1);
								v.setElementValue(transitions.indexOf(transition1) + 1, 1);
								vVectorList.add(v);
								addedVectors.add(transition1);
							}
						}
					}
				}
			}
		}
	}
	
	private List<Vector> getAttainableVectorList(Vector unprocessedVector) {
		List<Vector> buffer = new ArrayList<Vector>();
		List<Vector> listVectors = new ArrayList<Vector>();
		
		for(TransitionVector transitionVector : transitionVectors) {
			if(Arrays.equals(unprocessedVector.getElements(), transitionVector.getToVector().getElements())) {
				buffer.add(transitionVector.getFromVector());				
			}
		}
		
		while(buffer.size() > 0) {
			List<Vector> foundedVectors = new ArrayList<Vector>();
			for(TransitionVector transitionVector : transitionVectors) {
				for(Vector tempVector : buffer) {
					if(Arrays.equals(tempVector.getElements(), transitionVector.getToVector().getElements())) {
						foundedVectors.add(transitionVector.getFromVector());
					}
				}
			}
			
			Iterator<Vector> foundedIterator = foundedVectors.iterator();
			while(foundedIterator.hasNext()) {
				Vector foundVector = foundedIterator.next();
				for(Vector bufferVector : buffer) {
					if(Arrays.equals(foundVector.getElements(), bufferVector.getElements())) {
						foundedIterator.remove();
						break;
					}
				}
			}
			
			foundedIterator = foundedVectors.iterator();
			while(foundedIterator.hasNext()) {
				Vector foundVector = foundedIterator.next();
				for(Vector listVector : listVectors) {
					if(Arrays.equals(foundVector.getElements(), listVector.getElements())) {
						foundedIterator.remove();
						break;
					}
				}
			}
			
			listVectors.addAll(buffer);
			buffer.clear();
			buffer.addAll(foundedVectors);
		}
		
		return listVectors;
	}
}
