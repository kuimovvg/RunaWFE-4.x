import java.util.List;

import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ExecutionAPI;
import ru.runa.wfe.webservice.ExecutionWebService;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.WfTask;

public class GetTasks {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutionAPI executionAPI = new ExecutionWebService().getExecutionAPIPort();

            List<WfTask> tasks = executionAPI.getTasks(user, null);
            System.out.println("TASKS = " + tasks.size());
            for (WfTask task : tasks) {
                System.out.println(" Task " + task.getName() + " assigned to " + task.getOwner().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
