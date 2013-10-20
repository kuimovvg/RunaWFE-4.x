package ru.cg.runaex.web.controller;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import ru.cg.runaex.database.bean.FtlComponent;
import ru.cg.runaex.database.bean.ParFile;
import ru.cg.runaex.database.bean.WbaFile;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.dao.BaseDao;
import ru.cg.runaex.database.service.UpdateDbService;
import ru.cg.runaex.database.structure.DBStructureManager;
import ru.cg.runaex.database.util.WbaParser;
import ru.cg.runaex.exceptions.BusinessProcessException;
import ru.cg.runaex.generate_security.GenerateSecurityFunctionsService;
import ru.cg.runaex.generatedb.bean.Database;
import ru.cg.runaex.generatedb.bean.Field;
import ru.cg.runaex.generatedb.bean.Schema;
import ru.cg.runaex.generatedb.bean.Table;
import ru.cg.runaex.generatedb.util.DatabaseStructureGenerator;
import ru.cg.runaex.generatedb.util.TableHashSet;
import ru.cg.runaex.shared.bean.project.xml.Project;
import ru.cg.runaex.validation.ErrorMessage;
import ru.cg.runaex.web.model.DeployedBusinessApplication;
import ru.cg.runaex.web.model.DeployedProcess;
import ru.cg.runaex.web.security.SecurityUtils;
import ru.cg.runaex.web.security.model.RunaWfeUser;
import ru.cg.runaex.web.service.ComponentValidatorService;
import ru.cg.runaex.web.service.DatabaseMetadataService;
import ru.cg.runaex.web.service.RunaWfeService;
import ru.cg.runaex.web.utils.SessionUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ValidationException;
import java.util.*;

/**
 * @author Петров А.
 */
@Controller
public class ManageProcessDefinitionsController extends BaseController {
  private static final Logger logger = LoggerFactory.getLogger(ManageProcessDefinitionsController.class);

  private static final String DATABASE_CACHE_KEY = "database";
  private static final String WBA_FILE_SESSION_KEY = "wbaFile";

  private boolean generateWebGuardFunctions;

  @Autowired
  @Qualifier("runaexMessageSource")
  private ResourceBundleMessageSource messages;

  @Autowired
  private BaseDao baseDao;
  @Autowired
  private UpdateDbService updateDbService;
  @Autowired
  private RunaWfeService runaWfeService;

  @Autowired
  private DatabaseMetadataService databaseMetadataService;
  @Autowired
  private ComponentValidatorService componentValidatorService;
  @Autowired
  private DBStructureManager dbStructureManager;
  @Autowired(required=false)
  private GenerateSecurityFunctionsService generateSecurityFunctionsService;

  @PostConstruct
  private void init() {
    generateWebGuardFunctions = generateSecurityFunctionsService != null;
  }

