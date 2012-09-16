package ru.runa.common.web;

/**
  * Object identity type.
  * May be present in web request to specify type of object, described by id parameter.
  *
  * @author Konstantinov Aleksey 04.12.2011
  */
public enum IdentityType {
    /**
     * Id is belongs to task instance. 
     */
    TASK,

    /**
     * Id is belongs to process instance.
     */
    PROCESS_INSTANCE
}
