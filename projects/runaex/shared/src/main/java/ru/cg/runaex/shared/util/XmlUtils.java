package ru.cg.runaex.shared.util;

import java.io.InputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import ru.cg.runaex.shared.bean.project.xml.*;
import ru.cg.runaex.shared.bean.project.xml.Process;

/**
 * @author Петров А.
 */
public class XmlUtils {

  private static final XStream PROJECT_STRUCTURE_STREAM = new XStream(new DomDriver());

  static {
    PROJECT_STRUCTURE_STREAM.processAnnotations(Project.class);
    PROJECT_STRUCTURE_STREAM.processAnnotations(Category.class);
    PROJECT_STRUCTURE_STREAM.processAnnotations(Process.class);
    PROJECT_STRUCTURE_STREAM.processAnnotations(GroovyFunction.class);
    PROJECT_STRUCTURE_STREAM.processAnnotations(GroovyFunctionList.class);
  }

  public static String serializeProjectStructure(Project project) {
    return PROJECT_STRUCTURE_STREAM.toXML(project);
  }

  public static Project deserializeProjectStructure(String serializedProjectStructure) {
    return (Project) PROJECT_STRUCTURE_STREAM.fromXML(serializedProjectStructure);
  }

  public static Project deserializeProjectStructure(InputStream serializedProjectStructureStream) {
    return (Project) PROJECT_STRUCTURE_STREAM.fromXML(serializedProjectStructureStream);
  }

  public static String serializeFunctionList(GroovyFunctionList project) {
    return PROJECT_STRUCTURE_STREAM.toXML(project);
  }

  public static GroovyFunctionList deserializeFunctionList(String serializedProjectStructure) {
    return (GroovyFunctionList) PROJECT_STRUCTURE_STREAM.fromXML(serializedProjectStructure);
  }

  public static GroovyFunctionList deserializeFunctionList(InputStream serializedProjectStructureStream) {
    return (GroovyFunctionList) PROJECT_STRUCTURE_STREAM.fromXML(serializedProjectStructureStream);
  }
}
