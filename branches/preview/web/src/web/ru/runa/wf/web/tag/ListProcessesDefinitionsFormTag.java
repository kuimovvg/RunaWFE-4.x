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

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.TD;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.GroupState;
import ru.runa.common.web.Messages;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.CheckboxTDBuilder;
import ru.runa.common.web.html.EnvBaseImpl;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.web.Resources;
import ru.runa.wf.web.action.UndeployProcessDefinitionAction;
import ru.runa.wf.web.html.DefinitionUrlStrategy;
import ru.runa.wf.web.html.PropertiesProcessTDBuilder;
import ru.runa.wf.web.html.StartProcessTDBuilder;

/**
 * Created on 30.09.2004
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 * @jsp.tag name = "listProcessesDefinitionsForm" body-content = "JSP"
 */
public class ListProcessesDefinitionsFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = 8409543832272909874L;

    private boolean isButtonEnabled;

    @Override
    protected void fillFormElement(TD tdFormElement) throws JspException {
        try {
            DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
            BatchPresentation batchPresentation = getBatchPresentation();
            List<ProcessDefinition> definitions = definitionService.getLatestProcessDefinitionStubs(getSubject(), batchPresentation);
            PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, definitions.size());
            navigation.addPagingNavigationTable(tdFormElement);
            isButtonEnabled = isUndeployAllowed(definitions);
            TDBuilder[] builders = getBuilders(new TDBuilder[] { new CheckboxTDBuilder("nativeId", ProcessDefinitionPermission.UNDEPLOY_DEFINITION),
                    new StartProcessTDBuilder() }, batchPresentation, new TDBuilder[] { new PropertiesProcessTDBuilder() });
            String[] prefixCellsHeaders = getGrouppingCells(batchPresentation, definitions);
            SortingHeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, prefixCellsHeaders, new String[] { "" },
                    getReturnAction(), pageContext);
            RowBuilder rowBuilder = new ReflectionRowBuilder(definitions, batchPresentation, pageContext, Resources.ACTION_MAPPING_START_INSTANCE,
                    getReturnAction(), new DefinitionUrlStrategy(pageContext), builders);
            tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
            navigation.addPagingNavigationTable(tdFormElement);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private String[] getGrouppingCells(BatchPresentation batchPresentation, List<ProcessDefinition> definitions) {
        List<String> prefixCellsHeaders = new ArrayList<String>();
        int grouppingCells = GroupState.getMaxAdditionalCellsNum(batchPresentation, definitions, new EnvImpl(batchPresentation));
        for (int i = 0; i < 1 + grouppingCells; ++i) {
            prefixCellsHeaders.add("");
        }
        prefixCellsHeaders.add(Messages.getMessage(Messages.LABEL_START_INSTANCE, pageContext));
        return prefixCellsHeaders.toArray(new String[prefixCellsHeaders.size()]);
    }

    private boolean isUndeployAllowed(List<ProcessDefinition> definitions) throws AuthenticationException {
        AuthorizationService authorizationService = ru.runa.delegate.DelegateFactory.getInstance().getAuthorizationService();
        for (boolean undeploy : authorizationService.isAllowed(getSubject(), ProcessDefinitionPermission.UNDEPLOY_DEFINITION, definitions)) {
            if (undeploy) {
                return true;
            }
        }
        return false;
    }

    class EnvImpl extends EnvBaseImpl {

        public EnvImpl(BatchPresentation batch) {
            batchPresentation = batch;
        }

        @Override
        public Subject getSubject() {
            if (subject == null) {
                subject = SubjectHttpSessionHelper.getActorSubject(pageContext.getSession());
            }
            return subject;
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
            return new DefinitionUrlStrategy(pageContext).getUrl(Resources.ACTION_MAPPING_START_INSTANCE, object);
        }

        @Override
        public String getConfirmationMessage(Long pid) {
            return null;
        }

        @Override
        public boolean isAllowed(Permission permission, IdentifiableExtractor extractor) throws AuthorizationException, AuthenticationException {
            return false;
        }

        @Override
        public Object getTaskVariable(Object object, IdentifiableExtractor taskIdExtractor, String variableName) {
            return null;
        }

        Subject subject = null;
        BatchPresentation batchPresentation = null;
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_UNDEPLOY_DEFINITION, pageContext);
    }

    @Override
    protected boolean isFormButtonEnabled() {
        return isButtonEnabled;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PROCESS_DEFINITIONS, pageContext);
    }

    @Override
    public String getAction() {
        return UndeployProcessDefinitionAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.UNDEPLOY_PROCESS_DEFINITION_PARAMETER;
    }
}
