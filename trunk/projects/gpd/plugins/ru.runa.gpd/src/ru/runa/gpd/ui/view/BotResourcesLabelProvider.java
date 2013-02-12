package ru.runa.gpd.ui.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.editor.BotTaskConfigHelper;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.util.BotTaskContentUtil;

public class BotResourcesLabelProvider extends ResourcesLabelProvider {
    @Override
    public Image getImage(Object element) {
        if (element instanceof IProject) {
            return SharedImages.getImage("icons/bot_station.gif");
        }
        if (element instanceof IFolder) {
            return SharedImages.getImage("icons/bot.gif");
        }
        if (element instanceof IFile) {
            try {
                BotTask task = BotTaskContentUtil.getBotTaskFromFile((IFile) element);
                if (!BotTaskConfigHelper.isParamDefConfigEmpty(task.getParamDefConfig())) {
                    return SharedImages.getImage("icons/bot_task_formal.gif");
                }
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("", e);
            }
        }
        return SharedImages.getImage("icons/bot_task.gif");
    }
}
