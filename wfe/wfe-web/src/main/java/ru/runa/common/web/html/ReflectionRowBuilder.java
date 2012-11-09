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
package ru.runa.common.web.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Entities;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.GroupState;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ExpandCollapseGroupAction;
import ru.runa.common.web.form.GroupForm;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.ReturnActionForm;
import ru.runa.common.web.form.SetSortingForm;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.service.wf.DefinitionService;
import ru.runa.service.wf.ExecutionService;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;

import com.google.common.collect.Lists;

/**
 * @author Gritsenko_S
 */
public class ReflectionRowBuilder implements RowBuilder {

    class EnvImpl extends EnvBaseImpl {
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
            return itemUrlStrategy.getUrl(basePartOfUrlToObject, object);
        }

        @Override
        public String getConfirmationMessage(Long pid) {
            if (basePartOfUrlToObject.equals(ru.runa.common.WebResources.ACTION_MAPPING_START_PROCESS)
                    && ConfirmationPopupHelper.getInstance().isEnabled(ConfirmationPopupHelper.START_PROCESS_PARAMETER)
                    || ConfirmationPopupHelper.getInstance().isEnabled(ConfirmationPopupHelper.START_PROCESS_FORM_PARAMETER)) {
                DefinitionService definitionService = DelegateFactory.getDefinitionService();
                try {
                    if (!(definitionService.getStartInteraction(getSubject(), pid).hasForm() || definitionService.getOutputTransitionNames(
                            getSubject(), pid, null).size() > 1)) {
                        String actionParameter = ConfirmationPopupHelper.START_PROCESS_FORM_PARAMETER;
                        return ConfirmationPopupHelper.getInstance().getConfirmationPopupCodeHTML(actionParameter, getPageContext());
                    }

                } catch (Exception e) {
                    throw new InternalApplicationException(e);
                }
            }
            return null;
        }

        @Override
        public boolean isAllowed(Permission permission, IdentifiableExtractor extractor) throws AuthorizationException, AuthenticationException {
            boolean[] retVal = isAllowedCache.get(new Pair(permission, extractor));
            if (retVal == null) {
                AuthorizationService authorizationService = DelegateFactory.getAuthorizationService();
                if (extractor == null) {
                    retVal = authorizationService.isAllowed(getSubject(), permission, (List<Identifiable>) items);
                } else {
                    List<Identifiable> identifiables = Lists.newArrayListWithExpectedSize(items.size());
                    for (Object object : items) {
                        identifiables.add(extractor.getIdentifiable(object, this));
                    }
                    retVal = authorizationService.isAllowed(getSubject(), permission, identifiables);
                }
                isAllowedCache.put(new Pair(permission, extractor), retVal);
            }
            return retVal[currentState.getItemIndex()];
        }

        @Override
        public Object getTaskVariable(Object object, IdentifiableExtractor processIdExtractor, String variableName) throws AuthenticationException {
            Map<Long, Object> cache = taskVariableCache.get(variableName);
            if (cache == null) {
                cache = new HashMap<Long, Object>();
                taskVariableCache.put(variableName, cache);
                List<Long> ids = Lists.newArrayListWithExpectedSize(items.size());
                for (int i = 0; i < items.size(); ++i) {
                    ids.add(processIdExtractor.getIdentifiable(items.get(i), this).getId());
                }
                ExecutionService executionService = DelegateFactory.getExecutionService();
                Map<Long, Object> variables = executionService.getVariableValuesFromProcesses(getSubject(), ids, variableName);
                cache.putAll(variables);
            }
            return cache.get(processIdExtractor.getIdentifiable(object, this).getId());
        }

        private class Pair {
            Permission perm;
            IdentifiableExtractor ident;

            public Pair(Permission perm, IdentifiableExtractor ident) {
                this.perm = perm;
                this.ident = ident;
            }

            @Override
            public int hashCode() {
                return perm.hashCode();
            }

            @Override
            public boolean equals(Object other) {
                if (other == null) {
                    return false;
                }
                if (!(other instanceof Pair)) {
                    return false;
                }
                Pair otherPair = (Pair) other;
                return (perm.equals(otherPair.perm)) && (ident == null ? otherPair.ident == null : ident.equals(otherPair.ident));
            }
        }

        private final Map<Pair, boolean[]> isAllowedCache = new HashMap<Pair, boolean[]>();
        private final Map<String, Map<Long, Object>> taskVariableCache = new HashMap<String, Map<Long, Object>>();

