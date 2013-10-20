package ru.cg.runaex.components.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import ru.cg.runaex.components.bean.component.NavigationTreeMenu;

/**
 * @author urmancheev
 */
public final class NavigationTreeMenuParser {
  private static final Pattern NODE_SEPARATOR_PATTERN = Pattern.compile("(?<!\\\\);", Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.MULTILINE);
  private static final Pattern NODE_PARAMETER_SEPARATOR_PATTERN = Pattern.compile("(?<!\\\\),", Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.MULTILINE);

  public static List<NavigationTreeMenu.Node> parseNodes(String nodesStr) {
    if (nodesStr == null)
      return Collections.emptyList();

    String[] nodes = NODE_SEPARATOR_PATTERN.split(nodesStr, 0);
    List<NavigationTreeMenu.Node> nodeList = new ArrayList<NavigationTreeMenu.Node>(nodes.length);
    for (String node : nodes) {
      node = StringUtils.trimToNull(node);

      if (node != null) {
        String[] parts = NODE_PARAMETER_SEPARATOR_PATTERN.split(node, 0);
        String name = StringUtils.trimToNull(parts[0]);
        String parent = null;
        String task = null;
        int termCount = parts.length;

        if (termCount > 2) {
          parent = StringUtils.trimToNull(parts[1]);
          task = StringUtils.trimToNull(parts[2]);
        }
        else if (termCount == 2) {
          parent = StringUtils.trimToNull(parts[1]);
        }
        nodeList.add(new NavigationTreeMenu.Node(name, parent, task, termCount));
      }
    }
    return nodeList;
  }

  public static NavigationTreeMenu.Node buildTree(List<NavigationTreeMenu.Node> nodes) {
    NavigationTreeMenu.Node root = new NavigationTreeMenu.Node(null, null, null, 0);

    for (NavigationTreeMenu.Node node : nodes) {
      if (node.getParent() == null || node.getParent().isEmpty()) {
        root.getChildNodes().add(node);
      }
      else {
        for (NavigationTreeMenu.Node n : nodes) {
          if (node.getParent().equals(n.getName())) {
            n.getChildNodes().add(node);
            break;
          }
        }
      }
    }

    return root;
  }
}
