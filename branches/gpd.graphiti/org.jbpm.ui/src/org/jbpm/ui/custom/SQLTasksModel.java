package org.jbpm.ui.custom;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.jbpm.ui.PluginConstants;
import org.jbpm.ui.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SQLTasksModel extends Observable {

    public List<SQLTaskModel> tasks = new ArrayList<SQLTaskModel>();

    private SQLTasksModel() {
    }

    public static SQLTasksModel createDefault() {
        SQLTasksModel model = new SQLTasksModel();
        model.addNewTask(new SQLTaskModel());
        return model;
    }

    @Override
    public String toString() {
        try {
            Document document = XmlUtil.createDocument("database-tasks", "database-tasks.xsd");
            for (SQLTaskModel model : tasks) {
                model.serialize(document, document.getDocumentElement());
            }
            return new String(XmlUtil.writeXml(document), PluginConstants.UTF_ENCODING);
        } catch (Exception e) {
            throw new RuntimeException("Unable serialize model to XML", e);
        }
    }

    public static SQLTasksModel fromXml(String xml) throws Exception {
        SQLTasksModel model = new SQLTasksModel();
        Document document = XmlUtil.parseDocument(new ByteArrayInputStream(xml.getBytes(PluginConstants.UTF_ENCODING)));
        NodeList taskElements = document.getElementsByTagName("task");
        for (int i = 0; i < taskElements.getLength(); i++) {
            Element taskElement = (Element) taskElements.item(i);
            SQLTaskModel taskModel = SQLTaskModel.deserialize(taskElement);
            model.addNewTask(taskModel);
        }
        return model;
    }

    public SQLTaskModel getFirstTask() {
        return tasks.get(0);
    }

    private void addNewTask(SQLTaskModel taskModel) {
        taskModel.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                setChanged();
                notifyObservers();
            }

        });
        tasks.add(taskModel);
    }

    static class SQLTaskModel extends Observable {

        public String dsName = "";
        public List<SQLQueryModel> queries = new ArrayList<SQLQueryModel>();

        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }

        public void deleteQuery(int index) {
            queries.remove(index);
            notifyObservers();
        }

        public void addQuery() {
            queries.add(new SQLQueryModel());
            notifyObservers();
        }

        public void deleteQueryParameter(int index, boolean result, int paramIndex) {
            SQLQueryModel queryModel = queries.get(index);
            if (result) {
                queryModel.results.remove(paramIndex);
            } else {
                queryModel.params.remove(paramIndex);
            }
            notifyObservers();
        }

        public void addQueryParameter(int index, boolean result) {
            SQLQueryModel queryModel = queries.get(index);
            if (result) {
                queryModel.results.add(new SQLQueryParameterModel(result));
            } else {
                queryModel.params.add(new SQLQueryParameterModel(result));
            }
            notifyObservers();
        }

        public void moveUpQueryParameter(int index, boolean result, int paramIndex) {
            SQLQueryModel queryModel = queries.get(index);
            if (result) {
                Collections.swap(queryModel.results, paramIndex - 1, paramIndex);
            } else {
                Collections.swap(queryModel.params, paramIndex - 1, paramIndex);
            }
            notifyObservers();
        }

        public void serialize(Document document, Element parent) throws Exception {
            Element taskElement = document.createElement("task");
            taskElement.setAttribute("datasource", dsName);
            parent.appendChild(taskElement);

            Element queriesElement = document.createElement("queries");
            for (SQLQueryModel model : queries) {
                model.serialize(document, queriesElement);
            }
            taskElement.appendChild(queriesElement);
        }

        public static SQLTaskModel deserialize(Element element) throws Exception {
            SQLTaskModel model = new SQLTaskModel();
            model.dsName = element.getAttribute("datasource");
            Element queriesElement = (Element) element.getElementsByTagName("queries").item(0);
            NodeList queryElements = queriesElement.getElementsByTagName("query");
            for (int i = 0; i < queryElements.getLength(); i++) {
                SQLQueryModel queryModel = SQLQueryModel.deserialize((Element) queryElements.item(i));
                model.queries.add(queryModel);
            }
            return model;
        }
    }

    static class SQLQueryModel {

        public String query = "";
        public List<SQLQueryParameterModel> params = new ArrayList<SQLQueryParameterModel>();
        public List<SQLQueryParameterModel> results = new ArrayList<SQLQueryParameterModel>();

        public void serialize(Document document, Element parent) {
            Element queryElement = document.createElement("query");
            queryElement.setAttribute("sql", query);
            parent.appendChild(queryElement);
            for (SQLQueryParameterModel model : params) {
                model.serialize(document, queryElement);
            }
            for (SQLQueryParameterModel model : results) {
                model.serialize(document, queryElement);
            }
        }

        public static SQLQueryModel deserialize(Element element) throws Exception {
            SQLQueryModel model = new SQLQueryModel();
            model.query = element.getAttribute("sql");
            NodeList childs = element.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                if (child instanceof Element) {
                    SQLQueryParameterModel parameterModel = SQLQueryParameterModel.deserialize((Element) child);
                    if (parameterModel.result) {
                        model.results.add(parameterModel);
                    } else {
                        model.params.add(parameterModel);
                    }
                }
            }
            return model;
        }
    }

    static class SQLQueryParameterModel {

        public boolean result;
        public boolean swimlaneVar;
        public String varName = "";

        public SQLQueryParameterModel() {
        }

        public SQLQueryParameterModel(boolean result) {
            this.result = result;
        }

        public void serialize(Document document, Element parent) {
            String elementName;
            if (result) {
                elementName = swimlaneVar ? "swimlane-result" : "result";
            } else {
                elementName = swimlaneVar ? "swimlane-param" : "param";
            }
            Element paramElement = document.createElement(elementName);
            paramElement.setAttribute("var", varName);
            if (swimlaneVar) {
                paramElement.setAttribute("field", "code");
            }
            parent.appendChild(paramElement);
        }

        public static SQLQueryParameterModel deserialize(Element element) throws Exception {
            SQLQueryParameterModel model = new SQLQueryParameterModel();
            String elementName = element.getNodeName();
            if ("swimlane-result".equals(elementName)) {
                model.result = true;
                model.swimlaneVar = true;
            }
            if ("result".equals(elementName)) {
                model.result = true;
                model.swimlaneVar = false;
            }
            if ("swimlane-param".equals(elementName)) {
                model.result = false;
                model.swimlaneVar = true;
            }
            if ("param".equals(elementName)) {
                model.result = false;
                model.swimlaneVar = false;
            }
            model.varName = element.getAttribute("var");
            return model;
        }

    }

}