package ru.runa.wf.web.datafile.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class RelationDataFileBuilder implements DataFileBuilder {
    private final User user;

    public RelationDataFileBuilder(User user) {
        this.user = user;
    }

    @Override
    public void build(ZipOutputStream zos, Document script) {
        List<Relation> relations = Delegates.getRelationService().getRelations(user, BatchPresentationFactory.RELATIONS.createDefault());
        for (Relation relation : relations) {
            Element element = populateRelation(script, relation);
            List<RelationPair> relationPairs = Delegates.getRelationService().getRelationPairs(user, relation.getName(),
                    BatchPresentationFactory.RELATION_PAIRS.createDefault());
            Element leftElement = element.addElement("left", XmlUtils.RUNA_NAMESPACE);
            Element rightElement = element.addElement("right", XmlUtils.RUNA_NAMESPACE);
            List<String> leftExecutors = new ArrayList<String>();
            List<String> rightExecutors = new ArrayList<String>();

            for (RelationPair relationPair : relationPairs) {
                if (!leftExecutors.contains(relationPair.getLeft().getName())) {
                    populateExecutor(leftElement, script, relationPair.getLeft().getName());
                    leftExecutors.add(relationPair.getLeft().getName());
                }

                if (!rightExecutors.contains(relationPair.getRight().getName())) {
                    populateExecutor(rightElement, script, relationPair.getRight().getName());
                    rightExecutors.add(relationPair.getRight().getName());
                }
            }
        }

        new PermissionsDataFileBuilder(new ArrayList<Identifiable>(relations), "addPermissionsOnRelation", user).build(zos, script);

    }

    private Element populateRelation(Document script, Relation relation) {
        Element element = script.getRootElement().addElement("relation", XmlUtils.RUNA_NAMESPACE);
        if (StringUtils.isNotEmpty(relation.getName())) {
            element.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, relation.getName());
        }
        return element;
    }

    private void populateExecutor(Element element, Document script, String executorName) {
        Element exElement = element.addElement(AdminScriptConstants.EXECUTOR_ELEMENT_NAME, XmlUtils.RUNA_NAMESPACE);
        exElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, executorName);
    }
}
