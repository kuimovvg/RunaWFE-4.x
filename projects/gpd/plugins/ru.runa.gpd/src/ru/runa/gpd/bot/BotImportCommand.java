package ru.runa.gpd.bot;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.util.BotXmlUtil;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;

import com.google.common.io.ByteStreams;

public class BotImportCommand extends BotSyncCommand {
    protected InputStream inputStream;
    protected String botName;
    protected String botStationName;

    public BotImportCommand(InputStream inputStream, String botName, String botStationName) {
        this.inputStream = inputStream;
        this.botName = botName;
        this.botStationName = botStationName;
    }

    @Override
    protected void execute(IProgressMonitor progressMonitor) throws InvocationTargetException {
        try {
            ZipInputStream botZin = new ZipInputStream(inputStream);
            Map<String, byte[]> files = new HashMap<String, byte[]>();
            ZipEntry botEntry;
            while ((botEntry = botZin.getNextEntry()) != null) {
                byte[] bytes = ByteStreams.toByteArray(botZin);
                files.put(botEntry.getName(), bytes);
            }
            if (files.get("script.xml") == null) {
                throw new IOException("Incorrect bot archive");
            }
            //create bot
            IPath path = new Path(botStationName).append("/src/botstation/").append(botName.replaceAll(".bot", ""));
            IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
            if (!folder.exists()) {
                folder.create(true, true, null);
            }
            InputStream script = new ByteArrayInputStream(files.get("script.xml"));
            List<BotTask> botTasks = BotXmlUtil.getBotTasksFromScript(script);
            for (BotTask botTask : botTasks) {
                if (botTask.getConfig() != null && files.get(botTask.getConfig()) == null) {
                    botTask.setConfig(null);
                }
                IFile file = folder.getFile(botTask.getName());
                IOUtils.createFile(file);
                WorkspaceOperations.saveBotTask(file, botTask);
            }
            for (String fileName : files.keySet()) {
                if (!"script.xml".equals(fileName)) {
                    IFile gpdFile = folder.getFile(fileName);
                    if (gpdFile.exists()) {
                        gpdFile.setContents(new ByteArrayInputStream(files.get(fileName)), true, true, null);
                    } else {
                        gpdFile.create(new ByteArrayInputStream(files.get(fileName)), true, null);
                    }
                }
            }
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
}
