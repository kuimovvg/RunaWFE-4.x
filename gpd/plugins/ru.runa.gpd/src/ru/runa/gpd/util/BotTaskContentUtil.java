package ru.runa.gpd.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.dom4j.Document;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import ru.runa.gpd.editor.BotTaskConfigHelper;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.lang.model.BotTask;

import com.google.common.base.Charsets;

public class BotTaskContentUtil {
    public static InputStream createBotStationInfo(String botStationName, String rmiAddress) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(botStationName);
        buffer.append("\n");
        buffer.append(rmiAddress);
        buffer.append("\n");
        return new ByteArrayInputStream(buffer.toString().getBytes(Charsets.UTF_8));
    }

    public static InputStream createBotTaskInfo(BotTask botTask, IFolder parentFolder) throws CoreException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(botTask.getClazz() != null ? botTask.getClazz() : "");
        buffer.append("\n");
        buffer.append(botTask.getConfig() != null ? botTask.getName() + ".conf" : "");
        if (botTask.getConfig() != null) {
            if (!BotTaskConfigHelper.isParamDefConfigEmpty(botTask.getParamDefConfig()) && botTask.getConfig() != null) {
                botTask.setConfig(BotTaskConfigHelper.createConfigWithFormalParam(botTask, botTask.getConfig()));
            }
            IFile configFile = parentFolder.getFile(botTask.getName() + ".conf");
            ByteArrayInputStream confStream = new ByteArrayInputStream(botTask.getConfig().getBytes(Charsets.UTF_8));
            if (configFile.exists()) {
                configFile.setContents(confStream, true, true, null);
            } else {
                configFile.create(confStream, true, null);
            }
        }
        return new ByteArrayInputStream(buffer.toString().getBytes(Charsets.UTF_8));
    }

    public static BotTask getBotTaskFromFile(IFile botTaskFile) throws CoreException, IOException {
        BotTask task = new BotTask();
        BufferedReader reader = new BufferedReader(new InputStreamReader(botTaskFile.getContents(), Charsets.UTF_8));
        String clazz = reader.readLine();
        String conf = reader.readLine();
        reader.close();
        task.setName(botTaskFile.getName());
        task.setClazz(clazz);
        task.setDelegationClassName(clazz);
        if (conf != null) {
            IFile confFile = ((IFolder) botTaskFile.getParent()).getFile(conf);
            String fileConfig = IOUtils.readStream(confFile.getContents());
            if (BotTaskConfigHelper.isConfigParamInPlugin(fileConfig)) {
                Document document = XmlUtil.parseWithoutValidation(fileConfig);
                task.setParamDefConfig(ParamDefConfig.parse(document));
                task.setConfig(fileConfig);
                task.setDelegationConfiguration(fileConfig);
            } else if (BotTaskConfigHelper.isConfigParamInFile(fileConfig)) {
                ParamDefConfig paramDefConfig = BotTaskConfigHelper.getParamDefConfig(fileConfig);
                task.setParamDefConfig(paramDefConfig);
                String config = BotTaskConfigHelper.getConfigFromBotTaskConfig(fileConfig);
                task.setConfig(config);
                task.setDelegationConfiguration(config);
            } else {
                task.setParamDefConfig(BotTaskConfigHelper.createEmptyParamDefConfig());
                task.setConfig(fileConfig);
                task.setDelegationConfiguration(fileConfig);
            }
        } else {
            ParamDefConfig paramDefConfig = BotTaskConfigHelper.createEmptyParamDefConfig();
            task.setParamDefConfig(paramDefConfig);
        }
        return task;
    }
}
