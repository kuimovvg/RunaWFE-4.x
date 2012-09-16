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
package ru.runa.af.web.tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.TD;

import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.Permission;
import ru.runa.af.Relation;
import ru.runa.af.RelationPair;
import ru.runa.af.RelationPermission;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.presentation.FieldDescriptor;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.RelationService;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.IdentifiableFormTag;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

/**
 * List relations, which contains executor.
 * 
 * @jsp.tag name = "listExecutorLeftRelationsForm" body-content = "JSP"
 */
public class ListExecutorLeftRelationsFormTag extends IdentifiableFormTag {

    /**
     * Serializable version.
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) throws JspException {
        ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
        RelationService relationService = DelegateFactory.getInstance().getRelationService();
        try {
            List<Executor> executors = new ArrayList<Executor>();
            executors.add(getIdentifiable());
            BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
            batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
            for (Group group : executorService.getExecutorGroups(getSubject(), getIdentifiable(), batchPresentation, false)) {
                executors.add(group);
            }
            Set<Relation> relations = new HashSet<Relation>();
            for (RelationPair pair : relationService.getExecutorsRelationPairsLeft(getSubject(), null, executors)) {
                relations.add(pair.getRelation());
            }
            TableBuilder tableBuilder = new TableBuilder();

            TDBuilder[] builders = getBuilders(new TDBuilder[] {}, AFProfileStrategy.RELATION_GROUPS_DEFAULT_BATCH_PRESENTATOIN_FACTORY
                    .getDefaultBatchPresentation(), new TDBuilder[] {});

            RowBuilder rowBuilder = new ReflectionRowBuilder(Lists.newArrayList(relations), batchPresentation, pageContext,
                    ru.runa.af.web.Resources.ACTION_MAPPING_MANAGE_EXECUTOR_LEFT_RELATION, "", new RelationURLStrategy(), builders);
            HeaderBuilder headerBuilder = new StringsHeaderBuilder(getNames());

            tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_EXECUTOR_LEFT_RELATIONS, pageContext);
    }

    @Override
    protected boolean isFormButtonVisible() throws JspException {
        return false;
    }

    @Override
    protected Executor getIdentifiable() throws JspException {
        ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
        try {
            return executorService.getExecutor(getSubject(), getIdentifiableId());
        } catch (Exception e) {
            throw new JspException(e);
        }
    }

    @Override
    protected Permission getPermission() throws JspException {
        return RelationPermission.READ;
    }

    protected String[] getNames() {
        BatchPresentation batchPresentation = AFProfileStrategy.RELATION_GROUPS_DEFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        FieldDescriptor[] fields = batchPresentation.getDisplayFields();
        String[] result = new String[fields.length];
        for (int i = 0; i < fields.length; ++i) {
            result[i] = Messages.getMessage(fields[i].displayName, pageContext);
        }
        return result;
    }

    class RelationURLStrategy implements ItemUrlStrategy {

        @Override
        public String getUrl(String baseUrl, Object item) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("relationName", ((Relation) item).getName());
            params.put("executorId", getIdentifiableId());
            return Commons.getActionUrl(baseUrl, params, pageContext, PortletUrl.Action);
        }
    }
}
