package ru.cg.runaex.database.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import ru.cg.runaex.components.ContextVariable;
import ru.cg.runaex.components.UnicodeSymbols;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.database.bean.transport.*;
import ru.cg.runaex.database.dao.util.JdbcTemplateProvider;
import ru.cg.runaex.database.dao.util.QueryConverter;
import ru.cg.runaex.database.dao.util.QueryHelper;
import ru.cg.runaex.database.dao.util.ResultSetHelper;
import ru.cg.runaex.database.exception.DataAccessCommonException;
import ru.cg.runaex.database.util.GsonUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * sabirov
 */
public class BaseDaoImpl implements BaseDao {
  private static final Log logger = LogFactory.getLog(BaseDaoImpl.class);

  private JdbcTemplateProvider templateProvider;

  class FilterValue {
    private Object value;
    private ClassType classType;

    FilterValue(Object value, ClassType classType) {
      this.value = value;
      this.classType = classType;
    }

    public Object getValue() {
      return value;
    }

    public ClassType getClassType() {
      return classType;
    }
  }

  public void setTemplateProvider(JdbcTemplateProvider templateProvider) {
    this.templateProvider = templateProvider;
  }

  /**
   * Set value to transportData from rs by field
   *
   * @param transportData -
   * @param field         -
   * @param rs            -
   * @param isPk          -
   * @return transportData
   * @throws SQLException
   */
  private TransportData setValue(TransportData transportData, String field, ResultSet rs, boolean isPk) throws SQLException {
    field = field.trim();
    Data data = new Data();
    logger.debug("field - " + field);
    data.setField(field);
    data.setPk(isPk);
    /**
     * parse filed because may me [field;schema.table.field]  ps schema.table.field - references
     */
    String[] tmp = field.split(";");
    String originalField = field;
    if (tmp.length >= 2) {
      originalField = tmp[0].trim();
    }
    Object value = ResultSetHelper.getValue(rs, originalField);
    Serializable serializable = (Serializable) value;
    logger.debug("(Serializable) rs.getObject(" + originalField + "): " + serializable);
    data.setValue(serializable);
    if (value != null) {
      logger.debug("(rs.getObject(" + originalField + ")).getClass(): " + value.getClass().getSimpleName());
      data.setValueClass(value.getClass().getSimpleName());
    }
    logger.debug("data - " + data);
    transportData.add(data);
    logger.debug("transportData.add(data)");
    return transportData;
  }

  private Object getCorrectQueryValue(Object searchByQueryValue) {
    if (searchByQueryValue instanceof String && !((String) searchByQueryValue).isEmpty()) {
      String tmp = (String) searchByQueryValue;
      tmp = tmp.replace("'", "''");
      tmp = tmp.replace("*", "%");
      searchByQueryValue = tmp;
    }
    return searchByQueryValue;
  }

  private boolean isSearchOnFullEquals(Object searchByQueryValue) {
    return !(searchByQueryValue instanceof String && (((String) searchByQueryValue).contains("%") || ((String) searchByQueryValue).contains("*")));
  }

