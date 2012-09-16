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
package ru.runa.af.delegate.bot.impl;

import ru.runa.af.bot.BotInvokerException;
import ru.runa.af.delegate.bot.BotDelegateResources;
import ru.runa.af.service.bot.BotInvokerService;
import ru.runa.delegate.impl.EJB3Delegate;

/**
 * Created on 04.03.2005
 */
public class BotInvokerServiceDelegateRemoteImpl extends EJB3Delegate implements BotInvokerService {
    private String providerBaseUrl;

    public BotInvokerServiceDelegateRemoteImpl() {
    }

    public BotInvokerServiceDelegateRemoteImpl(String providerBaseUrl) {
        this.providerBaseUrl = providerBaseUrl;
    }

    
    @Override
    protected String getProviderUrl() {
        return (providerBaseUrl == null || providerBaseUrl.equals("")) ? BotDelegateResources.getDelegateDefaultProviderUrl() : providerBaseUrl;
    }

    @Override
    protected String getUrlPkgPrefixes() {
        return BotDelegateResources.getDelegateRemoteUrlPkgPrefixes();
    }

    @Override
    protected String getInitialContextFactory() {
        return BotDelegateResources.getDelegateRemoteInitialContextFactory();
    }


    @Override
    protected String getBeanName() {
        return "BotInvokerServiceBean";
    }

    private BotInvokerService getBotInvokerService() {
        setRemote(true);
        return getService();
    }

    @Override
    public void startPeriodicBotsInvocation() throws BotInvokerException {
        getBotInvokerService().startPeriodicBotsInvocation();
    }

    @Override
    public void cancelPeriodicBotsInvocation() {
        getBotInvokerService().cancelPeriodicBotsInvocation();
    }

    @Override
    public boolean isRunning() {
        return getBotInvokerService().isRunning();
    }

    @Override
    public void invokeBots() throws BotInvokerException {
        getBotInvokerService().invokeBots();
    }
    
    
    private final static String START_ARGUMENT = "start";
    private final static String STOP_ARGUMENT = "stop";
    private final static String STATUS_ARGUMENT = "status";


    public static void main(String[] args) throws Exception {
        try {
            BotInvokerServiceDelegateRemoteImpl delegate = new BotInvokerServiceDelegateRemoteImpl();
            if (args.length == 1) {
                if (START_ARGUMENT.equals(args[0])) {
                    delegate.startPeriodicBotsInvocation();
                    System.out.println("bots pereodic invocation started");
                    System.exit(0);
                } else if (STOP_ARGUMENT.equals(args[0])) {
                    delegate.cancelPeriodicBotsInvocation();
                    System.out.println("bots pereodic invocation stopped");
                    System.exit(1);
                } else if (STATUS_ARGUMENT.equals(args[0])) {
                    if (delegate.pringStatus(delegate)) {
                        System.exit(0);
                    } else {
                        System.exit(1);
                    }
                }
            }
            printUsage();
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("Failed to execute command because of: " + e.getMessage());
            System.out.println("Stack trace:");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void printUsage() {
        System.out.println("Allowed commands:");
        System.out.println("start - starts pereodic bots invocation.");
        System.out.println("stop - stops pereodic bots invocation.");
        System.out.println("status - checks pereodic bots invocation status.");
        System.out.println();
        System.out.println("Error codes:");
        System.out.println("-1 - invocation error.");
        System.out.println("0 - bots pereodic invocation started.");
        System.out.println("1 - bots pereodic invocation stopped.");
    }

    private boolean pringStatus(BotInvokerServiceDelegateRemoteImpl delegate) throws BotInvokerException {
        boolean isRunning = delegate.isRunning();
        String status = isRunning ? "started" : "stopped";
        System.out.println("bots pereodic invocation status:" + status);
        return isRunning;
    }
}
