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
package ru.runa.wfe.graph.view;

/**
 * Interface for operations, applied to {@link GraphElementPresentation}.
 */
public interface GraphElementPresentationVisitor {

    /**
     * Calling to handle subprocesses graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onSubprocess(SubprocessGraphElementPresentation element);

    /**
     * Calling to handle multiple instance graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onMultiinstance(MultiinstanceGraphElementPresentation element);

    /**
     * Calling to handle task state graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onTaskState(TaskGraphElementPresentation element);

    /**
     * Calling to handle state graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onState(WaitStateGraphElementPresentation element);

    /**
     * Calling to handle start state graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onStartState(StartStateGraphElementPresentation element);

    /**
     * Calling to handle end state graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onEndState(EndStateGraphElementPresentation element);

    /**
     * Calling to handle decision graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onDecision(DecisionGraphElementPresentation element);

    /**
     * Calling to handle fork graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onFork(ForkGraphElementPresentation element);

    /**
     * Calling to handle join graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onJoin(JoinGraphElementPresentation element);

    /**
     * Calling to handle node graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onNode(NodeGraphElementPresentation element);

    /**
     * Calling to handle send message graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onSendMessage(SendMessageGraphElementPresentation element);

    /**
     * Calling to handle receive message graph element.
     * 
     * @param element
     *            Element to handle.
     */
    void onReceiveMessage(ReceiveMessageGraphElementPresentation element);
}
