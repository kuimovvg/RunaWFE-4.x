package ru.cg.runaex.components_plugin.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.DelegableConfigurationDialog;

import ru.cg.runaex.runa_ext.handler.db_variable_handler.DbVariableHandlerConfiguration;
import ru.cg.runaex.runa_ext.handler.db_variable_handler.DbVariableHandlerConfigurationHelper;
import ru.cg.runaex.runa_ext.handler.db_variable_handler.DbVariableHandlerParameter;
import ru.cg.runaex.components_plugin.ComponentsPluginActivator;
import ru.cg.runaex.components_plugin.Localization;

public class DBVariableHandlerDescriptor extends DelegableProvider {

  @Override
  protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
    return new DbVariableHandlerConfigurationDialog(delegable.getDelegationConfiguration());
  }


  private static class DbVariableHandlerConfigurationDialog extends DelegableConfigurationDialog {
    private ParametersPanel configurationPanel;

    private DbVariableHandlerConfiguration initialConfiguration;

    public DbVariableHandlerConfigurationDialog(String initialValue) {
      super(initialValue);

      if (initialValue != null && !initialValue.trim().isEmpty()) {
        try {
          initialConfiguration = DbVariableHandlerConfigurationHelper.parseConfiguration(initialValue);
        }
        catch (Exception e) {
          ComponentsPluginActivator.logError("Could not parse webservice call configuration. Configuration: " + initialValue, e);
        }
      }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
      getShell().setText(title);

      Localization localization = HandlerFactory.getDBVariableHandlerDescriptorLocalization();

      Composite dialog = new Composite(parent, SWT.NONE);
      dialog.setLayout(new GridLayout(2, false));
      dialog.setLayoutData(new GridData(GridData.FILL_BOTH));

      configurationPanel = new ParametersPanel(dialog, localization, SWT.NONE);

      initValues();

      return dialog;
    }

    public void initValues() {
      if (initialConfiguration != null) {
        configurationPanel.setParameters(initialConfiguration.getParameters());
      }
    }

//    @Override
    protected String assembleResult() {
      DbVariableHandlerConfiguration configuration = new DbVariableHandlerConfiguration();
      configuration.setParameters(configurationPanel.getParameters());
      return DbVariableHandlerConfigurationHelper.serializeConfiguration(configuration);
    }
  }

  private static class ParametersPanel extends Composite {
    private TableViewer parametersViewer;
    private List<DbVariableHandlerParameter> parameters;

    private Localization localization;

    private final String VARIABLE_NAME_COLUMN = "variableName";
    private final String COLUMN_NAME_COLUMN = "columnName";

    private String[] columnNames = new String[] {
        VARIABLE_NAME_COLUMN,
        COLUMN_NAME_COLUMN
    };

    public ParametersPanel(Composite parent, Localization localization, int style) {
      super(parent, style);
      this.localization = localization;

      parameters = new ArrayList<DbVariableHandlerParameter>();

      this.setLayout(new GridLayout(1, false));
      this.setLayoutData(new GridData(GridData.FILL_BOTH));

      createParametersControls();
      createParametersTable();
    }

    private void createParametersControls() {
      Composite buttonPanel = new Composite(this, SWT.NONE);
      RowLayout layout = new RowLayout(SWT.NONE);
      layout.wrap = true;
      layout.pack = true;
      layout.justify = false;
      layout.type = SWT.HORIZONTAL;
      buttonPanel.setLayout(layout);

      Button add = new Button(buttonPanel, SWT.PUSH | SWT.CENTER);
      add.setText(localization.get("addButton"));
      add.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          DbVariableHandlerParameter parameter = new DbVariableHandlerParameter();
          parameter.setVariableName("");
          parameter.setColumnName("");
          parameters.add(parameter);

          parametersViewer.add(parameter);

          parametersViewer.editElement(parameter, 0);
        }
      });

      Button remove = new Button(buttonPanel, SWT.PUSH | SWT.CENTER);
      remove.setText(localization.get("removeButton"));
      remove.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          DbVariableHandlerParameter parameter = (DbVariableHandlerParameter) ((IStructuredSelection) parametersViewer.getSelection()).getFirstElement();

          if (parameter != null) {
            parametersViewer.remove(parameter);
            parameters.remove(parameter);
          }
        }
      });
    }

    private void createParametersTable() {
      Table parametersTable = new Table(this, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
      parametersTable.setLinesVisible(true);
      parametersTable.setHeaderVisible(true);

      GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
      data.heightHint = 200;
      parametersTable.setLayoutData(data);

      TableColumn column = new TableColumn(parametersTable, SWT.NONE, 0);
      column.setText(localization.get("variableNameHeader"));
      column.setWidth(300);

      column = new TableColumn(parametersTable, SWT.FILL, 1);
      column.setText(localization.get("columnNameHeader"));
      column.setWidth(330);

      parametersViewer = new TableViewer(parametersTable);
      parametersViewer.setUseHashlookup(true);
      parametersViewer.setColumnProperties(columnNames);

      CellEditor[] editors = new CellEditor[columnNames.length];
      editors[0] = new TextCellEditor(parametersTable);
      editors[1] = new TextCellEditor(parametersTable);

      parametersViewer.setCellEditors(editors);
      parametersViewer.setCellModifier(new ParameterCellModifier(columnNames, parametersViewer));

      parametersViewer.setLabelProvider(new ParameterLabelProvider());
      parametersViewer.setContentProvider(new ArrayContentProvider());
      parametersViewer.setInput(parameters);
    }

    public List<DbVariableHandlerParameter> getParameters() {
      return parameters;
    }

    public void setParameters(List<DbVariableHandlerParameter> parameters) {
      this.parameters.clear();
      this.parameters.addAll(parameters);
      parametersViewer.refresh();
    }
  }

  private static class ParameterLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      DbVariableHandlerParameter parameter = (DbVariableHandlerParameter) element;

      switch (columnIndex) {
        case 0:
          return parameter.getVariableName();
        case 1:
          return parameter.getColumnName();
      }
      return null;
    }
  }

  private static class ParameterCellModifier implements ICellModifier {
    private TableViewer viewer;
    private String[] columnNames;

    private ParameterCellModifier(String[] columnNames, TableViewer viewer) {
      this.columnNames = columnNames;
      this.viewer = viewer;
    }

    @Override
    public boolean canModify(Object element, String property) {
      return true;
    }

    @Override
    public Object getValue(Object element, String property) {
      int columnIndex = Arrays.asList(columnNames).indexOf(property);
      DbVariableHandlerParameter parameter = (DbVariableHandlerParameter) element;

      switch (columnIndex) {
        case 0:
          return parameter.getVariableName();
        case 1:
          return parameter.getColumnName();
      }
      return null;
    }

    @Override
    public void modify(Object element, String property, Object value) {
      int columnIndex = Arrays.asList(columnNames).indexOf(property);

      TableItem item = (TableItem) element;
      DbVariableHandlerParameter parameter = (DbVariableHandlerParameter) item.getData();

      switch (columnIndex) {
        case 0:
          parameter.setVariableName(((String) value).trim());
          break;
        case 1:
          parameter.setColumnName(((String) value).trim());
          break;
      }

      viewer.update(parameter, null);
    }
  }
}
