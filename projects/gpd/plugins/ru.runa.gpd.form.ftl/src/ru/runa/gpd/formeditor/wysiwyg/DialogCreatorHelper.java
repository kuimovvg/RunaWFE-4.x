package ru.runa.gpd.formeditor.wysiwyg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.formeditor.ftl.MethodTag.OptionalValue;
import ru.runa.gpd.formeditor.ftl.MethodTag.Param;
import ru.runa.gpd.formeditor.ftl.bean.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.bean.FtlComponent;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.property.EditableSyncPropertyDescriptor;
import ru.runa.gpd.ui.dialog.EditableSyncDialog;
import ru.runa.gpd.ui.dialog.MultipleMapListDialog;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DialogCreatorHelper {

    private static int compId;

    public static boolean openDialog(int componentId) {
        compId = componentId;
        final FtlComponent component = WYSIWYGHTMLEditor.getCurrent().getComponent(componentId);
        Openable dialog = new CkMethodTagDialog(component);
        return dialog.openDialog();
    }

    interface Openable {
        boolean openDialog();
    }

    private static class CkMethodTagDialog extends Dialog implements Openable {

        FtlComponent component;
        MethodTag tmpTag;

        final static Map<Param, String> paramsMap = Maps.newHashMap();

        public CkMethodTagDialog(FtlComponent component) {
            super(WYSIWYGHTMLEditor.getCurrent().getSite().getShell());
            this.component = component;
            paramsMap.clear();
        }

        @Override
        protected void configureShell(Shell newShell) {
            super.configureShell(newShell);
            newShell.setText(Localization.getString("DialogCreatorHelper.title"));
        }

        @Override
        protected boolean isResizable() {
            return true;
        }

        @Override
        protected Control createDialogArea(Composite parent) {
            final Composite area = (Composite) super.createDialogArea(parent);
            GridLayout gl = new GridLayout(2, false);
            area.setLayout(gl);

            GridData gridData = null;
            {
                gridData = new GridData();
                gridData.horizontalSpan = 2;
                Label label = new Label(area, SWT.NO_BACKGROUND);
                label.setText(Localization.getString("DialogCreatorHelper.tag"));
                label.setLayoutData(gridData);
            }

            final Combo tagsCombo = new Combo(area, SWT.READ_ONLY);
            GridData data = new GridData();
            data.horizontalSpan = 2;
            tagsCombo.setLayoutData(data);

            fillTagsCombo(tagsCombo);

            tmpTag = component.getType();

            tagsCombo.addSelectionListener(getChangeTagListener(area, tagsCombo));

            drawDialog(area, false);
            return area;
        }

        private void fillTagsCombo(final Combo tagsCombo) {
            int i = 0;
            for (MethodTag tag : MethodTag.getEnabled()) {
                tagsCombo.add(tag.name);
                if (tag.name.equals(component.getType().name)) {
                    tagsCombo.select(i);
                }
                i++;
            }
        }

        private SelectionAdapter getChangeTagListener(final Composite area, final Combo tagsCombo) {
            return new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    super.widgetSelected(e);
                    String tag = tagsCombo.getText();
                    if (!tmpTag.name.equals(tag)) {
                        changeTag(area, tag);
                        paramsMap.clear();
                    }
                }
            };
        }

        protected void changeTag(Composite parent, String tag) {
            for (MethodTag methodTag : MethodTag.getEnabled()) {
                if (methodTag.name.equals(tag)) {
                    tmpTag = methodTag;
                    break;
                }
            }
            Control[] children = parent.getChildren();
            for (int i = 2; i < children.length; i++) {
                children[i].dispose();
            }
            drawDialog(parent, true);
        }

        private void drawDialog(Composite parent, final boolean redraw) {

            for (final Param param : tmpTag.params) {
                GridData gridData = null;
                {
                    gridData = new GridData();
                    gridData.horizontalSpan = 2;
                    Label label = new Label(parent, SWT.NO_BACKGROUND);
                    label.setText(param.label);
                    label.setLayoutData(gridData);
                }
                boolean filterVariablesWithSpaces = false;// param.isVarCombo() || param.isRichCombo();
                if (param.isCombo() || param.isVarCombo()) {
                    final Combo combo = new Combo(parent, SWT.READ_ONLY);
                    drawCombo(combo, param, redraw, filterVariablesWithSpaces);
                    {
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.horizontalSpan = 2;
                        combo.setLayoutData(gridData);
                    }
                    combo.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            changeComboParam(param, combo);
                        }
                    });

                } else if (param.isRichCombo()) {

                    final String typeName = param.getVariableTypeFilter();

                    final Text text = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
                    {
                        gridData = new GridData(GridData.FILL_HORIZONTAL);
                        gridData.minimumWidth = 200;
                        gridData.minimumHeight = 200;
                        text.setLayoutData(gridData);
                    }
                    Button selectButton = new Button(parent, SWT.PUSH);
                    {
                        selectButton.setText("...");
                        selectButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
                    }
                    drawRichCombo(selectButton, text, param, typeName, redraw, filterVariablesWithSpaces);

                } else if (param.isTextForIDGeneration()) {
                    final Text text = new Text(parent, SWT.BORDER);
                    text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                    if (!redraw) {
                        text.setText(param.label);
                    }
                    text.addModifyListener(new ModifyListener() {

                        @Override
                        public void modifyText(ModifyEvent e) {
                            changeParam(param, text.getText());
                        }
                    });
                    paramsMap.put(param, param.label);
                }
            }
            redraw(parent);
        }

        private void drawRichCombo(Button selectButton, final Text text, final Param param, final String typeName, final boolean redraw,
                boolean filterVariablesWithSpaces) {
            for (OptionalValue option : param.optionalValues) {

                if (option.container) {
                    final List<Variable> variables;
                    variables = getVariables(filterVariablesWithSpaces, option);
                    Collections.sort(variables);

                    if (!redraw) {
                        IPropertyDescriptor[] propertyDescriptors = component.getPropertyDescriptors();
                        for (IPropertyDescriptor propertyDescriptor : propertyDescriptors) {
                            if (propertyDescriptor.getDisplayName().equals(param.label)) {
                                if (propertyDescriptor instanceof EditableSyncPropertyDescriptor) {
                                    final EditableSyncPropertyDescriptor descriptor = (EditableSyncPropertyDescriptor) propertyDescriptor;
                                    text.setText(convertValueFromString(descriptor.getRawValue()));
                                    paramsMap.put(param, descriptor.getRawValue());
                                    selectButton.addSelectionListener(new SelectionAdapter() {
                                        @Override
                                        public void widgetSelected(SelectionEvent e) {
                                            handleCkick(param, text, typeName, variables, descriptor, redraw);
                                        }
                                    });
                                    break;
                                }
                            }
                        }
                    } else {
                        selectButton.addSelectionListener(new SelectionAdapter() {
                            @Override
                            public void widgetSelected(SelectionEvent e) {
                                handleCkick(param, text, typeName, variables, null, redraw);
                            }
                        });
                    }
                }
            }
        }

        @SuppressWarnings("rawtypes")
        private void drawCombo(final Combo combo, final Param param, final boolean redraw, boolean filterVariablesWithSpaces) {
            for (OptionalValue option : param.optionalValues) {

                if (option.container) {
                    List<Variable> variables = getVariables(filterVariablesWithSpaces, option);
                    Collections.sort(variables);
                    int index = 0;
                    boolean selected = false;
                    for (Variable variable : variables) {
                        combo.add(variable.getName());
                        if (!selected && !redraw) {
                            for (ComponentParameter c : component.getParameters()) {
                                if (c.getParam().label.equals(param.label) && c.getParam().typeName.equals(param.typeName)) {
                                    if (variable.getName().equals(c.getStringValue())) {
                                        combo.select(index);
                                        selected = true;
                                        paramsMap.put(param, variable.getName());
                                    }
                                }
                            }
                        }
                        index++;
                    }
                } else {
                    combo.add(option.value);
                    combo.setData(option.value, option.name);
                    if (!redraw) {
                        for (ComponentParameter c : component.getParameters()) {
                            if (c.getParam().equals(param)) {
                                combo.select(Integer.parseInt(c.getRawValue().toString()));
                                if (!paramsMap.containsKey(param)) {
                                    paramsMap.put(param, c.getStringValue());
                                }
                            }
                        }
                    }
                }
            }
        }

        private void redraw(Composite parent) {
            parent.layout(true, true);
            parent.redraw();
            parent.update();
        }

        private List<Variable> getVariables(boolean filterVariablesWithSpaces, OptionalValue option) {
            List<Variable> variables;
            if (option.useFilter) {
                variables = new ArrayList<Variable>(WYSIWYGHTMLEditor.getCurrent().getVariables(filterVariablesWithSpaces, option.filterType)
                        .values());
            } else {
                variables = new ArrayList<Variable>(WYSIWYGHTMLEditor.getCurrent().getVariables(filterVariablesWithSpaces, null).values());
            }
            return variables;
        }

        @Override
        public boolean openDialog() {
            if (open() == IDialogConstants.OK_ID) {
                WYSIWYGHTMLEditor editor = WYSIWYGHTMLEditor.getCurrent();
                if (!tmpTag.equals(component.getType())) {
                    FtlComponent comp = editor.createComponentWithoutAddToList(tmpTag.id);
                    editor.replaceComponent(compId, comp);
                    changeParameter(comp);
                } else {
                    changeParameter(component);
                }
                return true;
            }
            return false;
        }

        @SuppressWarnings("rawtypes")
        private void changeParameter(FtlComponent comp) {
            for (ComponentParameter p : comp.getParameters()) {
                if (paramsMap.containsKey(p.getParam())) {
                    p.initValue(paramsMap.get(p.getParam()));
                    comp.firePropertyChange("", 0, null);
                }
            }
        }

        protected String convertValueFromString(String valueStr) {
            if (valueStr == null || valueStr.trim().isEmpty()) {
                return "";
            }
            return valueStr;
        }

        private void handleCkick(Param param, Text text, final String typeName, final List<Variable> variables,
                final EditableSyncPropertyDescriptor descriptor, boolean redraw) {
            String result = null;
            Map<String, Boolean> map = Maps.<String, Boolean> newLinkedHashMap();
            if (!redraw) {
                String[] vals = convertValueFromString(descriptor.getRawValue()).trim().split(",");

                List<String> selected = Lists.newLinkedList();
                for (String string : vals) {
                    selected.add(string.trim());
                }

                if (isMapOrList(typeName)) {
                    map.clear();
                    for (String selectedItem : selected) {
                        map.put(selectedItem, true);
                    }
                    for (Variable variable : variables) {
                        if (!map.containsKey(variable.getName())) {
                            map.put(variable.getName(), false);
                        }
                    }
                    MultipleMapListDialog dialog = new MultipleMapListDialog(typeName, descriptor.getMapValues());
                    result = (String) dialog.openDialog();

                } else {
                    EditableSyncDialog dialog = new EditableSyncDialog(typeName, descriptor.getValues(), descriptor.getRawValue());
                    result = (String) dialog.openDialog();
                }
            } else {
                if (isMapOrList(typeName)) {
                    map.clear();
                    for (Variable variable : variables) {
                        map.put(variable.getName(), false);
                    }
                    MultipleMapListDialog dialog = new MultipleMapListDialog(typeName, map);
                    result = (String) dialog.openDialog();

                } else {
                    EditableSyncDialog dialog = new EditableSyncDialog(typeName, Lists.<String> newArrayList(), "");
                    result = (String) dialog.openDialog();
                }
            }

            if (result != null) {
                text.setText(result);
                changeParam(param, result);
            }
        }

        protected void changeComboParam(Param param, Combo combo) {
            String data = (String) combo.getData(combo.getText());
            if (data != null) {
                changeParam(param, data);
            } else {
                changeParam(param, combo.getText());
            }
        }

        private void changeParam(Param param, String newValue) {
            String oldValue = paramsMap.get(param);
            if (!newValue.equals(oldValue)) {
                paramsMap.remove(param);
                paramsMap.put(param, newValue);
            }
        }

        private static boolean isMapOrList(String typeName) {
            return VariableFormatRegistry.isAssignableFrom(Map.class.getName(), typeName)
                    || VariableFormatRegistry.isAssignableFrom(List.class.getName(), typeName);
        }
    }
}
