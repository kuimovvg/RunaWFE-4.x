package ru.cg.runaex.generatedb.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.cg.runaex.generatedb.bean.*;
import ru.cg.runaex.database.bean.FtlComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author korablev
 */
public final class DatabaseStructureGenerator {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseStructureGenerator.class);

  public static Database generateDatabaseStructure(List<FtlComponent> ftlComponents) {
    Database database = new Database();
    database.setTables(generateTables(ftlComponents));
    database.setSequenceList(createSequences(ftlComponents));
    return database;
  }

  private static List<Sequence> createSequences(List<FtlComponent> ftlComponents) {
    List<Sequence> sequenceList = new ArrayList<Sequence>();
    for (FtlComponent ftlComponent : ftlComponents) {
      Sequence createSequence = SequencesGenerator.createSequence(ftlComponent);
      if (createSequence != null)
        sequenceList.add(createSequence);
    }
    return sequenceList;
  }

  private static TableHashSet<Table> generateTables(List<FtlComponent> ftlComponents) {
    TableHashSet<Table> tables = new TableHashSet<Table>();
    for (FtlComponent ftlComponent : ftlComponents) {
      List<Table> createdTables = DatabaseTableGenerator.createTable(ftlComponent);
      if (createdTables != null) {
        for (Table table : createdTables) {
          if (table.isEmpty()) {
            logger.debug("-------------------------Table is empty------------------------");
            logger.debug(table.toString());
            continue;
          }

          if (!tables.contains(table)) {
            logger.debug("-------------------------Objects------------------------");
            logger.debug(table.toString());
            tables.add(table);
          }
          else {
            logger.debug("--------------------------------------------------------");
            logger.debug("Duplicate table " + table.toString());
            logger.debug("--------------------------------------------------------");
          }
        }
      }
    }
    return createReferenceTables(tables);
  }

  private static TableHashSet<Table> createReferenceTables(TableHashSet<Table> tables) {
    TableHashSet<Table> tablesWithReferenceTables = tables.clone();
    for (Table table : tables) {
      if (table.getFields() != null) {
        for (Field field : table.getFields()) {
          References references = field.getReferences();
          if (references == null)
            continue;
          /**
           * check contains table by schema, table name and field name
           */
          Table refTable = references.getRefTable();
          if (refTable != null && !tables.contains(refTable)) {
            logger.debug("-------------------------Objects  by references---------");
            logger.debug(refTable.toString());
            tablesWithReferenceTables.add(refTable);
          }
          else {
            //exists duplicate table
            logger.debug("--------------------------------------------------------");
            logger.debug("Duplicate by reference" + table.toString());
            logger.debug("--------------------------------------------------------");
          }
        }
      }
    }
    return tablesWithReferenceTables;
  }
}
