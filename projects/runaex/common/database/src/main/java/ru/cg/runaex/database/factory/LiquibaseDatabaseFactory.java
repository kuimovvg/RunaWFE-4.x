package ru.cg.runaex.database.factory;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;

/**
 * @author Петров А.
 */
public interface LiquibaseDatabaseFactory {

  Database createTargetDatabase(String schema) throws DatabaseException;

  Database createReferenceDatabase(String schema) throws DatabaseException;
}