  /**
   * Get data
   * <p/>
   * if object1 and object2 is not empty then need to get data from link table from to column-reference table(object1 and object2)
   *
   * @param schema        - schema name
   * @param table         - table name
   * @param fields        - fields
   * @param fromItems     - from items (first result)
   * @param limitItems    - limit items
   * @param sortName      - sort field name
   * @param sortOrder     - sort order
   * @param query         - query (regex)
   * @param queryField    - query by field name
   * @param transportData - filter transportData data
   * @return data
   * @throws DataAccessException
   */
  public TransportDataSet getData(Long processDefinitionId, final String schema, final String table, final String[] fields,
                                  final Integer fromItems, final Integer limitItems,
                                  final String sortName, final String sortOrder,
                                  final String query, final String queryField,
                                  final TransportData transportData,
                                  final String object1, final String object2, final String editTreeLinkColumns) throws DataAccessException {
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    try {
      AtomicReference<TransportDataSet> dataSet = new AtomicReference<TransportDataSet>(template.execute(new ConnectionCallback<TransportDataSet>() {

        @Override
        public TransportDataSet doInConnection(Connection connection) throws SQLException, DataAccessException {
          TransportDataSet transportDataSet;
          ResultSet rs = null;
          Statement st = null;
          ResultSet rs1 = null;
          PreparedStatement ps = null;
          ResultSet rs2 = null;
          PreparedStatement ps1 = null;

          try {
            String defSchema = schema;
            String defTable = table;
            String baseTableAs = defTable;
            String pkTableId = defTable + "_id";

            boolean addQueryForFilterField = false;
            boolean addFilter = false;
            StringBuilder sqlSelect = new StringBuilder();
            StringBuilder sqlFrom = new StringBuilder();
            StringBuilder sqlWhere = new StringBuilder();
            StringBuilder sqlSort = new StringBuilder();
            StringBuilder sqlReferencesFrom = new StringBuilder();
            String firstColumnName = null;
            String[] linkColumnsArray = null;
            boolean isFirst = true;
            int count = 0;

            if (object1 != null && !object1.isEmpty() &&
                object2 != null && !object2.isEmpty()) {
              String[] tmp = object2.split(UnicodeSymbols.POINT);
              String schema = tmp[0];
              String table = tmp[1];
              String refPkTableId = table + "_id";
              String refTableAs = QueryHelper.getWrapQuotes(table);
              sqlReferencesFrom.append(" inner join  ").append((QueryHelper.getWrapQuotes(defSchema)).trim()).append(".").
                  append((QueryHelper.getWrapQuotes(defTable)).trim()).append(" as ").append((QueryHelper.getWrapQuotes(baseTableAs)).trim()).
                  append(" on ").append((QueryHelper.getWrapQuotes(baseTableAs)).trim()).append(".").append((QueryHelper.getWrapQuotes(refPkTableId)).trim()).append(" = ").
                  append(refTableAs.trim()).append(".").append((QueryHelper.getWrapQuotes(refPkTableId)).trim());
              count++;

              /**
               * change info on object2 table
               */
              defSchema = schema;
              defTable = table;
              baseTableAs = table;
              pkTableId = refPkTableId;
            }

            String correctQuery = (String) getCorrectQueryValue(query);
            if (correctQuery != null && !correctQuery.isEmpty()) {
              correctQuery = "%" + correctQuery + "%";
              addQueryForFilterField = true;
            }

            /**
             * make metadata map
             */
            String sqlTable = QueryHelper.getWrapQuotes(defSchema) + "." + QueryHelper.getWrapQuotes(defTable);
            st = connection.createStatement();
            rs = st.executeQuery("SELECT * FROM " + sqlTable + " limit 0 OFFSET 0");
            ResultSetMetaData rsmd = rs.getMetaData();
            int numOfCol = rsmd.getColumnCount();
            Map<String, String> columnMap = new HashMap<String, String>();
            for (int i = 1; i <= numOfCol; i++) {
              logger.debug("columnName - " + rsmd.getColumnName(i));
              logger.debug("rsmd.getColumnTypeName - " + rsmd.getColumnTypeName(i));
              columnMap.put(rsmd.getColumnName(i), rsmd.getColumnTypeName(i));
            }

            if (editTreeLinkColumns != null && !editTreeLinkColumns.isEmpty()) {
              linkColumnsArray = editTreeLinkColumns.split(",");
            }

            for (String field : fields) {
              field = field.trim();
              if (isFirst) {
                if (addQueryForFilterField)
                  sqlWhere.append("(");
                firstColumnName = field;
              }
              else if (addQueryForFilterField)
                sqlWhere.append(" or ");

              if (sqlSelect.length() > 0) {
                sqlSelect.append(",");
              }

              //if references on other table
              String[] fs;
              String refTableAs = null;
              String refField = null;
              /**
               * parse filed because may me [field;schema.table.field]  ps schema.table.field - references
               */
              String[] tmp = field.split(";");
              String originalField = null;
              String fieldReferences = null;
              if (tmp.length >= 2) {
                originalField = tmp[0].trim();
                fieldReferences = tmp[1].trim();
              }
              else {
                fieldReferences = field;
              }
              if ((fs = fieldReferences.split(UnicodeSymbols.POINT)).length > 1) {
                String refSchema = fs[0];
                String refTable = fs[1];
                String refPkTableId = refTable + "_id";
                refField = fs[2];
                refTableAs = refTable + count;
                sqlReferencesFrom.append(" left outer join  ").append((QueryHelper.getWrapQuotes(refSchema)).trim()).append(".").
                    append((QueryHelper.getWrapQuotes(refTable)).trim()).append(" as ").append((QueryHelper.getWrapQuotes(refTableAs)).trim()).
                    append(" on ").append((QueryHelper.getWrapQuotes(baseTableAs)).trim()).append(".").append((QueryHelper.getWrapQuotes(originalField)).trim()).append(" = ").
                    append((QueryHelper.getWrapQuotes(refTableAs)).trim()).append(".").append((QueryHelper.getWrapQuotes(refPkTableId)).trim());

                if (isFirst) {
                  firstColumnName = refField;
                }
              }

              if (refField == null) {
                if (field.contains(":")) {
                  String[] tmpField = field.split(":");
                  int idx = 1;
                  if (tmpField.length > 3)
                    idx = 2;
                  sqlSelect.append(QueryHelper.getWrapQuotes(tmpField[idx])).append(".").append(QueryHelper.getWrapQuotes(tmpField[++idx])).append(" as ").append(QueryHelper.getWrapQuotes(field));
                }
                else
                  sqlSelect.append(QueryHelper.getWrapQuotes(baseTableAs)).append(".").append(QueryHelper.getWrapQuotes(field));

                if (addQueryForFilterField)
                  sqlWhere.append(QueryConverter.getQueryParam(connection, QueryHelper.getWrapQuotes(baseTableAs), QueryHelper.getWrapQuotes(field), columnMap.get(field)));
              }
              else {
                sqlSelect.append(QueryHelper.getWrapQuotes(refTableAs)).append(".").append(QueryHelper.getWrapQuotes(refField)).append(" as ").append(QueryHelper.getWrapQuotes(originalField));
                if (addQueryForFilterField)
                  sqlWhere.append(QueryConverter.getQueryParam(connection, QueryHelper.getWrapQuotes(refTableAs), QueryHelper.getWrapQuotes(refField), columnMap.get(refField)));
              }

              if (addQueryForFilterField)
                sqlWhere.append(" ilike ? ");

              isFirst = false;
              count++;
            }

            if (addQueryForFilterField)
              sqlWhere.append(")");

            //add pk object2 field
            sqlSelect.append(", ").append(QueryHelper.getWrapQuotes(baseTableAs)).append(".").append(QueryHelper.getWrapQuotes(pkTableId));

            sqlFrom.append(QueryHelper.getWrapQuotes(defSchema)).append(".").append(QueryHelper.getWrapQuotes(defTable)).append(" as ").append(QueryHelper.getWrapQuotes(baseTableAs));

            if (linkColumnsArray != null)
              for (String value : linkColumnsArray) {
                String[] linkPart = value.split(UnicodeSymbols.POINT);

                sqlSelect.append(", ").append(QueryHelper.getWrapQuotes(linkPart[1])).append(".").append(QueryHelper.getWrapQuotes(linkPart[2])).append(" as ").
                    append("\"").append(WfeRunaVariables.LINK_COLUMN_PREFIX).append(linkPart[2]).append("\"");

                sqlFrom.append(" left outer join  ").append((QueryHelper.getWrapQuotes(defSchema)).trim()).append(".").
                    append((QueryHelper.getWrapQuotes(linkPart[1])).trim()).append(" as ").append((QueryHelper.getWrapQuotes(linkPart[1])).trim()).
                    append(" on ").append((QueryHelper.getWrapQuotes(baseTableAs)).trim()).append(".").append((QueryHelper.getWrapQuotes(pkTableId)).trim()).append(" = ").
                    append((QueryHelper.getWrapQuotes(linkPart[1])).trim()).append(".").append((QueryHelper.getWrapQuotes(linkPart[2])).trim());
              }

            /**
             * filter
             */
            List<FilterValue> filterValues = new ArrayList<FilterValue>();
            if (transportData != null) {
              /**
               * add class to value data from db
               */
              boolean isFindParentId = false;
              for (String columnName : columnMap.keySet()) {
                ClassType classType = ClassType.valueOfBySimpleName(columnMap.get(columnName));
                transportData.setDataType(columnName, classType, true);

                if (columnName.contains("_parent_id")) {
                  isFindParentId = true;
                }
              }

              if (!isFindParentId) {
                Data dataParentId = transportData.getData(defTable + "_parent_id");
                transportData.getData().remove(dataParentId);
              }

              boolean isFilterFirst = true;
              for (Data data : transportData.getData()) {
                String alias = baseTableAs;
                if (data.getTable() != null && !data.getTable().isEmpty()) {
                  alias = data.getTable();
                }
                Object value = data.getValue();

                if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                  continue;
                }

                Object correctFilterValue = getCorrectQueryValue(value);
                filterValues.add(new FilterValue(correctFilterValue, data.getClassType()));
                if (!isFilterFirst || addQueryForFilterField) {
                  sqlWhere.append(" and ");
                }
                sqlWhere.append(QueryHelper.getWrapQuotes(alias)).append(".").append(QueryHelper.getWrapQuotes(data.getFieldWithoutComparison()));
                if (isSearchOnFullEquals(correctFilterValue)) {
                  if (data.isFieldComparisonIsNull()) {
                    sqlWhere.append(" is null ");
                  }
                  else if (data.isFieldComparisonLt()) {
                    sqlWhere.append(" < ? ");
                  }
                  else if (data.isFieldComparisonLe()) {
                    sqlWhere.append(" <= ? ");
                  }
                  else if (data.isFieldComparisonGt()) {
                    sqlWhere.append(" > ? ");
                  }
                  else if (data.isFieldComparisonGe()) {
                    sqlWhere.append(" >= ? ");
                  }
                  else if (data.isFieldComparisonEq()) {
                    sqlWhere.append(" = ? ");
                  }
                  else if (data.isFieldComparisonNe()) {
                    sqlWhere.append(" != ? ");
                  }
                  else if (data.isFieldComparisonNe()) {
                    sqlWhere.append(" != ? ");
                  }
                  else if (data.isFieldComparisonIn() && data.getValue() instanceof List) {
                    sqlWhere.append(" in (");
                    for (int i = 0; i < ((List) data.getValue()).size(); ++i) {
                      sqlWhere.append("?");
                      if (i < ((List) data.getValue()).size() - 1) {
                        sqlWhere.append(", ");
                      }
                    }
                    sqlWhere.append(")");
                  }
                  else if (data.isFieldComparisonNotIn() && data.getValue() instanceof List) {
                    sqlWhere.append(" not in (");
                    for (int i = 0; i < ((List) data.getValue()).size(); ++i) {
                      sqlWhere.append("?");
                      if (i < ((List) data.getValue()).size() - 1) {
                        sqlWhere.append(", ");
                      }
                    }
                    sqlWhere.append(")");
                  }
                  else {
                    sqlWhere.append(" = ? ");
                  }
                }
                else {
                  sqlWhere.append(" ilike ? ");
                }
                isFilterFirst = false;
                addFilter = true;
              }
            }


            /**
             * sort
             */
            boolean isUndefinedSortName = false;
            boolean isUndefinedSortOrder = false;
            if (sortName == null || sortName.isEmpty() || WfeRunaVariables.UNDEFINED.equals(sortName)) {
              isUndefinedSortName = true;
            }
            if (sortOrder == null || sortOrder.isEmpty() || WfeRunaVariables.UNDEFINED.equals(sortOrder)) {
              isUndefinedSortOrder = true;
            }
            sqlSort.append(" order by ").append(isUndefinedSortName ? QueryHelper.getWrapQuotes(firstColumnName) : QueryHelper.getWrapQuotes(sortName))
                .append(" ")
                .append(isUndefinedSortOrder ? "asc" : sortOrder);

            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT ").append(sqlSelect.toString()).append(" ").
                append(" FROM ").append(sqlFrom.toString()).append(" ").
                append(sqlReferencesFrom.toString()).append(" ").
                append(sqlWhere.length() > 0 ? " WHERE " : "").append(sqlWhere.toString()).append(" ").
                append(sqlSort.length() > 0 ? sqlSort.toString() : "");

            if (limitItems != null && fromItems != null) {
              sqlBuilder.append(" limit ? OFFSET ?");
            }

            String sql = sqlBuilder.toString();
            logger.debug("excecute sql - " + sql);

            /**
             * excecute sql query
             */
            ps = connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
            int indexParam = 1;
            if (addQueryForFilterField) {
              for (int i = 0; i < fields.length; i++) {
                logger.debug("addQuery - " + correctQuery);
                logger.debug("ps.setString(" + indexParam + ", " + correctQuery + ")");
                ps.setString(indexParam++, correctQuery);
              }
            }
            if (addFilter) {
              logger.debug("filterValues - " + filterValues);
              for (FilterValue filterValue : filterValues) {
                Object value = filterValue.getValue();
                if (value instanceof String) {
                  if (!"null".equals(value)) {
                    logger.debug("ps.setString(" + indexParam + ", " + value + ")");
                    ps.setString(indexParam++, (String) value);
                  }
                }
                else if (value instanceof BigDecimal) {
                  BigDecimal bDValue = (BigDecimal) value;
                  logger.debug("ps.setBigDecimal(" + indexParam + ", " + value + ")");
                  ps.setBigDecimal(indexParam++, bDValue);
                }
                else if (value instanceof Long) {
                  Long lValue = (Long) value;
                  if (Long.MIN_VALUE != lValue) {
                    logger.debug("ps.setLong(" + indexParam + ", " + value + ")");
                    ps.setLong(indexParam++, lValue);
                  }
                }
                else if (value instanceof Integer) {
                  Integer iValue = (Integer) value;
                  if (Integer.MIN_VALUE != iValue) {
                    logger.debug("ps.setInt(" + indexParam + ", " + value + ")");
                    ps.setInt(indexParam++, iValue);
                  }
                }
                else if (value instanceof Boolean) {
                  logger.debug("ps.setBoolean(" + indexParam + ", " + value + ")");
                  ps.setBoolean(indexParam++, (Boolean) value);
                }
                else if (value instanceof java.util.Date) {
                  switch (filterValue.getClassType()) {
                    case DATE:
                      Date sqlDate = new Date(((java.util.Date) value).getTime());
                      ps.setDate(indexParam++, sqlDate);
                      break;
                    case DATETIME:
                    case TIMESTAMP:
                    case TIMESTAMP_WITH_TIMEZONE:
                      Timestamp timestamp = new Timestamp(((java.util.Date) value).getTime());
                      logger.debug("ps.setTimestamp(" + indexParam + ", " + timestamp + ")");
                      ps.setTimestamp(indexParam++, timestamp);
                      break;
                  }
                }
                else if (value instanceof List) {
                  for (Object objFilterValue : (List) value) {
                    logger.debug("ps.setObject(" + indexParam + ", " + objFilterValue + ")");
                    switch (filterValue.getClassType()) {
                      case DATE:
                        Date sqlDate = new Date(((java.util.Date) objFilterValue).getTime());
                        ps.setDate(indexParam++, sqlDate);
                        break;
                      case DATETIME:
                      case TIMESTAMP:
                      case TIMESTAMP_WITH_TIMEZONE:
                        Timestamp timestamp = new Timestamp(((java.util.Date) objFilterValue).getTime());
                        ps.setTimestamp(indexParam++, timestamp);
                        break;
                      default:
                        ps.setObject(indexParam++, objFilterValue);
                        break;
                    }
                  }
                }
              }
            }

            if (limitItems != null && fromItems != null) {
              logger.debug("ps.setInt(" + indexParam + ", " + limitItems + ")");
              ps.setInt(indexParam++, limitItems);
              logger.debug("ps.setInt(" + indexParam + ", " + fromItems + ")");
              ps.setInt(indexParam, fromItems);
            }

            transportDataSet = new TransportDataSet();
            rs1 = ps.executeQuery();

            // Get result set meta data
            rsmd = rs1.getMetaData();
            int numColumns = rsmd.getColumnCount();

            // Get the column names; column indices start from 1
            for (int i = 1; i < numColumns + 1; i++) {
              String columnName = rsmd.getColumnName(i);

              // Get the name of the column's table name
              String tableName = rsmd.getTableName(i);
              logger.debug("columnName - \'" + columnName + "\'");
              logger.debug("tableName - \'" + tableName + "\'");
            }

            Integer i = 0;
            while (rs1.next()) {
              TransportData transportData = new TransportData();
              for (String field : fields) {
                setValue(transportData, field, rs1, false);
              }
              setValue(transportData, pkTableId, rs1, true);
              if (linkColumnsArray != null)
                for (String value : linkColumnsArray) {
                  String[] linkPart = value.split(UnicodeSymbols.POINT);
                  setValue(transportData, WfeRunaVariables.LINK_COLUMN_PREFIX + linkPart[2], rs1, false);
                }
              logger.debug("transportData - " + transportData);
              transportData.setNum(i);
              logger.debug("transportDataSet - " + transportDataSet);
              transportDataSet.add(transportData);
              ++i;
            }


            //get row counts
            sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT count(*) FROM ").append(sqlFrom.toString()).append(" ").
                append(sqlReferencesFrom.toString()).append(" ").
                append(sqlWhere.length() > 0 ? " WHERE " : "").append(sqlWhere.toString()).append(" ");

            sql = sqlBuilder.toString();
            logger.debug("excecute sql - " + sql);
            /**
             * excecute sql query
             */
            ps1 = connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
            indexParam = 1;
            if (addQueryForFilterField) {
              for (int k = 0; k < fields.length; k++) {
                logger.debug("addQuery - " + correctQuery);
                logger.debug("ps.setString(" + indexParam + ", " + correctQuery + ")");
                ps1.setString(indexParam++, correctQuery);
              }
            }
            if (addFilter) {
              logger.debug("filterValues - " + filterValues);
              for (FilterValue filterValue : filterValues) {
                Object value = filterValue.getValue();
                if (value instanceof String) {
                  if (!"null".equals(value)) {
                    logger.debug("ps1.setString(" + indexParam + ", " + value + ")");
                    ps1.setString(indexParam++, (String) value);
                  }
                }
                else if (value instanceof BigDecimal) {
                  BigDecimal bDValue = (BigDecimal) value;
                  logger.debug("ps1.setBigDecimal(" + indexParam + ", " + value + ")");
                  ps1.setBigDecimal(indexParam++, bDValue);
                }
                else if (value instanceof Long) {
                  Long lValue = (Long) value;
                  if (Long.MIN_VALUE != lValue) {
                    logger.debug("ps1.setLong(" + indexParam + ", " + value + ")");
                    ps1.setLong(indexParam++, lValue);
                  }
                }
                else if (value instanceof Integer) {
                  Integer iValue = (Integer) value;
                  if (Integer.MIN_VALUE != iValue) {
                    logger.debug("ps1.setInt(" + indexParam + ", " + value + ")");
                    ps1.setInt(indexParam++, iValue);
                  }
                }
                else if (value instanceof Boolean) {
                  logger.debug("ps1.setBoolean(" + indexParam + ", " + value + ")");
                  ps1.setBoolean(indexParam++, (Boolean) value);
                }
                else if (value instanceof java.util.Date) {
                  switch (filterValue.getClassType()) {
                    case DATE:
                      Date sqlDate = new Date(((java.util.Date) value).getTime());
                      ps1.setDate(indexParam++, sqlDate);
                      break;
                    case DATETIME:
                    case TIMESTAMP:
                    case TIMESTAMP_WITH_TIMEZONE:
                      Timestamp timestamp = new Timestamp(((java.util.Date) value).getTime());
                      logger.debug("ps1.setTimestamp(" + indexParam + ", " + timestamp + ")");
                      ps1.setTimestamp(indexParam++, timestamp);
                      break;
                  }
                }
                else if (value instanceof List) {
                  for (Object objFilterValue : (List) value) {
                    logger.debug("ps1.setObject(" + indexParam + ", " + value + ")");
                    switch (filterValue.getClassType()) {
                      case DATE:
                        Date sqlDate = new Date(((java.util.Date) objFilterValue).getTime());
                        ps1.setDate(indexParam++, sqlDate);
                        break;
                      case DATETIME:
                      case TIMESTAMP:
                      case TIMESTAMP_WITH_TIMEZONE:
                        Timestamp timestamp = new Timestamp(((java.util.Date) objFilterValue).getTime());
                        ps1.setTimestamp(indexParam++, timestamp);
                        break;
                      default:
                        ps1.setObject(indexParam++, objFilterValue);
                        break;
                    }
                  }
                }
              }
            }
            rs2 = ps1.executeQuery();
            while (rs2.next()) {
              count = rs2.getInt("count");
              logger.debug("row counts - " + count);
              transportDataSet.setRowCounts(count);
            }

          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (rs != null) {
              rs.close();
            }
            if (rs1 != null) {
              rs1.close();
            }
            if (rs2 != null) {
              rs2.close();
            }
            if (st != null) {
              st.close();
            }
            if (ps != null) {
              ps.close();
            }
            if (ps1 != null) {
              ps1.close();
            }

            if (connection != null) {
              connection.close();
            }
          }
          return transportDataSet;
        }
      }));
      return dataSet.get();
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  /**
   * Get data by id
   *
   * @param id         - id
   * @param schema     - schema name
   * @param table      - table name
   * @param field      - field
   * @param references - references table
   * @return data by id
   * @throws DataAccessException
   */
  public TransportData getDataById(Long processDefinitionId, final Long id, final String schema, final String table, final String field, final String references) throws DataAccessException {
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    try {
      TransportData data = template.execute(new ConnectionCallback<TransportData>() {

        @Override
        public TransportData doInConnection(Connection connection) throws SQLException, DataAccessException {
          TransportData transportData;
          ResultSet rs = null;
          PreparedStatement ps = null;
          try {
            String baseTableAs = QueryHelper.getWrapQuotes(table);
            String pkTableId = table + "_id";

            StringBuilder sqlBuilder = new StringBuilder();
            StringBuilder sqlSelect = new StringBuilder();
            StringBuilder sqlFrom = new StringBuilder();
            StringBuilder sqlWhere = new StringBuilder();
            StringBuilder sqlReferencesFrom = new StringBuilder();

            /**
             * if references on other table
             */
            String refTableAs = null;
            String refField = null;
            if (references != null && !references.isEmpty()) {
              String[] fs;
              if ((fs = references.split(UnicodeSymbols.POINT)).length > 1) {
                String refSchema = fs[0];
                String refTable = fs[1];
                refTableAs = refTable;
                refField = fs[2];
                String refPkTableId = refTable + "_id";
                sqlReferencesFrom.append(" left outer join  ").append(QueryHelper.wrapTableReference(refSchema, refTable)).append(" as ").append((QueryHelper.getWrapQuotes(refTableAs)).trim()).
                    append(" on ").append(baseTableAs.trim()).append(".").append(QueryHelper.getWrapQuotes(field)).append(" = ").
                    append(QueryHelper.wrapTableReference(refTableAs, refPkTableId));
              }
            }
            if (refField == null)
              sqlSelect.append(QueryHelper.getWrapQuotes(field));
            else
              sqlSelect.append(QueryHelper.getWrapQuotes(refTableAs)).append(".").append(QueryHelper.getWrapQuotes(refField)).append(" as ").append(QueryHelper.getWrapQuotes(field));

            sqlFrom.append(QueryHelper.getWrapQuotes(schema)).append(".").append(QueryHelper.getWrapQuotes(table)).append(" as ").append(baseTableAs);

            sqlWhere.append(baseTableAs).append(".").append(QueryHelper.getWrapQuotes(pkTableId)).append(" = ? ");

            sqlBuilder.append("SELECT ").append(sqlSelect.toString()).append(" ").
                append(" FROM ").append(sqlFrom.toString()).append(" ").
                append(sqlReferencesFrom.toString()).append(" ").
                append(sqlWhere.length() > 0 ? " WHERE " : "").append(sqlWhere.toString()).append(" ");

            String sql = sqlBuilder.toString();
            logger.debug("excecute sql - " + sql);
            /**
             * excecute sql query
             */
            ps = connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS);
            ps.setLong(1, id == null ? -1L : id);
            transportData = new TransportData();
            rs = ps.executeQuery();
            while (rs.next()) {
              Data data = new Data();
              data.setField(field);
              Object value = rs.getObject(field);
              Serializable serializable = (Serializable) value;
              data.setValue(serializable);
              if (value != null) {
                data.setValueClass(value.getClass().getSimpleName());
              }
              transportData.add(data);
            }
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (rs != null) {
              rs.close();
            }
            if (ps != null) {
              ps.close();
            }
            if (connection != null) {
              connection.close();
            }
          }
          return transportData;
        }
      });
      return data;
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }

//    String sql = "SELECT "+field+" FROM "+schema+"."+table+" WHERE "+table+"_id = ?";
//    DbBean dbBean = jdbcTemplate.queryForObject(sql,
//        new Object[]{id}, new CustomMapper());
//    return dbBean.getName();
  }

