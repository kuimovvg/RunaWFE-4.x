package ru.runa.wfe.office.storage.handler;

import java.util.List;

import org.dom4j.Element;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.office.excel.IExcelConstraints;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.storage.ConditionItem;
import ru.runa.wfe.office.storage.Op;
import ru.runa.wfe.office.storage.UpdateConditionItem;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.QueryType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class StorageBindingsParser extends FilesSupplierConfigParser<DataBindings> {

    @Override
    protected DataBindings instantiate() {
        return new DataBindings();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void parseCustom(Element root, DataBindings bindings) throws Exception {
        List<Element> bindingElements = root.elements("binding");

        for (Element bindingElement : bindingElements) {

            String className = bindingElement.attributeValue("class");
            Preconditions.checkNotNull(className, "Missed 'class' attribute in binding element");

            String variableName = bindingElement.attributeValue("variable");
            Preconditions.checkNotNull(variableName, "Missed 'variable' attribute in binding element");

            IExcelConstraints constraints = ClassLoaderUtil.instantiate(className);
            Element configElement = bindingElement.element("config");
            Preconditions.checkNotNull(configElement, "Missed 'config' element in binding element");

            constraints.configure(configElement);

            Element conditionsElement = bindingElement.element("conditions");
            if (bindings.getQueryType() == null) {
                bindings.setQueryType(QueryType.valueOf(conditionsElement.attributeValue("type")));
            }

            DataBinding binding = new DataBinding();
            if (conditionsElement != null) {
                binding.setConditions(configureConditions(conditionsElement));
            }
            binding.setConstraints(constraints);
            binding.setVariableName(variableName);

            bindings.getBindings().add(binding);
        }
    }

    @SuppressWarnings("unchecked")
    private List<ConditionItem> configureConditions(Element conditionsElement) {
        List<ConditionItem> conditionItems = Lists.newArrayList();
        QueryType queryType = QueryType.valueOf(conditionsElement.attributeValue("type"));
        List<Element> conditions = conditionsElement.elements("condition");
        for (Element element : conditions) {
            ConditionItem conditionItem = null;
            Op op = Op.valueOf(element.attributeValue("is"));
            Object val = element.attributeValue("val");
            if (QueryType.UPDATE.equals(queryType)) {
                Object newVal = element.attributeValue("newVal");
                conditionItem = new UpdateConditionItem();
                ((UpdateConditionItem) conditionItem).setNewValue(newVal);
            } else {
                conditionItem = new ConditionItem();
            }
            conditionItem.setOperator(op);
            conditionItem.setValue(val);
            conditionItems.add(conditionItem);
        }
        return conditionItems;
    }
}