        private Subject subject = null;
    }

    private final List<? extends Object> items;

    private final String basePartOfUrlToObject;

    private final BatchPresentation batchPresentation;

    private GroupState currentState = null;

    private final String returnAction;

    private final PageContext pageContext;

    private ItemUrlStrategy itemUrlStrategy;

    private CssClassStrategy cssClassStrategy;

    private final int additionalEmptyCells;

    private final TDBuilder[] builders;
    private final EnvImpl env;

    public ReflectionRowBuilder(List<? extends Object> items, BatchPresentation batchPresentation, PageContext pageContext, String actionUrl,
            String returnAction, String idPropertyName, TDBuilder[] builders) {
        this(items, batchPresentation, pageContext, actionUrl, returnAction, builders);
        itemUrlStrategy = new DefaultItemUrlStrategy(idPropertyName, pageContext);
    }

    public ReflectionRowBuilder(List<? extends Object> items, BatchPresentation batchPresentation, PageContext pageContext, String actionUrl,
            String returnAction, ItemUrlStrategy itemUrlStrategy, TDBuilder[] builders) {
        this(items, batchPresentation, pageContext, actionUrl, returnAction, builders);
        this.itemUrlStrategy = itemUrlStrategy;
    }

    private ReflectionRowBuilder(List<? extends Object> items, BatchPresentation batchPresentation, PageContext pageContext, String actionUrl,
            String returnAction, TDBuilder[] builders) {
        this.items = items;
        this.batchPresentation = batchPresentation;
        this.pageContext = pageContext;
        basePartOfUrlToObject = actionUrl;
        this.returnAction = returnAction;
        env = new EnvImpl();
        currentState = GroupState.createStartState(items, batchPresentation, builders, env);
        additionalEmptyCells = GroupState.getMaxAdditionalCellsNum(batchPresentation, items, env);
        this.builders = builders;
    }

    private TR buildGroupHeader() {
        TR tr = new TR();
        createEmptyCells(tr, currentState.getGroupIndex() + currentState.getAdditionalColumn());

        IMG groupingImage = null;
        if (currentState.isVisible()) {
            groupingImage = new IMG(Commons.getUrl(Resources.GROUP_MINUS_IMAGE, pageContext, PortletUrlType.Resource));
            groupingImage.setAlt(Resources.GROUP_MINUS_ALT);
        } else {
            groupingImage = new IMG(Commons.getUrl(Resources.GROUP_PLUS_IMAGE, pageContext, PortletUrlType.Resource));
            groupingImage.setAlt(Resources.GROUP_PLUS_ALT);
        }
        groupingImage.setBorder(0);

        String anchorId = currentState.getCurrentGrouppedColumnValue(currentState.getGroupIndex());
        if (anchorId == null) {
            anchorId = "";
        }
        String groupId = currentState.getGroupId();

        TD td = new TD();
        td.setClass(Resources.CLASS_GROUP_NAME);
        td.addElement(new A().setName(anchorId));

        Map<String, String> params = new HashMap<String, String>();
        params.put(SetSortingForm.BATCH_PRESENTATION_ID, batchPresentation.getCategory());
        params.put(GroupForm.GROUP_ID, groupId);
        params.put(ReturnActionForm.RETURN_ACTION, returnAction);
        params.put(GroupForm.GROUP_ACTION_ID, currentState.isVisible() ? GroupForm.GROUP_ACTION_COLLAPSE : GroupForm.GROUP_ACTION_EXPAND);
        String actionUrl = Commons.getActionUrl(ExpandCollapseGroupAction.ACTION_PATH, params, anchorId, pageContext, PortletUrlType.Action);
        A link = new A(actionUrl, groupingImage);

        td.addElement(link);
        link.addElement(Entities.NBSP);
        FieldDescriptor[] fieldsToDisplayNames = batchPresentation.getAllFields();
        if (fieldsToDisplayNames[currentState.getCurrentGrouppedColumnIdx()].displayName.startsWith(ClassPresentation.removable_prefix)) {
            FieldDescriptor field = fieldsToDisplayNames[currentState.getCurrentGrouppedColumnIdx()];
            int end = fieldsToDisplayNames[currentState.getCurrentGrouppedColumnIdx()].displayName.lastIndexOf(':');
            int begin = field.displayName.lastIndexOf(':', end - 1) + 1;
            link.addElement(Messages.getMessage(field.displayName.substring(begin, end), pageContext) + " '"
                    + field.displayName.substring(field.displayName.lastIndexOf(':') + 1) + "':");
        } else if (fieldsToDisplayNames[currentState.getCurrentGrouppedColumnIdx()].displayName.startsWith(ClassPresentation.filterable_prefix)) {
            FieldDescriptor field = fieldsToDisplayNames[currentState.getCurrentGrouppedColumnIdx()];
            link.addElement(Messages.getMessage(field.displayName.substring(field.displayName.lastIndexOf(':') + 1), pageContext));
        } else {
            link.addElement(ru.runa.common.web.Messages.getMessage(fieldsToDisplayNames[currentState.getCurrentGrouppedColumnIdx()].displayName,
                    pageContext) + ":");
        }
        link.addElement(Entities.NBSP);
        link.addElement(currentState.getCurrentGrouppedColumnValue());
        td.setColSpan(builders.length + batchPresentation.getGrouppedFields().length - currentState.getGroupIndex() + additionalEmptyCells);
        tr.addElement(td);
        return tr;
    }

    private TR buildItemRow() {
        Object item = items.get(currentState.getItemIndex());
        TR tr = new TR();
        if (cssClassStrategy != null) {
            String cssClassName = cssClassStrategy.getClassName(item, env.getSubject());
            if (cssClassName != null) {
                tr.setClass(cssClassName);
            }

            String cssStyle = cssClassStrategy.getCssStyle(item);
            if (cssStyle != null) {
                tr.setStyle(cssStyle);
            }
        }
        if (batchPresentation.getGrouppedFields().length > 0) {
            createEmptyCells(tr, currentState.getGroupIndex() + additionalEmptyCells);
        }

        List<Object> listGroupTDBuilders = new ArrayList<Object>();
        for (FieldDescriptor fieldDescriptor : Arrays.asList(batchPresentation.getGrouppedFields())) {
            listGroupTDBuilders.add(fieldDescriptor.getTDBuilder());
        }

        for (int i = 0; i < builders.length; i++) {
            TD td = builders[i].build(item, env);

            if (listGroupTDBuilders.contains(builders[i])) {
                FieldDescriptor fieldDescriptorForBuilder = null;
                for (FieldDescriptor fieldDescriptor : Arrays.asList(batchPresentation.getGrouppedFields())) {
                    if (builders[i].equals(fieldDescriptor.getTDBuilder())) {
                        fieldDescriptorForBuilder = fieldDescriptor;
                    }
                }

                ConcreteElement tdElement = null;
                String message = null;
                String displayName = fieldDescriptorForBuilder.displayName;

                if (displayName.startsWith(ClassPresentation.removable_prefix)) {
                    message = displayName.substring(displayName.lastIndexOf(':') + 1);
                } else {
                    message = Messages.getMessage(displayName, pageContext);
                }

                if (td.elements().hasMoreElements()) {
                    ConcreteElement concreteElement = (ConcreteElement) td.elements().nextElement();
                    if (concreteElement instanceof StringElement) {
                        tdElement = new StringElement(message);
                    } else if (concreteElement instanceof A) {
                        A a = (A) concreteElement;
                        String href = a.getAttribute("href");
                        tdElement = new A(href, message);
                    }
                }

                td = new TD();
                if (tdElement != null) {
                    td.addElement(tdElement);
                }

                td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
            }

            tr.addElement(td);
        }

        return tr;
    }

    public void setCssClassStrategy(CssClassStrategy cssClassStrategy) {
        this.cssClassStrategy = cssClassStrategy;
    }

    private TR renderTRFromCurrentState() {
        if (currentState.isGroupHeader()) {
            return buildGroupHeader();
        }
        return buildItemRow();
    }

    @Override
    public TR buildNext() {
        TR tr = renderTRFromCurrentState();
        // If element not displayed (in group), we must emulate displaying.
        // int curIdx = currentState.getItemIndex();
        // if (currentState.isGroupHeader()) {
        // curIdx--;
        // }
        do {
            currentState = currentState.buildNextState(batchPresentation);
        } while (currentState.getStateType().equals(GroupState.StateType.TYPE_EMPTY_STATE));
        return tr;
    }

    private void createEmptyCells(TR tr, int numberOfCells) {
        for (int i = 0; i < numberOfCells; i++) {
            TD cell = new TD();
            cell.addElement(Entities.NBSP);
            tr.addElement(cell);
            cell.setClass(Resources.CLASS_EMPTY20_TABLE_TD);
        }
    }

    @Override
    public boolean hasNext() {
        return (!currentState.equals(GroupState.STATE_NO_MORE_ELEMENTS));
    }

    private class DefaultItemUrlStrategy implements ItemUrlStrategy {
        private final PageContext context;

        private final String idPropertyName;

        public DefaultItemUrlStrategy(String idPropertyName, PageContext pageContext) {
            this.idPropertyName = idPropertyName;
            context = pageContext;
        }

        @Override
        public String getUrl(String baseUrl, Object item) {
            try {
                String idValue = BeanUtils.getProperty(item, idPropertyName);
                return Commons.getActionUrl(baseUrl, IdForm.ID_INPUT_NAME, idValue, context, PortletUrlType.Action);
            } catch (Exception e) {
                throw new InternalApplicationException(e);
            }
        }
    }
}