  /**
   * /**
   * Update value from editable tree grid.
   *
   * @throws DataAccessException
   */
  public Long updateTreeCell(final String col, final Long rowId, final Object value, Long processDefinitionId, final TransportData filter) throws DataAccessException {
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    try {
      Long id = template.execute(new ConnectionCallback<Long>() {

        @Override
        public Long doInConnection(Connection connection) throws SQLException, DataAccessException {
          Statement st = null;
          ResultSet rs = null;
          PreparedStatement ps = null;
          ResultSet generatedKeys = null;
          Long idRow = null;

          String schema = null;
          String table = null;
          String mainTable = null;
          String column = null;
          String linkColumn = null;
          try {
            String[] tmp = col.split(":");
            schema = tmp[0];

            if (tmp.length > 3) {
              mainTable = tmp[1];
              table = tmp[2];
              column = tmp[3];
              linkColumn = tmp[4];
            }
            else {
              mainTable = tmp[1];
              column = tmp[2];
            }

            int autoGeneratedKeys = Statement.NO_GENERATED_KEYS;
            StringBuilder sqlBuilder = new StringBuilder();
            StringBuilder subQuery = new StringBuilder();
            idRow = rowId;

            /**
             * update cell in main table
             */
            String sql = null;
            if (table == null) {
              sql = "SELECT * FROM " + QueryHelper.getWrapQuotes(schema) + "." + QueryHelper.getWrapQuotes(mainTable) + " limit 0 OFFSET 0";
              sqlBuilder.append(" UPDATE ").append(QueryHelper.getWrapQuotes(schema)).append(".").
                  append(QueryHelper.getWrapQuotes(mainTable)).append(" SET ").
                  append(QueryHelper.getWrapQuotes(column)).append("=?").
                  append(" WHERE ").append("\"").append(mainTable).append("_id").append("\"").append("=").append(idRow);
            }
            else {
              /**
               * update cell in depend table
               */
              sql = new StringBuilder().append("SELECT * ").append(" FROM ").append(QueryHelper.getWrapQuotes(schema)).append(".").append(QueryHelper.getWrapQuotes(table)).append(" as ").
                  append(QueryHelper.getWrapQuotes(table)).append(" INNER JOIN ").append(QueryHelper.getWrapQuotes(schema)).append(".").
                  append(QueryHelper.getWrapQuotes(mainTable)).append(" as ").append(QueryHelper.getWrapQuotes(mainTable)).append(" on ").
                  append(QueryHelper.getWrapQuotes(mainTable)).append(".").append("\"").append(mainTable).append("_id").append("\"").
                  append(" = ").append(QueryHelper.getWrapQuotes(table)).append(".").append(QueryHelper.getWrapQuotes(linkColumn)).append(" limit 0 OFFSET 0").toString();

              sqlBuilder.append(" UPDATE ").append(QueryHelper.getWrapQuotes(schema)).append(".").
                  append(QueryHelper.getWrapQuotes(table)).append(" SET ").
                  append(QueryHelper.getWrapQuotes(column)).append("=?").
                  append(" WHERE ").append("\"").append(table).append("_id").append("\"").append(" = ");
              subQuery.append("SELECT ").append("\"").append(table).append("_id").append("\"").
                  append(" FROM ").append(QueryHelper.getWrapQuotes(schema)).append(".").append(QueryHelper.getWrapQuotes(table)).append(" as ").
                  append(QueryHelper.getWrapQuotes(table)).append(" INNER JOIN ").append(QueryHelper.getWrapQuotes(schema)).append(".").
                  append(QueryHelper.getWrapQuotes(mainTable)).append(" as ").append(QueryHelper.getWrapQuotes(mainTable)).append(" on ").
                  append(QueryHelper.getWrapQuotes(mainTable)).append(".").append("\"").append(mainTable).append("_id").append("\"").
                  append(" = ").append(QueryHelper.getWrapQuotes(table)).append(".").append(QueryHelper.getWrapQuotes(linkColumn)).append(" AND ").
                  append(QueryHelper.getWrapQuotes(mainTable)).append(".").append("\"").append(mainTable).append("_id").append("\"").
                  append(" = ").append(idRow).append("");
            }

            logger.debug("excecute sql - " + sql);
            Data data = new Data();

            /**
             * excecute sql query
             */
            st = connection.createStatement();
            rs = st.executeQuery(sql);
            Map<String, String> columnMap = new HashMap<String, String>();
            ResultSetMetaData rsmd = rs.getMetaData();
            int numOfCol = rsmd.getColumnCount();
            for (int i = 1; i <= numOfCol; i++) {
              String columnName = rsmd.getColumnName(i);
              if (columnName.equals(column)) {
                ClassType classType = ClassType.valueOfBySimpleName(rsmd.getColumnTypeName(i));
                data.setField(column);
                data.setValueClass(classType.getSimpleName());
                data.setValue(value);
              }
              columnMap.put(rsmd.getColumnName(i), rsmd.getColumnTypeName(i));
            }

            StringBuilder sqlWhere = new StringBuilder();
            List<FilterValue> filterValues = new ArrayList<FilterValue>();
            if (filter != null) {
              for (String columnName : columnMap.keySet()) {
                ClassType classType = ClassType.valueOfBySimpleName(columnMap.get(columnName));
                filter.setDataType(columnName, classType, true);
              }
              boolean isFilterFirst = true;
              for (Data filterData : filter.getData()) {
                String alias = mainTable;
                if (filterData.getTable() != null && !filterData.getTable().isEmpty()) {
                  alias = filterData.getTable();
                }
                Object value = filterData.getValue();

                if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                  continue;
                }

                Object correctFilterValue = getCorrectQueryValue(value);
                filterValues.add(new FilterValue(correctFilterValue, filterData.getClassType()));
                if (!isFilterFirst) {
                  sqlWhere.append(" and ");
                }
                sqlWhere.append(QueryHelper.getWrapQuotes(alias)).append(".").append(QueryHelper.getWrapQuotes(filterData.getFieldWithoutComparison()));
                if (isSearchOnFullEquals(correctFilterValue)) {
                  if (filterData.isFieldComparisonIsNull()) {
                    sqlWhere.append(" is null ");
                  }
                  else if (filterData.isFieldComparisonLt()) {
                    sqlWhere.append(" < ? ");
                  }
                  else if (filterData.isFieldComparisonLe()) {
                    sqlWhere.append(" <= ? ");
                  }
                  else if (filterData.isFieldComparisonGt()) {
                    sqlWhere.append(" > ? ");
                  }
                  else if (filterData.isFieldComparisonGe()) {
                    sqlWhere.append(" >= ? ");
                  }
                  else if (filterData.isFieldComparisonEq()) {
                    sqlWhere.append(" = ? ");
                  }
                  else if (filterData.isFieldComparisonNe()) {
                    sqlWhere.append(" != ? ");
                  }
                  else if (filterData.isFieldComparisonNe()) {
                    sqlWhere.append(" != ? ");
                  }
                  else if (filterData.isFieldComparisonIn() && filterData.getValue() instanceof List) {
                    sqlWhere.append(" in (");
                    for (int i = 0; i < ((List) filterData.getValue()).size(); ++i) {
                      sqlWhere.append("?");
                      if (i < ((List) filterData.getValue()).size() - 1) {
                        sqlWhere.append(", ");
                      }
                    }
                    sqlWhere.append(")");
                  }
                  else if (filterData.isFieldComparisonNotIn() && filterData.getValue() instanceof List) {
                    sqlWhere.append(" not in (");
                    for (int i = 0; i < ((List) filterData.getValue()).size(); ++i) {
                      sqlWhere.append("?");
                      if (i < ((List) filterData.getValue()).size() - 1) {
                        sqlWhere.append(", ");
                      }
                    }
                    sqlWhere.append(")");
                  }
                  else {
                    sqlWhere.append(" = ? ");
                  }
                }
                else {
                  sqlWhere.append(" ilike ? ");
                }
                isFilterFirst = false;
              }
            }
            if (subQuery.length() > 0) {
              sqlBuilder.append("(").append(subQuery);
              if (sqlWhere.length() > 0) {
                sqlBuilder.append(" WHERE ").append(sqlWhere);
              }
              sqlBuilder.append(")");
            }
            else if (sqlWhere.length() > 0) {
              sqlBuilder.append(" AND ").append(sqlWhere);
            }

            sql = sqlBuilder.toString();
            logger.debug("execute sql - " + sql);
            /**
             * excecute sql query
             */
            ps = connection.prepareStatement(sql, autoGeneratedKeys);
            int index = 1;
            Object value = data.getValue();

            if (value != null)
              logger.debug("value class - " + value.getClass());
            else
              logger.debug("value class - \"\"");
            if (value instanceof Long) {
              logger.debug("setLong(index, (Long) value)");
              ps.setLong(index, (Long) value);
            }
            else if (value instanceof Integer) {
              logger.debug("setInt(index, (Integer) value)");
              ps.setInt(index, (Integer) value);
            }
            else if (value instanceof BigDecimal) {
              logger.debug("setBigDecimal(index, (Double) value)");
              ps.setBigDecimal(index, (BigDecimal) value);
            }
            else if (value instanceof String) {
              logger.debug("setString(index, (String) value)");
              ps.setString(index, (String) value);
            }
            else if (value instanceof Boolean) {
              logger.debug("setBoolean(index, (Boolean) value)");
              ps.setBoolean(index, (Boolean) value);
            }
            else if (value instanceof byte[]) {
              logger.debug("setBytes(index, (byte[]) value)");
              ps.setBytes(index, (byte[]) value);
            }
            else if (value instanceof java.util.Date) {
              switch (data.getClassType()) {
                case DATE:
                  Date sqlDate = new Date(((java.util.Date) value).getTime());
                  ps.setDate(index, sqlDate);
                  break;
                case DATETIME:
                case TIMESTAMP:
                case TIMESTAMP_WITH_TIMEZONE:
                  Timestamp timestamp = new Timestamp(((java.util.Date) value).getTime());
                  logger.debug("setTimestamp(index, timestamp)");
                  ps.setTimestamp(index, timestamp);
                  break;
              }
            }
            else if (value instanceof Collection) {
              logger.debug("setArray(index, value)");
              ps.setArray(index, connection.createArrayOf("varchar", ((Collection) value).toArray()));
            }
            else if (value == null) {
              switch (ClassType.valueOfBySimpleName(data.getValueClass())) {
                case LONG:
                case INT8:
                  logger.debug("setNull(index, java.sql.Types.BIGINT)");
                  ps.setNull(index, java.sql.Types.BIGINT);
                  break;
                case BIG_DECIMAL:
                  logger.debug("setNull(index, java.sql.Types.NUMERIC)");
                  ps.setNull(index, Types.NUMERIC);
                  break;
                case STRING:
                case VARCHAR:
                  logger.debug("setNull(index, java.sql.Types.VARCHAR)");
                  ps.setNull(index, java.sql.Types.VARCHAR);
                  break;
                case TIMESTAMP:
                case TIMESTAMP_WITH_TIMEZONE:
                  logger.debug("setNull(index, Types.TIMESTAMP)");
                  ps.setNull(index, Types.TIMESTAMP);
                  break;
                case DATE:
                  logger.debug("setNull(index, Types.DATE)");
                  ps.setNull(index, Types.DATE);
                  break;
                case BYTEA:
                case BYTEARRAY:
                  logger.debug("setNull(index, Types.BINARY)");
                  ps.setNull(index, Types.BINARY);
                  break;
              }
            }

            if (sqlWhere.length() > 0) {
              index++;
              logger.debug("filterValues - " + filterValues);
              for (FilterValue filterValue : filterValues) {
                value = filterValue.getValue();
                if (value instanceof String) {
                  if (!"null".equals(value)) {
                    logger.debug("ps.setString(" + index + ", " + value + ")");
                    ps.setString(index++, (String) value);
                  }
                }
                else if (value instanceof BigDecimal) {
                  BigDecimal bDValue = (BigDecimal) value;
                  logger.debug("ps.setBigDecimal(" + index + ", " + value + ")");
                  ps.setBigDecimal(index++, bDValue);
                }
                else if (value instanceof Long) {
                  Long lValue = (Long) value;
                  if (Long.MIN_VALUE != lValue) {
                    logger.debug("ps.setLong(" + index + ", " + value + ")");
                    ps.setLong(index++, lValue);
                  }
                }
                else if (value instanceof Integer) {
                  Integer iValue = (Integer) value;
                  if (Integer.MIN_VALUE != iValue) {
                    logger.debug("ps.setInt(" + index + ", " + value + ")");
                    ps.setInt(index++, iValue);
                  }
                }
                else if (value instanceof Boolean) {
                  logger.debug("ps.setBoolean(" + index + ", " + value + ")");
                  ps.setBoolean(index++, (Boolean) value);
                }
                else if (value instanceof java.util.Date) {
                  switch (filterValue.getClassType()) {
                    case DATE:
                      Date sqlDate = new Date(((java.util.Date) value).getTime());
                      ps.setDate(index++, sqlDate);
                      break;
                    case DATETIME:
                    case TIMESTAMP:
                    case TIMESTAMP_WITH_TIMEZONE:
                      Timestamp timestamp = new Timestamp(((java.util.Date) value).getTime());
                      logger.debug("ps.setTimestamp(" + index + ", " + timestamp + ")");
                      ps.setTimestamp(index++, timestamp);
                      break;
                  }
                }
                else if (value instanceof List) {
                  for (Object objFilterValue : (List) value) {
                    logger.debug("ps.setObject(" + index + ", " + value + ")");
                    ps.setObject(index++, objFilterValue);
                  }
                }
              }
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
              throw new SQLException("Creating object failed, no rows affected.");
            }
            generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
              idRow = generatedKeys.getLong(1);
            }
          }
          catch (PSQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
          }
          finally {
            if (rs != null) {
              rs.close();
            }
            if (generatedKeys != null) {
              generatedKeys.close();
            }
            if (st != null) {
              st.close();
            }
            if (ps != null) {
              ps.close();
            }

            if (connection != null) {
              connection.close();
            }
          }
          return idRow;
        }
      });
      return id;
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  /**
   * Delete data.
   * Need fill bean SaveTransportData. Not need set the bean Data.classType
   *
   * @param transportData - data
   * @return count affected rows
   * @throws DataAccessException
   */
  public Integer deleteData(Long processDefinitionId, final SaveTransportData transportData) throws DataAccessException {
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    Integer affectedRows1;
    try {
      affectedRows1 = template.execute(new ConnectionCallback<Integer>() {

        @Override
        public Integer doInConnection(Connection connection) throws SQLException, DataAccessException {
          int affectedRows;
          PreparedStatement ps = null;
          try {
            String pkTableId = transportData.getTable() + "_id";
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("DELETE FROM ").
                append(QueryHelper.wrapTableReference(transportData.getSchema(), transportData.getTable())).
                append(" WHERE ").
                append(QueryHelper.getWrapQuotes(pkTableId)).append(transportData.getId() != null ? "=?" : "in (" + QueryHelper.preparePlaceHolders(transportData.getIds().size()) + ")");

            String sql = sqlBuilder.toString();
            logger.debug("excecute sql - " + sql);
            /**
             * excecute sql query
             */
            ps = connection.prepareStatement(sql);
            Long id = transportData.getId();
            List<Long> ids = transportData.getIds();
            if (id != null) {
              logger.debug("delete id - " + id);
              ps.setLong(1, id);
            }
            else {
              logger.debug("delete ids - " + StringUtils.join(ids.toArray(), ", "));
              QueryHelper.setArrayValues(1, ps, ids);
            }
            affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
              logger.debug("Delete affected rows - " + affectedRows);
            }

            /**
             * save ObjectInfo
             */
            ObjectInfo objectInfo = new ObjectInfo();
            objectInfo.setSchema(transportData.getSchema());
            objectInfo.setTable(transportData.getTable());
            objectInfo.setId(null);
            saveOrUpdateObjectInfo(transportData.getProcessInstanceId(), objectInfo);
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (ps != null) {
              ps.close();
            }

            if (connection != null) {
              connection.close();
            }
          }

          return affectedRows;
        }
      });
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    return affectedRows1;
  }

