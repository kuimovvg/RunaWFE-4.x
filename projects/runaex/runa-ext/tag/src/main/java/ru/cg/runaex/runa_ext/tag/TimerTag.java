package ru.cg.runaex.runa_ext.tag;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import freemarker.template.TemplateModelException;
import org.apache.ecs.html.Div;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.Timer;

/**
 * @author Абдулин Ильдар
 */
public class TimerTag extends BaseFreemarkerTag<Timer> {
  private static final long serialVersionUID = 8032564302155418032L;

  @Override
  protected ComponentType getComponentType() {
    return ComponentType.TIMER;
  }

  @Override
  protected String executeToHtml(Timer component) throws TemplateModelException, AuthorizationException, AuthenticationException, TaskDoesNotExistException {
    initObjectInfo(component.getSchema(), component.getTable());

    Long selectedRowId = getSelectedRowId();
    Date dateValue = getValue(selectedRowId, component.getSchema(), component.getTable(), component.getDataColumn(), null, Date.class, null);

    StringBuilder html = new StringBuilder();
    if (dateValue != null) {
      String pattern = component.getPattern() == null ? "HH-MM-SS" : component.getPattern();

      Calendar c = new GregorianCalendar();
      c.setTime(dateValue);
      Div div = new Div();
      String fieldId = "timer_" + System.nanoTime();
      div.setID(fieldId);
      div.setClass("timer-tag");

      addObjectToJs("year", c.get(Calendar.YEAR));
      addObjectToJs("month", c.get(Calendar.MONTH));
      addObjectToJs("day", c.get(Calendar.DAY_OF_MONTH));
      addObjectToJs("hours", c.get(Calendar.HOUR_OF_DAY));
      addObjectToJs("minutes", c.get(Calendar.MINUTE));
      addObjectToJs("seconds", c.get(Calendar.SECOND));
      addObjectToJs("fieldId", fieldId);
      addObjectToJs("pattern", pattern);
      appendComponentJsReference(TimerTag, html);
      appendComponentCssReference(TimerTag, html);
      setJsTemplateName(TimerTag);
      html.append(div.toString());
    }

    return html.toString();
  }
}
