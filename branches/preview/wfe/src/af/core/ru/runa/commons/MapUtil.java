package ru.runa.commons;

import java.util.List;
import java.util.Map;

import ru.runa.bpm.graph.def.Node;

import com.google.common.collect.Maps;

public class MapUtil {

    // TODO move to appr
    public static Map<String, Node> getMap(List<Node> nodes) {
        Map<String, Node> result = Maps.newHashMap();
        for (Node node : nodes) {
            result.put(node.getName(), node);
        }
        return result;
    }
}