  @RequestMapping(value = "/manageProcessDefinitions", method = RequestMethod.GET)
  public ModelAndView manageProcessDefinition() {
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "manage_process_definition");
    return mv;
  }

  @RequestMapping(value = "/uploadFiles", method = RequestMethod.POST)
  public ModelAndView uploadParFiles(MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {
    HttpSession session = request.getSession();
    List<MultipartFile> multipartFiles = request.getFiles("parfile");

    if (multipartFiles.isEmpty()) {
      String errorMessage = messages.getMessage("uploadProcessRequestEmpty", null, Locale.ROOT);
      return getManageProcessDefinitionModelAndView(null, errorMessage);
    }
    else if (multipartFiles.size() > 1) {
      String errorMessage = messages.getMessage("uploadProcessRequestMultipleFile", null, Locale.ROOT);
      return getManageProcessDefinitionModelAndView(null, errorMessage);
    }
    else if (!multipartFiles.get(0).getOriginalFilename().endsWith(".wba")) {
      String errorMessage = messages.getMessage("uploadProcessRequestInvalidFileFormat", null, Locale.ROOT);
      return getManageProcessDefinitionModelAndView(null, errorMessage);
    }

    boolean generateDatabase = false;
    WbaFile wbaFile = null;
    Database database = null;
    String errorMessage = null;
    List<ErrorMessage> validateErrors = null;
    removeEmptyFiles(multipartFiles);
    try {
      wbaFile = WbaParser.processFile(multipartFiles.get(0), messages);
      generateDatabase = isDatabaseGenerationRequired(wbaFile);

      List<FtlComponent> ftlComponents = new ArrayList<FtlComponent>();
      if (!wbaFile.getParFiles().isEmpty()) {
        for (ParFile parFile : wbaFile.getParFiles()) {
          List<FtlComponent> parFileComponents = parFile.getFtlComponents();
          ftlComponents.addAll(parFileComponents);
        }
        validateErrors = componentValidatorService.validateComponents(ftlComponents);
      }
      else
        errorMessage = messages.getMessage("uploadProcessRequestInvalidWba", null, Locale.ROOT);

      if (errorMessage == null && validateErrors.isEmpty() && generateDatabase)
        database = DatabaseStructureGenerator.generateDatabaseStructure(ftlComponents);
    }
    catch (BusinessProcessException ex) {
      logger.error(ex.toString(), ex);
      errorMessage = messages.getMessage("businessProcessException", null, Locale.ROOT);
    }
    catch (ValidationException ex) {
      logger.error(ex.toString(), ex);
      errorMessage = messages.getMessage("validationException", null, Locale.ROOT);
    }
    catch (Exception ex) {
      logger.error(ex.toString(), ex);
      errorMessage = ex.toString();
    }

    if (errorMessage != null || validateErrors != null && !validateErrors.isEmpty()) {
      ModelAndView mv = new ModelAndView("main");
      mv.addObject("content", "manage_process_definition");
      mv.addObject("validateErrors", validateErrors);
      mv.addObject("error", errorMessage);
      return mv;
    }
    if (!generateDatabase)
      return handleGenerateDbRequest(database, null, wbaFile);

    boolean hasReferences = false;
    for (Table table : database.getTables()) {
      if (table.hasReferences()) {
        hasReferences = true;
        break;
      }
    }

    if (!hasReferences) {
      return handleGenerateDbRequest(database, null, wbaFile);
    }

    for (ParFile parFile : wbaFile.getParFiles()) {
      if (runaWfeService.processDefinitionExists(SecurityUtils.getCurrentRunaUser(), parFile.getProcessName())) {
        loadCascadePreferences(database.getTables());
        break;
      }
    }
    List<Table> sortedTables = new LinkedList<Table>(database.getTables());
    Collections.sort(sortedTables, new Comparator<Table>() {
      @Override
      public int compare(Table o1, Table o2) {
        int result = o1.getSchema().getName().compareTo(o2.getSchema().getName());
        if (result == 0) {
          return o1.getName().compareTo(o2.getName());
        }
        return result;
      }
    });

    session.setAttribute(SessionUtils.getKeyWithinSession(session, DATABASE_CACHE_KEY), database);
    session.setAttribute(SessionUtils.getKeyWithinSession(session, WBA_FILE_SESSION_KEY), wbaFile);

    String debugMessage = messages.getMessage("filesUploadSuccessfully", null, Locale.ROOT);
    return getDbCascadeModelAndView(sortedTables, errorMessage, debugMessage);
  }

  @SuppressWarnings("unchecked")
  private void loadCascadePreferences(TableHashSet<Table> tables) {
    String[] selectFields = new String[2];
    selectFields[0] = "table_name";
    selectFields[1] = "fields";
    List<String> tableNames = new ArrayList<String>();
    for (Table table : tables) {
      tableNames.add(table.getSchema().getName().concat(".").concat(table.getName()));
    }
    List<Data> dataList = new ArrayList<Data>(1);
    dataList.add(new Data("table_name_in", tableNames, "varchar"));
    TransportData filterData = new TransportData(0, dataList);
    TransportDataSet resultData = baseDao.getData(null, "metadata", "cascade_fks", selectFields, null, null, null, null,
        null, null, filterData, "dummy", null, null);
    Map<String, String[]> fieldsForCascadeDeletion = new HashMap<String, String[]>();
    for (TransportData td : resultData.getSets()) {
      fieldsForCascadeDeletion.put((String) td.getData("table_name").getValue(), (String[]) td.getData("fields").getValue());
    }

    for (Table table : tables) {
      String[] arrFields = fieldsForCascadeDeletion.get(table.getSchema().getName() + "." + table.getName());
      if (arrFields != null) {
        List<String> fields = Arrays.asList(arrFields);
        for (Field field : table.getFields()) {
          if (field.getReferences() != null && fields.contains(field.getName())) {
            //assumes that if field with the same name exists it maps to the same column
            field.getReferences().setCascadeDeletion(true);
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/generateDb", method = RequestMethod.POST)
  public ModelAndView generateDb(@RequestParam(value = "cascadeFks", required = false) String[] cascadeFks, HttpServletRequest request) throws Exception {
    HttpSession session = request.getSession();
    Database database = (Database) session.getAttribute(SessionUtils.getKeyWithinSession(session, DATABASE_CACHE_KEY));
    WbaFile wbaFile = (WbaFile) session.getAttribute(SessionUtils.getKeyWithinSession(session, WBA_FILE_SESSION_KEY));

    return handleGenerateDbRequest(database, cascadeFks, wbaFile);
  }

  private ModelAndView handleGenerateDbRequest(Database database, String[] cascadeFks, WbaFile wbaFile) throws Exception {
    RunaWfeUser currentUser = SecurityUtils.getCurrentUser();
    boolean generateDatabase = isDatabaseGenerationRequired(wbaFile);

    try {
      if (generateDatabase) {
        TableHashSet<Table> tables = database.getTables();
        setFieldsForCascadeDeletion(tables, cascadeFks);
        database.setTables(tables);
        applyDatabaseDiff(database);
        databaseMetadataService.saveCascadePreferences(tables);
      }

      Project project = wbaFile.getProjectStructure();
      DeployedBusinessApplication deployedBusinessApplication = runaWfeService.redeployParFiles(currentUser.getUser(), wbaFile.getParFiles(), project.getProjectName());

      databaseMetadataService.updateDbConnections(deployedBusinessApplication, wbaFile.getJndiName(), wbaFile.getJdbcDriverClassName());
      databaseMetadataService.saveMetadata(deployedBusinessApplication, wbaFile.getJndiName());
      databaseMetadataService.saveProjectGroovyFunction(wbaFile.getProjectGroovyFunctions(), project.getProjectName());
      saveProjectStructure(project);

      if (generateWebGuardFunctions)
        generateSecurityFunctionsService.generateSecurityFunctions(wbaFile.getParFiles());

      List<Long> processIds = new ArrayList<Long>(deployedBusinessApplication.getProcesses().size());
      for (DeployedProcess process : deployedBusinessApplication.getProcesses()) {
        processIds.add(process.getDefinitionId());
      }
      dbStructureManager.initDbStructure(processIds);
    }
    catch (Exception ex) {
      logger.error(ex.toString(), ex);
      throw ex;
    }

    String successMessage = messages.getMessage("processesSuccessfullyUploaded", null, Locale.ROOT); //todo use client locale

    return getManageProcessDefinitionModelAndView(successMessage, null);
  }

  private boolean isDatabaseGenerationRequired(WbaFile wbaFile) {
    return wbaFile.getJndiName() == null;
  }

  private TableHashSet<Table> setFieldsForCascadeDeletion(TableHashSet<Table> tables, String[] cascadeFks) {
    MultiKeyMap fieldsByTableForCascadeDeletion = new MultiKeyMap();
    if (cascadeFks != null) {
      for (String fkKey : cascadeFks) {
        String[] parts = StringEscapeUtils.unescapeHtml(fkKey).split("\\.");
        String schema = parts[0];
        String table = parts[1];
        String field = parts[2];
        MultiKey key = new MultiKey(schema, table);

        List<String> fields = (List<String>) fieldsByTableForCascadeDeletion.get(key);
        if (fields == null) {
          fields = new LinkedList<String>();
          fieldsByTableForCascadeDeletion.put(key, fields);
        }
        fields.add(field);
      }
    }

    List<String> fieldsForCascadeDeletion;
    for (Table table : tables) {
      fieldsForCascadeDeletion = (List<String>) fieldsByTableForCascadeDeletion.get(new MultiKey(table.getSchema().getName(), table.getName()));
      if (fieldsForCascadeDeletion != null) {
        for (Field field : table.getFields()) {
          if (fieldsForCascadeDeletion.contains(field.getName())) {
            field.getReferences().setCascadeDeletion(true);
          }
          else if (field.getReferences() != null) {
            field.getReferences().setCascadeDeletion(false);
          }
        }
      }
      else {
        for (Field field : table.getFields()) {
          if (field.getReferences() != null) {
            field.getReferences().setCascadeDeletion(false);
          }
        }
      }
    }

    return tables;
  }

  private void applyDatabaseDiff(Database database) throws Exception {
    TableHashSet<Table> tables = database.getTables();
    List<String> schemas = new ArrayList<String>(tables.getSchemas().size());
    for (Schema schema : tables.getSchemas())
      schemas.add(schema.getName());

    try {
      updateDbService.applyDb(database.getSQL(), schemas);
    }
    catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw e;
    }
  }

  private ModelAndView getManageProcessDefinitionModelAndView(String success, String error) {
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "manage_process_definition");
    mv.addObject("success", success);
    mv.addObject("error", error);
    return mv;
  }

  private ModelAndView getDbCascadeModelAndView(List<Table> sortedTables, String error, String debug) {
    ModelAndView mv = new ModelAndView("main");
    mv.addObject("content", "set_db_cascades");
    mv.addObject("tables", sortedTables);
    mv.addObject("error", error);
    mv.addObject("debug", debug);
    return mv;
  }

  private void saveProjectStructure(Project project) {
    if (project == null) {
      return;
    }

    databaseMetadataService.saveProjectStructure(project);
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }
}
