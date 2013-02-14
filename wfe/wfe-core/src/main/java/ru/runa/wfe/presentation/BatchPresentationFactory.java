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
package ru.runa.wfe.presentation;

import ru.runa.wfe.audit.SystemLogClassPresentation;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.relation.RelationClassPresentation;
import ru.runa.wfe.relation.RelationGroupClassPresentation;
import ru.runa.wfe.task.TaskClassPresentation;
import ru.runa.wfe.user.ActorClassPresentation;
import ru.runa.wfe.user.ExecutorClassPresentation;
import ru.runa.wfe.user.GroupClassPresentation;

/**
 * Default batch presentation factory.
 * 
 * @author Dofs
 * @since 4.0
 */
public class BatchPresentationFactory {
    public static final BatchPresentationFactory EXECUTORS = new BatchPresentationFactory(ExecutorClassPresentation.class, 100);
    public static final BatchPresentationFactory ACTORS = new BatchPresentationFactory(ActorClassPresentation.class, 100);
    public static final BatchPresentationFactory GROUPS = new BatchPresentationFactory(GroupClassPresentation.class, 100);
    public static final BatchPresentationFactory RELATIONS = new BatchPresentationFactory(RelationGroupClassPresentation.class);
    public static final BatchPresentationFactory RELATION_PAIRS = new BatchPresentationFactory(RelationClassPresentation.class);
    public static final BatchPresentationFactory SYSTEM_LOGS = new BatchPresentationFactory(SystemLogClassPresentation.class);
    public static final BatchPresentationFactory PROCESSES = new BatchPresentationFactory(ProcessClassPresentation.class, 100);
    public static final BatchPresentationFactory DEFINITIONS = new BatchPresentationFactory(DefinitionClassPresentation.class);
    public static final BatchPresentationFactory TASKS = new BatchPresentationFactory(TaskClassPresentation.class);

    private final ClassPresentation classPresentation;
    private final int defaultPageRangeSize;

    public BatchPresentationFactory(Class<?> classPresentationClass) {
        this(classPresentationClass, BatchPresentationConsts.getAllowedViewSizes()[0]);
    }

    public BatchPresentationFactory(Class<?> classPresentationClass, int defaultPageRangeSize) {
        this.classPresentation = ClassPresentations.getClassPresentation(classPresentationClass);
        this.defaultPageRangeSize = defaultPageRangeSize;
    }

    public BatchPresentation createDefault() {
        return createDefault(BatchPresentationConsts.DEFAULT_ID);
    }

    public BatchPresentation createDefault(String batchPresentationId) {
        BatchPresentation result = new BatchPresentation(BatchPresentationConsts.DEFAULT_NAME, batchPresentationId, classPresentation);
        result.setRangeSize(defaultPageRangeSize);
        return result;
    }

    public BatchPresentation createNonPaged() {
        BatchPresentation batchPresentation = createDefault(BatchPresentationConsts.DEFAULT_ID);
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        return batchPresentation;
    }

}
