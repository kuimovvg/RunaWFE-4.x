package ru.runa.wfe.service.client;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.service.BotInvokerService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Invokes bots on selected bot station.
 * 
 * @author Dofs
 */
public class BotInvokerClient {
    private final static String START_ARGUMENT = "start";
    private final static String STOP_ARGUMENT = "stop";
    private final static String STATUS_ARGUMENT = "status";

    public static void main(String[] args) throws Exception {
        try {
            BotInvokerService botInvokerService = Delegates.getBotInvokerService();
            if (args.length == 1) {
                if (START_ARGUMENT.equals(args[0])) {
                    String botStationName = args[1];
                    BotStation botStation = Delegates.getBotService().getBotStationByName(botStationName);
                    if (botStation == null) {
                        System.err.println("No botstation could not be found '" + botStationName + "'");
                    }
                    botInvokerService.startPeriodicBotsInvocation(botStation);
                    System.out.println("bots pereodic invocation started");
                    System.exit(0);
                } else if (STOP_ARGUMENT.equals(args[0])) {
                    botInvokerService.cancelPeriodicBotsInvocation();
                    System.out.println("bots pereodic invocation stopped");
                    System.exit(1);
                } else if (STATUS_ARGUMENT.equals(args[0])) {
                    if (printStatus(botInvokerService)) {
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
        System.out.println("start - starts pereodic bots invocation, botStationName");
        System.out.println("stop - stops pereodic bots invocation.");
        System.out.println("status - checks pereodic bots invocation status.");
        System.out.println();
        System.out.println("Error codes:");
        System.out.println("-1 - invocation error.");
        System.out.println("0 - bots pereodic invocation started.");
        System.out.println("1 - bots pereodic invocation stopped.");
    }

    private static boolean printStatus(BotInvokerService botInvokerService) {
        boolean isRunning = botInvokerService.isRunning();
        String status = isRunning ? "started" : "stopped";
        System.out.println("bots pereodic invocation status:" + status);
        return isRunning;
    }

}
