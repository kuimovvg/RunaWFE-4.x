package ru.runa.gpd.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.BotTaskConfigHelper;
import ru.runa.gpd.handler.action.ParamDefConfig;
import ru.runa.gpd.lang.model.BotTask;

import com.google.common.base.Charsets;

public class BotTaskContentUtil {
    public static InputStream createBotTaskInfo() throws UnsupportedEncodingException {
        StringBuffer buffer = new StringBuffer();
        return new ByteArrayInputStream(buffer.toString().getBytes(PluginConstants.UTF_ENCODING));
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

    public static BotTask getBotTaskFromFile(IFile botTaskFile) {
        BotTask task = new BotTask();
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(botTaskFile.getContents(), PluginConstants.UTF_ENCODING));
            String clazz = r.readLine();
            String conf = r.readLine();
            task.setName(botTaskFile.getName());
            task.setClazz(clazz);
            task.setDelegationClassName(clazz);
            if (conf != null) {
                IFile confFile = ((IFolder) botTaskFile.getParent()).getFile(conf);
                BufferedReader confReader = new BufferedReader(new InputStreamReader(confFile.getContents(), PluginConstants.UTF_ENCODING));
                StringBuilder strConfiguration = new StringBuilder();
                char[] readBuffer = new char[1024];
                int n;
                while ((n = confReader.read(readBuffer)) > 0) {
                    strConfiguration.append(readBuffer, 0, n);
                }
                confReader.close();
                String fileConfig = strConfiguration.toString();
                if (BotTaskConfigHelper.isConfigParamInPlugin(fileConfig)) {
                    Document doc = DocumentHelper.parseText(fileConfig);
                    task.setParamDefConfig(ParamDefConfig.parse(doc));
                    task.setConfig(fileConfig);
                    task.setDelegationConfiguration(fileConfig);
                } else if (BotTaskConfigHelper.isConfigParamInFile(fileConfig)) {
                    ParamDefConfig paramDefConfig = BotTaskConfigHelper.getParamDefConfig(fileConfig);
                    task.setParamDefConfig(paramDefConfig);
                    String config = BotTaskConfigHelper.getConfigFromBotTaskConfig(fileConfig);
                    task.setConfig(config);
                    task.setDelegationConfiguration(config);
                } else {
                    ParamDefConfig paramDefConfig = BotTaskConfigHelper.createEmptyParamDefConfig();
                    task.setParamDefConfig(paramDefConfig);
                    task.setConfig(fileConfig);
                    task.setDelegationConfiguration(fileConfig);
                }
            } else {
                ParamDefConfig paramDefConfig = BotTaskConfigHelper.createEmptyParamDefConfig();
                task.setParamDefConfig(paramDefConfig);
            }
            r.close();
        } catch (Exception e) {
            PluginLogger.logError("Unable read bot task file", e);
        }
        return task;
    }
}
