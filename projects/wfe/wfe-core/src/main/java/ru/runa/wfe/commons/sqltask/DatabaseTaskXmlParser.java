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
package ru.runa.wfe.commons.sqltask;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.runa.wfe.commons.xml.PathEntityResolver;
import ru.runa.wfe.commons.xml.SimpleErrorHandler;
import ru.runa.wfe.commons.xml.XMLHelper;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Created on 01.04.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class DatabaseTaskXmlParser {
    private static final String TASK_ELEMENT_NAME = "task";

    private static final String DATASOURCE_ATTRIBUTE_NAME = "datasource";

    private static final String SQL_ATTRIBUTE_NAME = "sql";

    private static final String VARIABLE_ATTRIBUTE_NAME = "var";

    private static final String QUERY_ELEMENT_NAME = "query";

    private static final String QUERIES_ELEMENT_NAME = "queries";

    private static final String PROCEDURE_ELEMENT_NAME = "procedure";

    private static final String PARAMETER_ELEMENT_NAME = "param";

    private static final String SWIMLANE_PARAMETER_ELEMENT_NAME = "swimlane-param";

    private static final String RESULT_ELEMENT_NAME = "result";

    private static final String SWIMLANE_RESULT_ELEMENT_NAME = "swimlane-result";

    private static final String FIELD_PARAMETER_ELEMENT_NAME = "field";

    private static final String XSD_PATH = "/database-tasks.xsd";

    private static final PathEntityResolver PATH_ENTITY_RESOLVER = new PathEntityResolver(XSD_PATH);

    /**
     * Parses DatabaseTaskHandler configuration
     * 
     * @param bytes
     *            xml configuration bytes
     * @param variableProvider
     *            process variables to substitute values in query string
     */
    public static DatabaseTask[] parse(byte[] bytes, IVariableProvider variableProvider) throws DatabaseTaskXmlParserException {
        try {
            InputStream is = new ByteArrayInputStream(bytes);
            Document document = XMLHelper.getDocument(is, PATH_ENTITY_RESOLVER, SimpleErrorHandler.getInstance());
            DatabaseTask[] databaseTasks = parseDatabaseTasks(document, variableProvider);
            return databaseTasks;
        } catch (Exception e) {
            throw new DatabaseTaskXmlParserException(e);
        }
    }

    private static DatabaseTask[] parseDatabaseTasks(Document document, IVariableProvider variableProvider) {
        NodeList taskElementList = document.getElementsByTagName(TASK_ELEMENT_NAME);
        DatabaseTask[] databaseTasks = new DatabaseTask[taskElementList.getLength()];
        for (int i = 0; i < databaseTasks.length; i++) {
            Element taskElement = (Element) taskElementList.item(i);
            String datasourceName = parseSQLQueryElement(taskElement.getAttribute(DATASOURCE_ATTRIBUTE_NAME), variableProvider);
            AbstractQuery[] abstractQueries = parseTaskQueries(taskElement, variableProvider);
            databaseTasks[i] = new DatabaseTask(datasourceName, abstractQueries);
        }
        return databaseTasks;
    }

    private static AbstractQuery[] parseTaskQueries(Element taskElement, IVariableProvider variableProvider) {
        Element queriesElement = (Element) taskElement.getElementsByTagName(QUERIES_ELEMENT_NAME).item(0);

        NodeList queryElementList = queriesElement.getChildNodes();
        List<AbstractQuery> queryList = new ArrayList<AbstractQuery>(queryElementList.getLength());
        for (int i = 0; i < queryElementList.getLength(); i++) {
            Node node = queryElementList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE
                    || !(QUERY_ELEMENT_NAME.equals(node.getNodeName()) || PROCEDURE_ELEMENT_NAME.equals(node.getNodeName()))) {
                continue;
            }
            Element queryElement = (Element) node;
            String sql = parseSQLQueryElement(queryElement.getAttribute(SQL_ATTRIBUTE_NAME), variableProvider);
            List<Parameter> parameterList = new ArrayList<Parameter>();
            List<Result> resultList = new ArrayList<Result>();
            parseQueryParameters(queryElement, parameterList, resultList);
            Parameter[] parameters = parameterList.toArray(new Parameter[parameterList.size()]);
            Result[] results = resultList.toArray(new Result[resultList.size()]);
            if (QUERY_ELEMENT_NAME.equals(queryElement.getLocalName())) {
                queryList.add(new Query(sql, parameters, results));
            } else if (PROCEDURE_ELEMENT_NAME.equals(queryElement.getLocalName())) {
                queryList.add(new StoredProcedureQuery(sql, parameters, results));
            }
        }
        return queryList.toArray(new AbstractQuery[queryList.size()]);
    }

    private static void parseQueryParameters(Element queryElement, List<Parameter> parameterList, List<Result> resultList) {
        NodeList queryNodes = queryElement.getChildNodes();
        for (int k = 0; k < queryNodes.getLength(); k++) {
            Node queryNode = queryNodes.item(k);
            if (queryNode.getNodeType() == Node.ELEMENT_NODE) {
                Element parameterElement = (Element) queryNode;
                String elementName = parameterElement.getNodeName();
                String variableName = parameterElement.getAttribute(VARIABLE_ATTRIBUTE_NAME);
                String fieldName = parameterElement.getAttribute(FIELD_PARAMETER_ELEMENT_NAME);
                if (PARAMETER_ELEMENT_NAME.equals(elementName)) {
                    parameterList.add(new Parameter(variableName, fieldName));
                } else if (SWIMLANE_PARAMETER_ELEMENT_NAME.equals(elementName)) {
                    parameterList.add(new SwimlaneParameter(variableName, fieldName));
                } else if (RESULT_ELEMENT_NAME.equals(elementName)) {
                    resultList.add(new Result(variableName, fieldName));
                } else if (SWIMLANE_RESULT_ELEMENT_NAME.equals(elementName)) {
                    resultList.add(new SwimlaneResult(variableName, fieldName));
                }
            }
        }
    }

    private static final Pattern pattern = Pattern.compile("\\$\\{(.*)\\}");

    public static String parseSQLQueryElement(String sqlElement, IVariableProvider variableProvider) {
        if (!sqlElement.startsWith("$")) {
            return sqlElement;
        }
        String sql = "";
        Matcher matcher = pattern.matcher(sqlElement);
        if (matcher.matches()) {
            String variableName = matcher.group(1);
            sql = variableProvider.getNotNull(String.class, variableName);
        }
        return sql;
    }

    public static void main(String[] args) {
        String s = DatabaseTaskXmlParser.parseSQLQueryElement("${command}", null);
        System.out.println(s);
    }

}
