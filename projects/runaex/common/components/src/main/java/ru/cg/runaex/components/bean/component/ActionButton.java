package ru.cg.runaex.components.bean.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.part.Reference;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.GroovyScriptSyntax;

/**
 * @author urmancheev
 */
public class ActionButton extends Component implements ComponentWithCustomValidation {
  private static final long serialVersionUID = 2272871521395891094L;

  private static final int NAME = 0;
  private static final int ACTION = 1;
  private static final int LINK_INFO = 2;
  private static final int NEXT_TASK = 3;
  private static final int TABLE_ID = 4;
  private static final int GROOVY_SCRIPT = 5;
  private static final int VISIBILITY_RULE = 6;
  private static final int WIDTH = 7;

  private Action action;
  private LinkInfo linkInfo;
  private Integer width;

  @Override
  public int getParametersNumber() {
    return 8;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    action = getActionByCode(getActionStr());
    if (shouldHaveLinkInfo())
      linkInfo = parseLinkInfo();

    if (getParameter(WIDTH) != null)
      try {
        width = Integer.valueOf(getParameter(WIDTH));
      }
      catch (NumberFormatException ex) {
      }
  }

  @NotNull
  public String getName() {
    return getParameter(NAME);
  }

  public String getActionStr() {
    return getParameter(ACTION);
  }

  @NotNull
  public Action getAction() {
    ensureFullyInitialized();
    return action;
  }

  @Valid
  public LinkInfo getLinkInfo() {
    ensureFullyInitialized();
    return linkInfo;
  }

  public String getNextTask() {
    return getParameter(NEXT_TASK);
  }

  public String getTableId() {
    return getParameter(TABLE_ID);
  }

  public Integer getWidth() {
    ensureFullyInitialized();
    return width;
  }

  @GroovyScriptSyntax
  public String getGroovyScript() {
    return getGroovyScriptParameter(GROOVY_SCRIPT);
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  @Override
  public List<String> customValidate() {

    List<String> constraintCodes = new ArrayList<String>(1);
    if (getParameter(WIDTH) != null)
      try {
        Integer.valueOf(getParameter(WIDTH));
      }
      catch (NumberFormatException ex) {
        constraintCodes.add("ActionButton.widthInvalidSyntax");
      }
    Action action = getAction();
    if (action == null)
      return constraintCodes;

    boolean shouldHaveLinkInfo = shouldHaveLinkInfo();
    if (shouldHaveLinkInfo && getParameter(LINK_INFO) == null)
      constraintCodes.add("ActionButton.saveAndLinkNullLinkInfo");
    else if (shouldHaveLinkInfo && !isLinkInfoSyntaxValid())
      constraintCodes.add("ActionButton.linkInfoInvalidSyntax");

    return constraintCodes;
  }

  private boolean isLinkInfoSyntaxValid() {
    String[] parts = getParameter(LINK_INFO).split("::");
    return parts.length == 2;
  }

  public boolean shouldHaveLinkInfo() {
    return action == Action.SAVE_AND_LINK;
  }

  public static Action getActionByCode(String code) {
    if ("save".equals(code))
      return Action.SAVE;
    if ("delete".equals(code))
      return Action.DELETE;
    if ("find".equals(code))
      return Action.FIND;
    if ("link".equals(code))
      return Action.LINK;
    if ("unlink".equals(code))
      return Action.UNLINK;
    if ("cancel".equals(code))
      return Action.CANCEL;
    if ("saveandlink".equals(code))
      return Action.SAVE_AND_LINK;
    return null;
  }

  public LinkInfo parseLinkInfo() {
    LinkInfo linkInfo = null;

    String linkInfoStr = getParameter(LINK_INFO);
    if (linkInfoStr != null) {
      String[] parts = linkInfoStr.split("::");

      TableReference dependentTableReference = parseTableReference(parts[0]);
      TableReference mainTableReference = parseTableReference(parts[1]);
      linkInfo = new LinkInfo(dependentTableReference, mainTableReference);
    }

    return linkInfo;
  }

  public enum Action {
    SAVE,
    DELETE,
    FIND,
    LINK,
    UNLINK,
    CANCEL,
    SAVE_AND_LINK
  }

  public static class LinkInfo implements Serializable {
    private static final long serialVersionUID = -5190786923296112072L;

    @Valid
    @NotNull
    private TableReference dependentTableReference;
    @Valid
    @NotNull
    private TableReference mainTableReference;

    public LinkInfo(TableReference dependentTableReference, TableReference mainTableReference) {
      this.dependentTableReference = dependentTableReference;
      this.mainTableReference = mainTableReference;
    }

    public Reference getDependentTableReference() {
      return dependentTableReference;
    }

    public Reference getMainTableReference() {
      return mainTableReference;
    }
  }
}