  /**
   * Save link data.
   * Need fill bean SaveTransportData. Not need set the bean Data.classType
   *
   * @param saveTransportData - data
   * @return id if create object data
   * @throws DataAccessException
   */
  public Long linkData(Long processDefinitionId, final SaveTransportData saveTransportData) throws DataAccessException {
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    try {
      Long id = template.execute(new ConnectionCallback<Long>() {

        @Override
        public Long doInConnection(Connection connection) throws SQLException, DataAccessException {
          Long linkId = null;
          ResultSet generatedKeys = null;
          PreparedStatement ps = null;
          int autoGeneratedKeys = Statement.RETURN_GENERATED_KEYS;
          try {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append(" insert into ").append(QueryHelper.wrapTableReference(saveTransportData.getSchema(), saveTransportData.getTable())).append("(");
            boolean isFirst = true;
            StringBuilder values = new StringBuilder();
            for (Data data : saveTransportData.getData()) {
              if (!isFirst) {
                sqlBuilder.append(", ");
                values.append(", ");
              }
              sqlBuilder.append(QueryHelper.getWrapQuotes(data.getField()));
              values.append("?");
              isFirst = false;
            }

            sqlBuilder.append(") values (").append(values.toString()).append(")");

            String sql = sqlBuilder.toString();
            logger.debug("excecute sql - " + sql);
            /**
             * excecute sql query
             */
            ps = connection.prepareStatement(sql, autoGeneratedKeys);
            int index = 1;
            for (Data data : saveTransportData.getData()) {
              Object value = data.getValue();
              logger.debug("field - " + data.getField());
              logger.debug("data.getValueClass() - " + data.getValueClass());
              logger.debug("value - " + value);
              if (value != null && value instanceof String)
                ps.setLong(index, Long.valueOf((String) value));
              index++;
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
              throw new SQLException("Creating object failed, no rows affected.");
            }
            generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
              linkId = generatedKeys.getLong(1);
            }

            /**
             * save ObjectInfo
             */
            ObjectInfo objectInfo = new ObjectInfo();
            objectInfo.setSchema(saveTransportData.getSchema());
            objectInfo.setTable(saveTransportData.getTable());
            objectInfo.setId(linkId);
            ObjectInfo base = new ObjectInfo();
            base.setSchema("");
            base.setTable("");
            base.setId(null);
            objectInfo.setBase(base);
            ObjectInfo attachable = new ObjectInfo();
            attachable.setSchema("");
            attachable.setTable("");
            attachable.setId(null);
            objectInfo.setAttachable(attachable);
            saveOrUpdateObjectInfo(saveTransportData.getProcessInstanceId(), objectInfo);
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (generatedKeys != null) {
              generatedKeys.close();
            }
            if (ps != null) {
              ps.close();
            }

            if (connection != null) {
              connection.close();
            }
          }
          return linkId;
        }
      });
      return id;
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  /**
   * Delete data.
   * Need fill bean SaveTransportData. Not need set the bean Data.classType
   *
   * @param transportData - data
   * @return count affected rows
   * @throws DataAccessException
   */
  public Integer unlinkData(Long processDefinitionId, final SaveTransportData transportData) throws DataAccessException {
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    try {
      Integer affectedRows1 = template.execute(new ConnectionCallback<Integer>() {

        @Override
        public Integer doInConnection(Connection connection) throws SQLException, DataAccessException {
          PreparedStatement ps = null;
          int affectedRows;
          try {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("DELETE FROM ").
                append(QueryHelper.wrapTableReference(transportData.getSchema(), transportData.getTable())).append(" WHERE ");

            boolean isFirst = true;
            List<Data> list = transportData.getData();
            for (Data data : list) {
              if (!isFirst) {
                sqlBuilder.append(" and ");
              }
              sqlBuilder.append(QueryHelper.getWrapQuotes(data.getField())).append("=?");
              isFirst = false;
            }

            String sql = sqlBuilder.toString();
            logger.debug("excecute sql - " + sql);
            /**
             * excecute sql query
             */
            ps = connection.prepareStatement(sql);
            int index = 1;
            for (Data data : list) {
              String strId = (String) data.getValue();
              Long id = Long.valueOf(strId.trim());
              logger.debug("delete index-" + index + ", data - " + data.getField() + " id - " + id);
              ps.setLong(index, id);
              index++;
            }
            affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
              logger.debug("Delete affected rows - " + affectedRows);
            }

            /**
             * save ObjectInfo
             */
            ObjectInfo objectInfo = new ObjectInfo();
            objectInfo.setSchema(transportData.getSchema());
            objectInfo.setTable(transportData.getTable());
            objectInfo.setId(null);
            saveOrUpdateObjectInfo(transportData.getProcessInstanceId(), objectInfo);
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (ps != null) {
              ps.close();
            }
            if (connection != null) {
              connection.close();
            }
          }
          return affectedRows;
        }
      });
      return affectedRows1;
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }


  /**
   * Add variable
   *
   * @param variableName - variable id
   * @param jsonData     - json data
   * @throws DataAccessException
   */
  public void addVariableToDb(final Long processInstanceId, final String variableName, final String jsonData) throws DataAccessException {
    JdbcTemplate template = templateProvider.getMetadataTemplate();
    try {
      template.execute(new ConnectionCallback<Integer>() {

        @Override
        public Integer doInConnection(Connection connection) throws SQLException, DataAccessException {
          /**
           * Because delete is hard operations
           */
//          removeVariable(connection, variableName);
          try {
            Long id;
            id = getVariableId(connection, processInstanceId, variableName);
            if (id == null)
              return addVariable(connection, processInstanceId, variableName, jsonData);
            else
              return chgVariable(connection, id, processInstanceId, variableName, jsonData);
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (connection != null) {
              connection.close();
            }
          }
        }
      });
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }


  /**
   * Remove variable
   *
   * @param variableName - variable id
   * @throws DataAccessException
   */
  public void removeVariableFromDb(final String variableName) throws DataAccessException {
    JdbcTemplate template = templateProvider.getMetadataTemplate();
    try {
      template.execute(new ConnectionCallback<Integer>() {

        @Override
        public Integer doInConnection(Connection connection) throws SQLException, DataAccessException {
          return removeVariable(connection, variableName);
        }
      });
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  @Override
  public void removeVariableFromDb(final Long processInstanceId) throws DataAccessException {
    try {
      JdbcTemplate jdbcTemplate = templateProvider.getMetadataTemplate();
      jdbcTemplate.execute(new ConnectionCallback<Integer>() {

        @Override
        public Integer doInConnection(Connection connection) throws SQLException, DataAccessException {
          return removeVariable(connection, processInstanceId);
        }
      });
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  @Override
  public void removeVariableFromDb(final String variableName, final Long processInstanceId) {
    try {
      JdbcTemplate jdbcTemplate = templateProvider.getMetadataTemplate();
      jdbcTemplate.execute(new ConnectionCallback<Integer>() {

        @Override
        public Integer doInConnection(Connection connection) throws SQLException, DataAccessException {
          return removeVariable(connection, processInstanceId, variableName);
        }
      });
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  /**
   * Get variable
   *
   * @param variableName - variable id
   * @return json data
   * @throws DataAccessException
   */
  public String getVariableFromDb(final Long processInstanceId, final String variableName) throws DataAccessException {
    JdbcTemplate template = templateProvider.getMetadataTemplate();
    try {
      return template.execute(new ConnectionCallback<String>() {

        @Override
        public String doInConnection(Connection connection) throws SQLException, DataAccessException {
          return getVariable(connection, processInstanceId, variableName);
        }
      });
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  private Integer addVariable(Connection connection, Long processInstanceId, String variableName, String jsonData) throws DataAccessException, SQLException {
    int affectedRows = 0;
    PreparedStatement ps = null;
    try {
      StringBuilder sql = new StringBuilder("insert into ");
      sql.append(ContextVariable.SCHEMA).append(".").append(ContextVariable.TABLE).append("(");
      sql.append(ContextVariable.FIELD_PROCESS_INSTANCE_ID).append(",").append(ContextVariable.FIELD_NAME).append(",").append(ContextVariable.FIELD_VALUE).append(") ");
      sql.append(" values (?, ?, ?)");
      logger.debug("sql - " + sql.toString());
      ps = connection.prepareStatement(sql.toString());
      logger.debug("save");
      logger.debug("variableName - " + variableName);
      logger.debug("jsonData - " + jsonData);
      ps.setLong(1, processInstanceId);
      ps.setString(2, variableName);
      ps.setString(3, jsonData);
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        logger.debug("save affected rows - " + affectedRows);
      }
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new DataAccessCommonException(ex.getMessage(), ex);
    }
    finally {
      if (ps != null) {
        ps.close();
      }
    }
    return affectedRows;
  }

  private Integer chgVariable(Connection connection, Long id, Long processInstanceId, String variableName, String jsonData) throws DataAccessException, SQLException {
    PreparedStatement ps = null;
    int affectedRows;
    try {
      StringBuilder sql = new StringBuilder("update ");
      sql.append(ContextVariable.SCHEMA).append(".").append(ContextVariable.TABLE).append(" set \"");
      sql.append(ContextVariable.FIELD_NAME).append("\"=? ").append(", ").append(ContextVariable.FIELD_VALUE).append("=? ").append(", ").append(ContextVariable.FIELD_PROCESS_INSTANCE_ID).append("=? ");
      sql.append(" where ").append(ContextVariable.TABLE).append("_id=?");
      logger.debug("sql - " + sql.toString());
      ps = connection.prepareStatement(sql.toString());
      logger.debug("save");
      logger.debug("id - " + id);
      logger.debug("variableName - " + variableName);
      logger.debug("jsonData - " + jsonData);
      ps.setString(1, variableName);
      ps.setString(2, jsonData);
      ps.setLong(3, processInstanceId);
      ps.setLong(4, id);
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        logger.debug("save affected rows - " + affectedRows);
      }
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new DataAccessCommonException(ex.getMessage(), ex);
    }
    finally {
      if (ps != null) {
        ps.close();
      }
    }
    return affectedRows;
  }

  private Integer removeVariable(Connection connection, String variableName) throws DataAccessException, SQLException {
    PreparedStatement ps = null;
    int affectedRows;
    try {
      StringBuilder sql = new StringBuilder();
      sql.append("DELETE FROM ");
      sql.append(ContextVariable.SCHEMA).append(".").append(ContextVariable.TABLE);
      sql.append(" WHERE \"").append(ContextVariable.FIELD_NAME).append("\"=?");
      logger.debug("sql - " + sql.toString());
      ps = connection.prepareStatement(sql.toString());
      logger.debug("delete name - " + variableName);
      ps.setString(1, variableName);
      affectedRows = ps.executeUpdate();
      if (affectedRows == 0) {
        logger.debug("Delete affected rows - " + affectedRows);
      }
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new DataAccessCommonException(ex.getMessage(), ex);
    }
    finally {
      if (ps != null) {
        ps.close();
      }

      if (connection != null) {
        connection.close();
      }
    }
    return affectedRows;
  }

  private Integer removeVariable(Connection connection, Long processInstanceId) throws DataAccessException, SQLException {
    PreparedStatement ps = null;
    int affectedRows;
    try {
      StringBuilder sql = new StringBuilder();
      sql.append("DELETE FROM ");
      sql.append(ContextVariable.SCHEMA).append(".").append(ContextVariable.TABLE);
      sql.append(" WHERE \"").append(ContextVariable.FIELD_PROCESS_INSTANCE_ID).append("\"=?");
      logger.debug("sql - " + sql.toString());
      ps = connection.prepareStatement(sql.toString());
      logger.debug("delete processInstanceId - " + processInstanceId);
      ps.setLong(1, processInstanceId);
      affectedRows = ps.executeUpdate();
      logger.debug("Delete affected rows - " + affectedRows);
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new DataAccessCommonException(ex.getMessage(), ex);
    }
    finally {
      if (ps != null) {
        ps.close();
      }

      if (connection != null) {
        connection.close();
      }
    }
    return affectedRows;
  }

  private Integer removeVariable(Connection connection, Long processInstanceId, String variableName) throws DataAccessException, SQLException {
    PreparedStatement ps = null;
    int affectedRows;
    try {
      StringBuilder sql = new StringBuilder();
      sql.append("DELETE FROM ");
      sql.append(ContextVariable.SCHEMA).append(".").append(ContextVariable.TABLE);
      sql.append(" WHERE \"").append(ContextVariable.FIELD_PROCESS_INSTANCE_ID).append("\"=?");
      sql.append(" AND \"").append(ContextVariable.FIELD_NAME).append("\"=?");
      logger.debug("sql - " + sql.toString());
      ps = connection.prepareStatement(sql.toString());
      logger.debug("delete processInstanceId - " + processInstanceId + " variablename " + variableName);
      ps.setLong(1, processInstanceId);
      ps.setString(2, variableName);
      affectedRows = ps.executeUpdate();
      logger.debug("Delete affected rows - " + affectedRows);
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new DataAccessCommonException(ex.getMessage(), ex);
    }
    finally {
      if (ps != null) {
        ps.close();
      }

      if (connection != null) {
        connection.close();
      }
    }
    return affectedRows;
  }

  private String getVariable(Connection connection, final Long processInstanceId, String variableName) throws DataAccessException, SQLException {
    String jsonData;
    ResultSet rs = null;
    PreparedStatement ps = null;
    try {
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT value FROM ");
      sql.append(ContextVariable.SCHEMA).append(".").append(ContextVariable.TABLE);
      sql.append(" WHERE \"").append(ContextVariable.FIELD_NAME).append("\"=?").append(" and \"").append(ContextVariable.FIELD_PROCESS_INSTANCE_ID).append("\"=?").append("  limit 1");
      logger.debug("sql - " + sql.toString());
      ps = connection.prepareStatement(sql.toString());
      logger.debug("get name - " + variableName);
      ps.setString(1, variableName);
      ps.setLong(2, processInstanceId);
      rs = ps.executeQuery();
      jsonData = "";
      while (rs.next()) {
        jsonData = rs.getString("value");
      }
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new DataAccessCommonException(ex.getMessage(), ex);
    }
    finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
      if (connection != null) {
        connection.close();
      }
    }
    return jsonData;
  }

  private Long getVariableId(Connection connection, final Long processInstanceId, String variableName) throws DataAccessException, SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    Long id;
    try {
      String strId = ContextVariable.TABLE + "_id";
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT ");
      sql.append(strId);
      sql.append(" FROM ");
      sql.append(ContextVariable.SCHEMA).append(".").append(ContextVariable.TABLE);
      sql.append(" WHERE \"").append(ContextVariable.FIELD_NAME).append("\"=?").append(" and \"").append(ContextVariable.FIELD_PROCESS_INSTANCE_ID).append("\"=?").append("  limit 1");
      logger.debug("sql - " + sql.toString());
      ps = connection.prepareStatement(sql.toString());
      logger.debug("get name - " + variableName);
      ps.setString(1, variableName);
      ps.setLong(2, processInstanceId);
      rs = ps.executeQuery();
      id = null;
      while (rs.next()) {
        id = rs.getLong(strId);
      }
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new DataAccessCommonException(ex.getMessage(), ex);
    }
    finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
    }
    return id;
  }


  /**
   * Get list object info
   *
   * @param processInstanceId - process instance id
   * @return list object info
   * @throws DataAccessException
   */
  public Map<Long, String> getObjectInfoFromDb(final Long processInstanceId) throws DataAccessException {
    JdbcTemplate template = templateProvider.getMetadataTemplate();
    try {
      return template.execute(new ConnectionCallback<Map<Long, String>>() {

        @Override
        public Map<Long, String> doInConnection(Connection connection) throws SQLException, DataAccessException {
          Map<Long, String> variables;
          try {
            variables = getVariables(connection, String.valueOf(processInstanceId));
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (connection != null) {
              connection.close();
            }
          }
          return variables;
        }
      });
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  private Map<Long, String> getVariables(Connection connection, String variableName) throws DataAccessException, SQLException {
    Map<Long, String> map;
    ResultSet rs = null;
    PreparedStatement ps = null;
    try {
      StringBuilder sql = new StringBuilder();
      sql.append("SELECT \"").append(ContextVariable.TABLE).append("_id\", value FROM ");
      sql.append(ContextVariable.SCHEMA).append(".").append(ContextVariable.TABLE);
      sql.append(" WHERE \"").append(ContextVariable.FIELD_NAME).append("\"=?");
      logger.debug("sql - " + sql.toString());
      ps = connection.prepareStatement(sql.toString());
      logger.debug("get name - " + variableName);
      ps.setString(1, variableName);
      rs = ps.executeQuery();
      map = new HashMap<Long, String>();
      while (rs.next()) {
        Long contextVariableId = rs.getLong(ContextVariable.TABLE + "_id");
        String value = rs.getString("value");
        map.put(contextVariableId, value);
      }
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      throw new DataAccessCommonException(ex.getMessage(), ex);
    }
    finally {
      if (rs != null) {
        rs.close();
      }
      if (ps != null) {
        ps.close();
      }
    }
    return map;
  }

  /**
   * Save or update objectInfo
   *
   * @param processInstanceId - process instance id
   * @param objectInfo        - objectInfo
   * @throws DataAccessException
   */
  public void saveOrUpdateObjectInfo(final Long processInstanceId, final ObjectInfo objectInfo) throws DataAccessException {
    JdbcTemplate template = templateProvider.getMetadataTemplate();
    try {
      template.execute(new ConnectionCallback<Void>() {

        @Override
        public Void doInConnection(Connection connection) throws SQLException, DataAccessException {
          try {
            Map<Long, String> map = getVariables(connection, String.valueOf(processInstanceId));
            Long findPId = null;
            ObjectInfo findObjectInfo = null;
            for (Long contextVariableId : map.keySet()) {
              ObjectInfo tmpObjectInfo = GsonUtil.getObjectFromJson(map.get(contextVariableId), ObjectInfo.class);
              if (!WfeRunaVariables.isEmpty(tmpObjectInfo.getSchema()) && tmpObjectInfo.getSchema().equals(objectInfo.getSchema()) &&
                  !WfeRunaVariables.isEmpty(tmpObjectInfo.getTable()) && tmpObjectInfo.getTable().equals(objectInfo.getTable())) {
                findPId = contextVariableId;
                findObjectInfo = tmpObjectInfo;
                break;
              }
            }
            if (findObjectInfo != null) {
              Map<Long, String> changeMap = new HashMap<Long, String>();
              changeMap.put(findPId, GsonUtil.toJson(objectInfo));
              chgObjectInfo(changeMap);
            }
            else {
              addObjectInfo(processInstanceId, objectInfo);
            }
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (connection != null) {
              connection.close();
            }
          }
          return null;
        }
      });
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  /**
   * Add objectInfo
   *
   * @param processInstanceId - process instance id
   * @param objectInfo        - objectInfo
   * @return true if is add
   * @throws DataAccessException
   */
  public boolean addObjectInfo(final Long processInstanceId, final ObjectInfo objectInfo) throws DataAccessException {
    JdbcTemplate template = templateProvider.getMetadataTemplate();
    try {
      boolean add = template.execute(new ConnectionCallback<Boolean>() {

        @Override
        public Boolean doInConnection(Connection connection) throws SQLException, DataAccessException {
          PreparedStatement ps = null;
          try {
            if (objectInfo.isEmpty()) {
              return false;
            }
            StringBuilder sql = new StringBuilder("insert into ");
            sql.append(ContextVariable.SCHEMA).append(".").append(ContextVariable.TABLE).append("(");
            sql.append(ContextVariable.FIELD_PROCESS_INSTANCE_ID).append(",").append(ContextVariable.FIELD_NAME).append(",").append(ContextVariable.FIELD_VALUE).append(") ");
            sql.append(" values (?, ?, ?)");
            logger.debug("sql - " + sql.toString());
            ps = connection.prepareStatement(sql.toString());
            logger.debug("save");
            String strProcessInstanceId = WfeRunaVariables.toString(processInstanceId);
            String strObjectInfo = GsonUtil.toJson(objectInfo);
            logger.debug("processInstanceId - " + strProcessInstanceId);
            logger.debug("objectInfo - " + strObjectInfo);
            ps.setLong(1, processInstanceId);
            ps.setString(2, strProcessInstanceId);
            ps.setString(3, strObjectInfo);
            ps.execute();
            return true;
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (ps != null) {
              ps.close();
            }
            if (connection != null) {
              connection.close();
            }
          }
        }
      });
      return add;
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  /**
   * Chg map objectInfo
   *
   * @param map - map Map<Long, String>  Long - primary id (context_variable)  String - json ObjectInfo
   * @throws DataAccessException
   */
  public Integer chgObjectInfo(final Map<Long, String> map) throws DataAccessException {
    JdbcTemplate template = templateProvider.getMetadataTemplate();
    try {
      Integer affectedRows = template.execute(new ConnectionCallback<Integer>() {

        @Override
        public Integer doInConnection(Connection connection) throws SQLException, DataAccessException {
          int totalAffectedRows = 0;
          PreparedStatement ps = null;
          try {
            for (Map.Entry<Long, String> entry : map.entrySet()) {
              Long contextVariableId = entry.getKey();
              String value = entry.getValue();

              StringBuilder sql = new StringBuilder("update ");
              sql.append(ContextVariable.SCHEMA).append(".").append(ContextVariable.TABLE).append(" set ");
              sql.append(ContextVariable.FIELD_VALUE).append("=? ");
              sql.append(" where ").append(ContextVariable.TABLE).append("_id=?");
              logger.debug("sql - " + sql.toString());
              ps = connection.prepareStatement(sql.toString());
              logger.debug("save");
              logger.debug("contextVariableId - " + contextVariableId);
              logger.debug("value - " + value);
              ps.setString(1, value);
              ps.setLong(2, contextVariableId);
              int affectedRows = ps.executeUpdate();
              logger.debug("save affected rows - " + affectedRows);
              totalAffectedRows = totalAffectedRows + affectedRows;
            }
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (ps != null) {
              ps.close();
            }
            if (connection != null) {
              connection.close();
            }
          }
          return totalAffectedRows;
        }
      });
      return affectedRows;
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  /**
   * Get data by field value
   *
   * @param schema - schema name
   * @param table  - table name
   * @param field  - field
   * @param value  - field value
   * @return first query result
   * @throws DataAccessException
   */
  public TransportData getIdByFieldValue(Long processDefinitionId, final String schema, final String table, final String field, final String value) throws DataAccessException {
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    try {
      TransportData data = template.execute(new ConnectionCallback<TransportData>() {

        @Override
        public TransportData doInConnection(Connection connection) throws SQLException, DataAccessException {
          PreparedStatement ps = null;
          ResultSet rs = null;
          TransportData transportData;
          try {
            StringBuilder sql = new StringBuilder();
            String idField = table.concat("_id");
            sql.append("select ").append(QueryHelper.getWrapQuotes(idField))
                .append(" from ").append(QueryHelper.wrapTableReference(schema, table))
                .append(" where ").append(QueryHelper.getWrapQuotes(field)).append(" = ? limit 1");

            logger.debug("sql.toString() - " + sql.toString());

            ps = connection.prepareStatement(sql.toString(), Statement.NO_GENERATED_KEYS);
            ps.setString(1, value);
            transportData = new TransportData();
            rs = ps.executeQuery();
            if (rs.next()) {
              Data data = new Data();
              data.setField(idField);
              Object value = rs.getObject(idField);
              Serializable serializable = (Serializable) value;
              data.setValue(serializable);
              if (value != null) {
                data.setValueClass(value.getClass().getSimpleName());
              }
              transportData.add(data);
            }
          }
          catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new DataAccessCommonException(ex.getMessage(), ex);
          }
          finally {
            if (rs != null) {
              rs.close();
            }
            if (ps != null) {
              ps.close();
            }
          }
          return transportData;
        }
      });
      return data;
    }
    catch (DataAccessException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
    catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }
  }

  @Override
  public List<String> getColumns(Long processDefinitionId, final String schema, final String table) {
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    String sql = "SELECT * FROM " + QueryHelper.wrapTableReference(schema, table) + " limit 0 OFFSET 0";
    return template.query(sql, new ResultSetExtractor<List<String>>() {
      @Override
      public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<String> result = new ArrayList<String>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int numOfCol = rsmd.getColumnCount();
        for (int i = 1; i <= numOfCol; i++) {
          String columnName = rsmd.getColumnName(i);
          result.add(columnName);
        }
        return result;
      }
    });
  }

  @Override
  public TransportData getDataById(Long processDefinitionId, String schema, String table, String[] columns, Long id) {
    return getDataById(processDefinitionId, schema, table, Arrays.asList(columns), id);
  }

  @Override
  public TransportData getDataById(Long processDefinitionId, final String schema, final String table, final Collection<String> columns, final Long id) throws DataAccessException {
    String pkTableId = table + "_id";

    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append("SELECT ");
    for (String column : columns) {
      sqlBuilder.append(QueryHelper.getWrapQuotes(column)).append(",");
    }
    sqlBuilder.replace(sqlBuilder.length() - 1, sqlBuilder.length(), " ");
    sqlBuilder.append("FROM ").append(QueryHelper.wrapTableReference(schema, table)).append(" ");
    sqlBuilder.append("WHERE ").append(QueryHelper.getWrapQuotes(pkTableId)).append("=?");

    String sql = sqlBuilder.toString();
    RowMapper<TransportData> rowMapper = new RowMapper<TransportData>() {
      @Override
      public TransportData mapRow(ResultSet resultSet, int i) throws SQLException {
        TransportData transportData = new TransportData();
        for (String column : columns) {
          Data data = new Data();
          data.setField(column);
          Object value = resultSet.getObject(column);
          Serializable serializable = (Serializable) value;
          data.setValue(serializable);
          if (value != null) {
            data.setValueClass(value.getClass().getSimpleName());
          }
          transportData.add(data);
        }
        return transportData;
      }
    };
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    final TransportData transportData = template.queryForObject(sql, new Object[] {id}, rowMapper);
    return transportData;
  }

  @Override
  public Long getSequenceNextValue(Long processDefinitionId, String schemaName, String sequenceName) {
    String sql = String.format("select nextval('%s.%s');", QueryHelper.getWrapQuotes(schemaName), QueryHelper.getWrapQuotes(sequenceName));
    JdbcTemplate template = templateProvider.getTemplate(processDefinitionId);
    return template.query(sql, new ResultSetExtractor<Long>() {
      @Override
      public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
        rs.next();
        return rs.getLong(1);
      }
    });
  }
}

