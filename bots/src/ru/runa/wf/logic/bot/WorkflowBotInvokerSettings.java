/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.logic.bot;

public class WorkflowBotInvokerSettings /*extends DBSettingsCommon*/{
    /*public static class WorkflowBotInvokerSettingsCreator implements DBSettingsModuleCreator{
    	public WorkflowBotInvokerSettingsCreator(){}
    	public Set<DBSettingsModule> create() {
    		Set<DBSettingsModule> result = new HashSet<DBSettingsModule>();
    		String botstations = new SettingsLogic().getSettingDBValue(Resources.BOT_INVOKER_COMMON_MODULE, Resources.BOT_INVOKER_BOTSTATIONS);
    		if(botstations == null)
    			botstations = Resources.BOT_INVOKER_BOTSTATIONS_DV;
    		for(String serverId : botstations.split(","))
    			result.add(new WorkflowBotInvokerSettings(serverId));
    		return result;
    	}
    	
    }
    private static final String WORKFLOW_BOT_INVOKER_USER = "login";
    private static final String WORKFLOW_BOT_INVOKER_USER_DN = "settings.module.botstation.login";
    private static final String WORKFLOW_BOT_INVOKER_USER_DD = "settings.module.botstation.login.description";
    private static final String WORKFLOW_BOT_INVOKER_USER_DV = "localbotstation";
    private static final String WORKFLOW_BOT_INVOKER_PWD = "password";
    private static final String WORKFLOW_BOT_INVOKER_PWD_DN = "settings.module.botstation.pwd";
    private static final String WORKFLOW_BOT_INVOKER_PWD_DD = "settings.module.botstation.pwd.description";
    private static final String WORKFLOW_BOT_INVOKER_PWD_DV = "123";
    private static final String WORKFLOW_BOT_INVOKER_THREADS = "threads";
    private static final String WORKFLOW_BOT_INVOKER_THREADS_DN = "settings.module.botstation.threads";
    private static final String WORKFLOW_BOT_INVOKER_THREADS_DD = "settings.module.botstation.threads.description";
    private static final String WORKFLOW_BOT_INVOKER_THREADS_DV = "1";
    
    private static Set<SettingTemplate> createSettings(String serverId){
    	Set<SettingTemplate> allowedSettings = new HashSet<SettingTemplate>();
    	allowedSettings.add(new SettingTemplate(WORKFLOW_BOT_INVOKER_USER, WORKFLOW_BOT_INVOKER_USER_DN, WORKFLOW_BOT_INVOKER_USER_DD, WORKFLOW_BOT_INVOKER_USER_DV, null));
    	allowedSettings.add(new SettingTemplate(WORKFLOW_BOT_INVOKER_PWD, WORKFLOW_BOT_INVOKER_PWD_DN, WORKFLOW_BOT_INVOKER_PWD_DD, WORKFLOW_BOT_INVOKER_PWD_DV, null));
    	String biclass = new SettingsLogic().getSettingDBValue(Resources.BOT_INVOKER_CUSTOM_MODULE_PREFIX + serverId, Resources.BOT_INVOKER_CLASS_NAME);
    	if(biclass == null)
    		biclass = Resources.BOT_INVOKER_CLASS_NAME_DV;
    	allowedSettings.add(new SettingTemplate(WORKFLOW_BOT_INVOKER_THREADS, WORKFLOW_BOT_INVOKER_THREADS_DN, WORKFLOW_BOT_INVOKER_THREADS_DD, WORKFLOW_BOT_INVOKER_THREADS_DV, null, !biclass.equals(WorkflowThreadPoolBotInvoker.class.getCanonicalName())));
    	return allowedSettings;
    }
    
    public WorkflowBotInvokerSettings(){
    	this(ResourceCommons.getServerId());
    }
    public WorkflowBotInvokerSettings(String serverId){
    	super(Resources.BOT_INVOKER_CUSTOM_MODULE_PREFIX + serverId, Resources.BOT_INVOKER_CUSTOM_MODULE_DN, DBSettingsCommon.addInitializeFlag(createSettings(serverId)), Resources.menuPosition);
    	checkInitialized();
    }
    
    public void reinitialize() throws IllegalResourceRequestException {
    	try{
    		WorkflowBotConfigurationParser parser = new WorkflowBotConfigurationParser();
    		parser.readLoginAndPassword();
    		setSetting(WORKFLOW_BOT_INVOKER_USER, parser.login);
    		setSetting(WORKFLOW_BOT_INVOKER_PWD, parser.password);
    		setSetting(WORKFLOW_BOT_INVOKER_THREADS, Integer.toString(parser.threadPoolSize));
    	}catch(WorkflowBotConfigurationParserException e){
    		defaultReinitialize();
    	}
    }

    public int getThreadCount(){
    	return Integer.parseInt(getSetting(WORKFLOW_BOT_INVOKER_THREADS));
    }

    public String getLogin(){
    	return getSetting(WORKFLOW_BOT_INVOKER_USER);
    }
    public String getPassword(){
    	return getSetting(WORKFLOW_BOT_INVOKER_PWD);
    }*/
}
