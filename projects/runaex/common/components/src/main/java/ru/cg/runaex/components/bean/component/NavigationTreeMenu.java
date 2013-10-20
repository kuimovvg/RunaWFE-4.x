package ru.cg.runaex.components.bean.component;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.parser.NavigationTreeMenuParser;
import ru.cg.runaex.components.validation.annotation.NavigationTreeMenuNode;

/**
 * @author urmancheev
 */
public class NavigationTreeMenu extends Component {
  private static final long serialVersionUID = -1018855192844608367L;

  private static final int TITLE = 0;
  private static final int NODES = 1;
  private static final int VISIBILITY_RULE = 2;

  private List<Node> nodes;

  @Override
  public int getParametersNumber() {
    return 3;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    nodes = NavigationTreeMenuParser.parseNodes(getNodesStr());
  }

  @NotNull
  public String getTitle() {
    return getParameter(TITLE);
  }

  @NotNull
  private String getNodesStr() {
    return getParameter(NODES);
  }

  @Valid
  public List<Node> getNodes() {
    ensureFullyInitialized();
    return nodes;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  @NavigationTreeMenuNode
  public static class Node implements Serializable {
    private static final long serialVersionUID = 631121183804672516L;

    @NotNull
    private String name;
    private String parent;
    private String task;
    private int termCount;
    private List<Node> childNodes = new LinkedList<Node>();

    public Node(String name, String parent, String task, int termCount) {
      this.name = name;
      this.parent = parent;
      this.task = task;
      this.termCount = termCount;
    }

    public String getName() {
      return name;
    }

    public String getParent() {
      return parent;
    }

    public String getTask() {
      return task;
    }

    public int getTermCount() {
      return termCount;
    }

    public List<Node> getChildNodes() {
      return childNodes;
    }
  }

}
