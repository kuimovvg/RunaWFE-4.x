using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using RunaClient;

namespace RunaClientDemo
{
    class Program
    {
        static void Main(string[] args)
        {

            //Аутентификация//
            var LogPas = new authenticateByLoginPassword();
            LogPas.arg0 = "Administrator";           
            LogPas.arg1 = "wf";
            authenticateByLoginPasswordResponse response = new authenticateByLoginPasswordResponse();
            var auth = new AuthenticationWebService();
            response = auth.authenticateByLoginPassword(LogPas);
          
            //запуск процесса//
            var exec = new ExecutionWebService();
            var startPr = new startProcessWS();
            //создание переменных
            wfVariable[] vars = new wfVariable[2];
            wfVariable str1 = new wfVariable();            
            str1.name = "Str1";
            str1.stringValue = "Hello World";
            str1.formatClassName = "ru.runa.wfe.var.format.StringFormat";
            wfVariable int1 = new wfVariable();            
            int1.name = "Int1";
            int1.longValue = 100;
            int1.formatClassName = "ru.runa.wfe.var.format.LongFormat";
            int1.longValueSpecified = true;
            vars[0] = str1;
            vars[1] = int1;
            startPr.arg0 = response.@return;//user
            startPr.arg1 = "test_srvc2";
            startPr.arg2 = vars;            
            exec.startProcessWS(startPr);



            /*
            Console.WriteLine("Test1: Start process.");
            Console.WriteLine("\n input user:");

            //////////////////
            //Аутентификация//
            //////////////////

            var LogPas = new authenticateByLoginPassword();
            LogPas.arg0 = Console.ReadLine();
            Console.WriteLine("\n input password:");
            LogPas.arg1 = Console.ReadLine();

            authenticateByLoginPasswordResponse response = new authenticateByLoginPasswordResponse();
            Console.WriteLine("\nAuthentication...");

            var auth = new AuthenticationWebService();
            try
            {
                response = auth.authenticateByLoginPassword(LogPas);                                
            }
            catch (Exception exc)
            {
                Console.WriteLine("\nStackTrace: ");
                Console.WriteLine("Error: " + exc.ToString());
            }
            finally
            {
                Console.WriteLine(response.ToString());
            }

            //////////////////////////////////



            //////////////////////////////////
            //получение определения процесса//
            //////////////////////////////////

            var definition = new DefinitionWebService();            
            var proc_def_param = new getLatestProcessDefinition();

            proc_def_param.arg0 = response.@return;
            proc_def_param.arg1 = "test_srvc2";

            var process_def = definition.getLatestProcessDefinition(proc_def_param);


            /////////////////////////////////////////////////////////////
            //получение списка описаний переменных определения процесса//
            /////////////////////////////////////////////////////////////

            var getVarParam = new getVariables();
            getVarParam.arg0 = response.@return;
            getVarParam.arg1 = process_def.@return.id;
            getVarParam.arg1Specified = process_def.@return.idSpecified;

            variableDefinition[] vars_def = definition.getVariables(getVarParam);

            var exec = new ExecutionWebService();

            var startPr = new startProcessWS();
            
            //создание переменных
            wfVariable[] vars = new wfVariable[2];

            wfVariable str1 = new wfVariable();
            //str1.name = vars_def[0].name;
            str1.name = "Str1";
            str1.stringValue = "Hello World";
            str1.formatClassName = "ru.runa.wfe.var.format.StringFormat";

            wfVariable int1 = new wfVariable();
            //int1.name = vars_def[1].name;
            int1.name = "Int1";
            int1.longValue = 100;
            int1.formatClassName = "ru.runa.wfe.var.format.LongFormat";
            int1.longValueSpecified = true;
            vars[0] = str1;
            vars[1] = int1;

            startPr.arg0 = response.@return;
            startPr.arg1 = "test_srvc2";
            startPr.arg2 = vars;
            
            //запуск процесса
            exec.startProcessWS(startPr);
            

            Console.Write("Press any key to continue . . . ");
            Console.ReadKey(true);
            */
        }
    }
}
