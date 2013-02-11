package ru.runa.gpd.extension.orgfunction;

import java.util.HashMap;
import java.util.Map;

public class SwimlaneGUIConfiguration {
	private Map<String, String> SWIMLANE_EDITOR_PATH_MAP = new HashMap<String, String>();
	
	public void putSwimlanePath(String swimlaneName, String newPath) {
		SWIMLANE_EDITOR_PATH_MAP.put(swimlaneName, newPath);
	}

	public void removeSwimlanePath(String swimlaneName) {
		SWIMLANE_EDITOR_PATH_MAP.remove(swimlaneName);
	}

	public Map<String, String> getSwimlanePaths() {
		return SWIMLANE_EDITOR_PATH_MAP;
	}

	public String getEditorPath(String swimlaneName) {
        if (!SWIMLANE_EDITOR_PATH_MAP.containsKey(swimlaneName)) {
            // not initialized yet, open manual group
            SWIMLANE_EDITOR_PATH_MAP.put(swimlaneName, "SwimlaneElement.ManualLabel");
        }
		return SWIMLANE_EDITOR_PATH_MAP.get(swimlaneName);
	}
}
