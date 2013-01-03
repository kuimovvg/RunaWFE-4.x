package ru.runa.gpd.bot;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.util.BotTaskContentUtil;
import ru.runa.gpd.util.ProjectFinder;

public class BotTaskExportCommand extends BotExportCommand {
    public BotTaskExportCommand(IResource exportResource, OutputStream outputStream) {
        super(exportResource, outputStream);
    }

    @Override
    protected IFolder getBotFolder() {
        return (IFolder) exportResource.getParent();
    }

    @Override
    protected List<BotTask> getBotTaskForExport(IFolder botFolder) {
        List<IFile> botTaskFiles = ProjectFinder.getBotTaskFiles(botFolder);
        List<BotTask> botTaskForExport = new ArrayList<BotTask>();
        for (IFile botTaskFile : botTaskFiles) {
            if (exportResource.getName().equals(botTaskFile.getName())) {
                BotTask task = BotTaskContentUtil.getBotTaskFromFile(botTaskFile);
                botTaskForExport.add(task);
                break;
            }
        }
        return botTaskForExport;
    }

    @Override
    protected void writeConfigurationFiles(IFolder botFolder, ZipOutputStream zipStream) throws CoreException, IOException {
        for (IResource resource : botFolder.members()) {
            if (resource instanceof IFile && resource.getName().equals(exportResource.getName() + ".conf")) {
                write(zipStream, new ZipEntry(resource.getName()), (IFile) resource);
            }
        }
    }
}
