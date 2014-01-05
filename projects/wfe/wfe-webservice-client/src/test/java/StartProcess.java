import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;

import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ExecutionAPI;
import ru.runa.wfe.webservice.ExecutionWebService;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.Variable;

public class StartProcess {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutionAPI executionAPI = new ExecutionWebService().getExecutionAPIPort();
            List<Variable> variables = new ArrayList<Variable>();

/*            Variable variable1 = new Variable();
            variable1.setName("var1");
            GregorianCalendar c = new GregorianCalendar();
            variable1.setDateValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
            variables.add(variable1);

            Variable variable2 = new Variable();
            variable2.setName("Переменная1");
            variable2.setStringValue("СТРОКА");
            variables.add(variable2);

            Variable variable3 = new Variable();
            variable3.setName("Переменная2");
            variable3.setLongValue(-3L);
            variables.add(variable3);*/

            Long processId = executionAPI.startProcessWS(user, "ESCALATIONTEST", variables);
            System.out.println(processId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
