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
package ru.runa.wf.presentation;

import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.presentation.DefaultBatchPresentationFactory;
import ru.runa.af.presentation.Profile;

/**
 * 
 * Created on 22.10.2005
 */
public class WFProfileStrategy extends AFProfileStrategy {
    public static final String PROCESS_INSTANCE_BATCH_PRESENTATION_ID = "listProcessesInstancesForm";
    public static final String PROCESS_DEFINITION_BATCH_PRESENTATION_ID = "listProcessesDefinitionsForm";
    public static final String PROCESS_TASK_BATCH_PRESENTATION_ID = "listTasksForm";
    public static final String SYSTEM_LOGS_BATCH_PRESENTATION_ID = "listSystemLogsForm";

    public static final DefaultBatchPresentationFactory TASK_DEFAULT_BATCH_PRESENTATION_FACTORY = new DefaultBatchPresentationFactory(
            getClassPresentationId(TaskClassPresentation.class));
    public static final DefaultBatchPresentationFactory PROCESS_DEFINITION_DEFAULT_BATCH_PRESENTATION_FACTORY = new DefaultBatchPresentationFactory(
            getClassPresentationId(ProcessDefinitionClassPresentation.class));
    public static final DefaultBatchPresentationFactory PROCESS_INSTANCE_DEFAULT_BATCH_PRESENTATION_FACTORY = new DefaultBatchPresentationFactory(
            getClassPresentationId(ProcessInstanceClassPresentation.class));
    public static final DefaultBatchPresentationFactory SYSTEM_LOGS_DEFAULT_BATCH_PRESENTATION_FACTORY = new DefaultBatchPresentationFactory(
            getClassPresentationId(SystemLogsClassPresentation.class));

    @Override
    public Profile createDefaultProfile() {
        Profile profile = super.createDefaultProfile();
        profile.addBatchPresentation(PROCESS_INSTANCE_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, PROCESS_INSTANCE_BATCH_PRESENTATION_ID));
        profile.addBatchPresentation(PROCESS_DEFINITION_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, PROCESS_DEFINITION_BATCH_PRESENTATION_ID));
        profile.addBatchPresentation(TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation(BatchPresentationConsts.DEFAULT_NAME,
                PROCESS_TASK_BATCH_PRESENTATION_ID));
        profile.addBatchPresentation(SYSTEM_LOGS_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation(BatchPresentationConsts.DEFAULT_NAME,
                SYSTEM_LOGS_BATCH_PRESENTATION_ID));
        return profile;
    }
}
