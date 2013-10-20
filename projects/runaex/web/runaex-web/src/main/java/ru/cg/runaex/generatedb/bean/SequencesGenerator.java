package ru.cg.runaex.generatedb.bean;

import ru.cg.runaex.database.bean.FtlComponent;
import ru.cg.runaex.components.bean.component.field.RecordNumberGenerator;

/**
 * @author korablev
 */
public class SequencesGenerator {
  public static Sequence createSequence(FtlComponent ftlComponent) throws RuntimeException {
    switch (ftlComponent.getComponentType()) {
      case RECORD_NUMBER_GENERATOR:
        RecordNumberGenerator recordNumberGenerator = ftlComponent.getComponent();
        return createSequence(recordNumberGenerator);
    }
    return null;
  }

  private static Sequence createSequence(RecordNumberGenerator source) {
    Sequence sequence = new Sequence();
    sequence.setSchema(new Schema(source.getSchema()));
    sequence.setTableName(source.getTable());
    sequence.setFieldName(source.getField());
    sequence.setSequenceName(source.getFormattedSequenceName());
    return sequence;
  }
}
