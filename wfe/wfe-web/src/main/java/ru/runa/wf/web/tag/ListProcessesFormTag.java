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
package ru.runa.wf.web.tag;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.TD;

import ru.runa.common.web.GroupState;
import ru.runa.common.web.Messages;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.EnvBaseImpl;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 15.10.2004
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 * @jsp.tag name = "listProcessesForm" body-content = "JSP"
 */
public class ListProcessesFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = 585180395259884607L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        BatchPresentation batchPresentation = getBatchPresentation();
        ExecutionService executionService = Delegates.getExecutionService();

        int instanceCount = executionService.getAllProcessesCount(getUser(), batchPresentation);
        // we must call getProcesses before obtaining current page number
        // since it can be changed after getProcesses call
        List<WfProcess> processes = executionService.getProcesses(getUser(), batchPresentation);
        // batchPresentation must be recalculated since the current page
        // number might changed
        batchPresentation = getBatchPresentation();
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, batchPresentation, instanceCount, getReturnAction());
        navigation.addPagingNavigationTable(tdFormElement);

        TDBuilder[] builders = getBuilders(new TDBuilder[] {}, batchPresentation, new TDBuilder[] {});
        String[] prefixCellsHeaders = getGrouppingCells(batchPresentation, processes);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, prefixCellsHeaders, new String[0], getReturnAction(), pageContext);
        RowBuilder rowBuilder = new ReflectionRowBuilder(processes, batchPresentation, pageContext, ShowGraphModeHelper.getManageProcessAction(),
                getReturnAction(), "id", builders);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));

        navigation.addPagingNavigationTable(tdFormElement);
    }

    private String[] getGrouppingCells(BatchPresentation batchPresentation, List<WfProcess> list) {
        List<String> prefixCellsHeaders = new ArrayList<String>();
        int grouppingCells = GroupState.getMaxAdditionalCellsNum(batchPresentation, list, new EnvImpl(batchPresentation));
        for (int i = 0; i < grouppingCells; ++i) {
            prefixCellsHeaders.add("");
        }
        return prefixCellsHeaders.toArray(new String[prefixCellsHeaders.size()]);
    }

    class EnvImpl extends EnvBaseImpl {

        public EnvImpl(BatchPresentation batch) {
            batchPresentation = batch;
        }

        @Override
        public PageContext getPageContext() {
            return pageContext;
        }

        @Override
        public BatchPresentation getBatchPresentation() {
            return batchPresentation;
        }

        @Override
        public String getURL(Object object) {
            return getReturnAction();
        }

        @Override
        public String getConfirmationMessage(Long pid) {
            return null;
        }

        @Override
        public boolean isAllowed(Permission permission, IdentifiableExtractor extractor) {
            return false;
        }

        BatchPresentation batchPresentation = null;
    }

    @Override
    protected boolean isFormButtonEnabled() {
        return false;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PROCESSES, pageContext);
    }
}
