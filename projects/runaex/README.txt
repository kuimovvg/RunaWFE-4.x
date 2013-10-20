
--------------------- Components - Компоненты - freemarker-tags для Руны -------------------------------------------------------
Модуль: components
!!!В ресурсах необходимо прописывать xml, images и localization для последующего настйроки плагина для компонента!!!

* После сборки components необходимо полученный runaex-components.jar скопировать в %HOME_RUNA%\gpd\plugins\org.jbpm.core_3.0.1\lib\
и %HOME_RUNA%\...\server\default\deploy\

--------------------- Плагин для настройки компонентов ---------------------------------------------------------
Интрефейс для плагина - %HOME_RUNA%\gpd\plugins\tk.eclipse.plugin.wysiwyg_3.0.1\plugin.xml
Картинку для представления компонента необходимо положить в %HOME_RUNA%\gpd\plugins\tk.eclipse.plugin.wysiwyg_3.0.1\metadata\ftl_icons\
пример:
<extension>
  ...
  <tag height="40" id="TextField" image="metadata/ftl_icons/TextFieldDisplay1.jpg" name="%Method.TextField" width="250">
    <param name="%Param.TextFieldVariable" type="combo" values="string" variableAccess="READ" />
    <param name="%Param.TextFieldVariable_Pattern" type="combo" values="string" variableAccess="NONE">
      <paramValue name="%Presentation.NONE" value="" />
      <paramValue name="%Presentation.STRING" value="text" />
      <paramValue name="%Presentation.NUMBER" value="number" />
    </param>
  </tag>
  ...
</extension>


Локализация - %HOME_RUNA%\gpd\plugins\tk.eclipse.plugin.wysiwyg_3.0.1\plugin.properties
пример:
  ru.cg.TextFieldTag=Text field
  Method.TextField=Text field
  Param.TextFieldVariable=Var name
  Param.TextFieldVariable_Pattern=Pattern
  Presentation.NONE=No pattern
  Presentation.STRING=Only character. Max 9
  Presentation.NUMBER=Only number. Max 9

Локализация - %HOME_RUNA%\gpd\plugins\tk.eclipse.plugin.wysiwyg_3.0.1\plugin_ru.properties
пример:
  Method.TextField=\u0422\u0435\u043A\u0441\u0442\u043E\u0432\u043E\u0435 \u043F\u043E\u043B\u0435
  Param.TextFieldVariable=\u041D\u0430\u0438\u043C\u0435\u043D\u043E\u0432\u0430\u043D\u0438\u0435 \u043F\u0435\u0440\u0435\u043C\u0435\u043D\u043D\u043E\u0439
  Param.TextFieldVariable_Pattern=\u041F\u0440\u0430\u0432\u0438\u043B\u043E \u0432\u0432\u043E\u0434\u0430
  Presentation.NONE=\u0411\u0435\u0437 \u043E\u0433\u0440\u0430\u043D\u0438\u0447\u0435\u043D\u0438\u0439
  Presentation.STRING=\u0422\u043E\u043B\u044C\u043A\u043E \u043B\u0430\u0442\u0438\u043D\u0438\u0446\u0430. \u041C\u0430\u043A\u0441 9
  Presentation.NUMBER=\u0422\u043E\u043B\u044C\u043A\u043E \u0447\u0438\u0441\u043B\u0430. \u041C\u0430\u043A\u0441 9


Регистрация компонета - %HOME_RUNA%\...\server\default\conf\freemarker-tags.xml
пример:
  <?xml version="1.0" encoding="UTF-8" ?>
  <configuration xmlns="http://runa.ru/xml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://runa.ru/xml">
    <ftltag name="ChooseActor" class="ru.runa.wf.web.ftl.tags.ChooseActorTag" />
    <ftltag name="DisplayActor" class="ru.runa.wf.web.ftl.tags.DisplayActorTag" />
    <ftltag name="GroupMembers" class="ru.runa.wf.web.ftl.tags.GroupMembersTag" />
    <ftltag name="ViewFile" class="ru.runa.wf.web.ftl.tags.FileTag" />
    <ftltag name="InputDateTime" class="ru.runa.wf.web.ftl.tags.DateTimeInputTag" />
    <ftltag name="RelationResult" class="ru.runa.wf.web.ftl.tags.RelationResultTag" />
    <ftltag name="DownloadFile" class="ru.runa.wf.web.ftl.VarTagMethodModel">
      <vartagClassName>ru.runa.wf.web.html.vartag.FileVariableValueDownloadVarTag</vartagClassName>
    </ftltag>
    <ftltag name="TextField" class="ru.cg.TextFieldTag" />
  </configuration>
--------------------- Web Services for RUNA ------------------------------------------------------------------
Модули: ws и wsclient
ws - веб сервис для Руны, который встраивается в область видимости Руны.
wsclient -  вызов веб сервиса.

* После сборки ws необходимо полученный runaex-ws.war скопировать в %HOME_RUNA%\...\server\default\deploy\
* Перенести два файла: runa-common.jar и wfe-custom.jar из %HOME_RUNA%\...\server\default\deploy\ в
%HOME_RUNA%\...\server\default\lib\

Запустить runSimulation.bat и скопировать в брайзур http://localhost:8080/jbossws/services. Там должено появиться
...
Endpoint Name	jboss.ws:context=runaex-ws,endpoint=CgDevelopersWS
Endpoint Address	http://localhost:8080/runaex-ws/CgDevelopersWS?wsdl
...
----------------------- RUNAEX-CORE PLAY FRAMEWORK ---------------------------------------------------------------
Настройка движка проигрывания БП. Необходимо положить файл ...\lib\runaex-core\properties.xml в домашний
каталог пользователя. Это настроечный файл для компонент.
------------------------------------------------------------------------------------------------------------------

Скопировать файлы:
   gson-2.2.2.jar
   runaex-components.jar
   runaex-components-core.jar
   runaex-ws.war
   runaex-wsclient.jar
   runawfe-ws-client-3.4.2.jar
в %HOME_RUNA%\...\server\default\lib\

Перед запуском WFE RUNA необходимо удалить runaex-ws.war из %HOME_RUNA%\...\server\default\
 и добавить после успешного запуска.


Увеличить диалоговое окно:
RunaWFE\gpd\plugins\tk.eclipse.plugin.wysiwyg_3.0.1\FCKeditor\editor\plugins\FreemarkerTags\fckplugin.js
(400,200) на (600, 400)

конфигурация jBoss для работы с SQLActionHandler:
 в /server/default/conf/jbossjta-properties.xml добавить
 <property name="com.arjuna.ats.jta.allowMultipleLastResources" value="true" />
