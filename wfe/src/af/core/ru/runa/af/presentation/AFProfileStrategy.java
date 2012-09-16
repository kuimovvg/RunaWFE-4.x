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
package ru.runa.af.presentation;

/**
 * Created on 22.10.2005
 *
 */
public class AFProfileStrategy implements ProfileStrategy {
    public static final String ALL_EXECUTORS_BATCH_PRESENTATION_ID = "listAllExecutorsForm";
    public static final String EXECUTORS_GROUPS_BATCH_PRESENTATION_ID = "listExecutorGroupsForm";
    public static final String EXECUTORS_WITHOUT_PERMISSIONS_ON_EXECUTOR_BATCH_PRESENTATION_ID = "listExecutorsWithoutPermissionsOnExecutorForm";
    public static final String GROUP_MEMBERS_BATCH_PRESENTATION_ID = "listGroupMembersForm";
    public static final String NOT_EXECUTOR_IN_GROUPS_BATCH_PRESENTATION_ID = "listNotExecutorGroupsForm";
    public static final String NOT_GROUP_MEMBERS_BATCH_PRESENTATION_ID = "listNotGroupMembersForm";
    public static final String EXECUTORS_WITHOUT_PERMISSIONS_ON_SYSTEM_BATCH_PRESENTATION_ID = "listExecutorsWithoutPermissionsOnSystemForm";
    public static final String EXECUTORS_WITHOUT_PERMISSIONS_ON_DEFINITION_BATCH_PRESENTATION_ID = "listExecutorsWithoutPermissionsOnDefinitionForm";
    public static final String EXECUTORS_WITHOUT_PERMISSIONS_ON_INSTANCE_BATCH_PRESENTATION_ID = "listExecutorsWithoutPermissionsOnProcessInstanceForm";
    public static final String EXECUTORS_WITHOUT_PERMISSIONS_ON_RELATIONS_BATCH_PRESENTATION_ID = "listExecutorsWithoutPermissionsOnRelationsForm";
    public static final String EXECUTORS_WITHOUT_BOT_STATION_PERMISSION = "listExecutorsWithoutBotStationPermission";
    public static final String RELATIONS = "listRelations";
    public static final String RELATION_GROUPS = "listRelationGroups";
    public static final DefaultBatchPresentationFactory EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY = new DefaultBatchPresentationFactory(
            getClassPresentationId(ExecutorClassPresentation.class), 100);
    public static final DefaultBatchPresentationFactory RELATIONS_DEFAULT_BATCH_PRESENTATOIN_FACTORY = new DefaultBatchPresentationFactory(
            getClassPresentationId(RelationClassPresentation.class));
    public static final DefaultBatchPresentationFactory RELATION_GROUPS_DEFAULT_BATCH_PRESENTATOIN_FACTORY = new DefaultBatchPresentationFactory(
            getClassPresentationId(RelationGroupClassPresentation.class));

    public Profile createDefaultProfile() {
        BatchPresentation ListAllExecutorsFormTagPresentation = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, ALL_EXECUTORS_BATCH_PRESENTATION_ID);
        BatchPresentation ListExecutorGroupsFormTagPresentation = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, EXECUTORS_GROUPS_BATCH_PRESENTATION_ID);
        BatchPresentation ListExecutorsWithoutPermissionsOnExecutorFormTagPresentation = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY
                .getDefaultBatchPresentation(BatchPresentationConsts.DEFAULT_NAME, EXECUTORS_WITHOUT_PERMISSIONS_ON_EXECUTOR_BATCH_PRESENTATION_ID);
        BatchPresentation ListGroupChildrenFormTagPresentation = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, GROUP_MEMBERS_BATCH_PRESENTATION_ID);
        BatchPresentation ListNotExecutorGroupsFormTagPresentation = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, NOT_EXECUTOR_IN_GROUPS_BATCH_PRESENTATION_ID);
        BatchPresentation ListNotGroupChildrenTagPresentation = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, NOT_GROUP_MEMBERS_BATCH_PRESENTATION_ID);
        BatchPresentation ListExecutorsWithoutPermissionsOnSystemFormTagPresentation = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY
                .getDefaultBatchPresentation(BatchPresentationConsts.DEFAULT_NAME, EXECUTORS_WITHOUT_PERMISSIONS_ON_SYSTEM_BATCH_PRESENTATION_ID);
        BatchPresentation ListExecutorsWithoutPermissionsOnDefinitionFormTagPresentation = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY
                .getDefaultBatchPresentation(BatchPresentationConsts.DEFAULT_NAME, EXECUTORS_WITHOUT_PERMISSIONS_ON_DEFINITION_BATCH_PRESENTATION_ID);
        BatchPresentation ListExecutorsWithoutPermissionsOnPocessInstanceFormTagPresentation = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY
                .getDefaultBatchPresentation(BatchPresentationConsts.DEFAULT_NAME, EXECUTORS_WITHOUT_PERMISSIONS_ON_INSTANCE_BATCH_PRESENTATION_ID);
        BatchPresentation ListExecutorsWithoutPermissionsOnRelationsFormTagPresentation = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY
                .getDefaultBatchPresentation(BatchPresentationConsts.DEFAULT_NAME, EXECUTORS_WITHOUT_PERMISSIONS_ON_RELATIONS_BATCH_PRESENTATION_ID);
        BatchPresentation ListExecutorsWithoutBotStationPermission = EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, EXECUTORS_WITHOUT_BOT_STATION_PERMISSION);
        BatchPresentation ListRelations = RELATIONS_DEFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, RELATIONS);
        BatchPresentation ListRelationGroups = RELATION_GROUPS_DEFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation(
                BatchPresentationConsts.DEFAULT_NAME, RELATION_GROUPS);
        BatchPresentation[] batchPresentations = { ListAllExecutorsFormTagPresentation, ListExecutorGroupsFormTagPresentation,
                ListExecutorsWithoutPermissionsOnExecutorFormTagPresentation, ListGroupChildrenFormTagPresentation,
                ListNotExecutorGroupsFormTagPresentation, ListNotGroupChildrenTagPresentation,
                ListExecutorsWithoutPermissionsOnSystemFormTagPresentation, ListExecutorsWithoutPermissionsOnDefinitionFormTagPresentation,
                ListExecutorsWithoutPermissionsOnPocessInstanceFormTagPresentation, ListExecutorsWithoutBotStationPermission, ListRelations,
                ListRelationGroups, ListExecutorsWithoutPermissionsOnRelationsFormTagPresentation };
        Profile profile = new Profile(batchPresentations);
        return profile;
    }

    static protected int getClassPresentationId(Class classPresentationClass) {
        return classPresentationClass.getName().hashCode();
    }

    static protected ClassPresentation getClassPresentation(int classPresentationId) {
        return ClassPresentationFinderFactory.getClassPresentationFinder().getClassPresentationById(classPresentationId);
    }
}
