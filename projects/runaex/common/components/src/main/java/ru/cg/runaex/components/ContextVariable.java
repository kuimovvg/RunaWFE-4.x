/*
 * Copyright (c) 2012.
 *
 * Date: 9/3/2012
 *
 * @author Sabirov
 * Company Center
 */

package ru.cg.runaex.components;

/**
 * Config table
 */
public class ContextVariable {
  public static final String SCHEMA = "public";
  public static final String TABLE = "context_variable";
  public static final String FIELD_PROCESS_INSTANCE_ID = "process_instance_id";
  public static final String FIELD_NAME = "name";
  public static final String FIELD_VALUE = "value";
  public static final GenerateFieldType FIELD_NAME_TYPE = GenerateFieldType.VARCHAR;
  public static final GenerateFieldType FIELD_VALUE_TYPE = GenerateFieldType.VARCHAR;
  public static final Integer FIELD_NAME_LENGTH = 255;
  public static final Integer FIELD_VALUE_LENGTH = 4000;
}
