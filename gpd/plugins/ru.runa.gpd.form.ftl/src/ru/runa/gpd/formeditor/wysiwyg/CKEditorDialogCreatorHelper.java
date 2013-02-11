package ru.runa.gpd.formeditor.wysiwyg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.formeditor.ftl.FormatTag.FtlFormat;
import ru.runa.gpd.formeditor.ftl.MethodTag;
import ru.runa.gpd.formeditor.ftl.MethodTag.OptionalValue;
import ru.runa.gpd.formeditor.ftl.MethodTag.Param;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.IOUtils;

public class CKEditorDialogCreatorHelper {
    private interface CKElement {
        public void write(StringBuilder writer, int prefix);
    }

    @SuppressWarnings("unchecked")
    private abstract static class CKElementBase<T extends CKElementBase> implements CKElement {
        @Override
        public abstract void write(StringBuilder writer, int prefix);

        String id = null, label = null;

        public T setId(String id) {
            this.id = id;
            return (T) this;
        }

        public T setLabel(String label) {
            this.label = label;
            return (T) this;
        }

        protected StringBuilder writePrefix(StringBuilder writer, int prefix) {
            while (prefix > 0) {
                writer.append("    ");
                --prefix;
            }
            return writer;
        }
    }

    private static class CKTextElement extends CKElementBase<CKTextElement> {
        @Override
        public void write(StringBuilder writer, int prefix) {
            if (id == null || label == null) {
                throw new RuntimeException();
            }
            writePrefix(writer, prefix).append("{\n");
            prefix++;
            writePrefix(writer, prefix).append("id : ").append(id).append(",\n");
            writePrefix(writer, prefix).append("label : ").append(label).append(",\n");
            writePrefix(writer, prefix).append("type : 'text',\n");
            prefix--;
            writePrefix(writer, prefix).append("}");
        }
    }

    private static class CKSelectElement extends CKElementBase<CKSelectElement> {
        String defaultValue = null;
        List<String> selectItems = new ArrayList<String>();
        List<String> elementCallbacks = new ArrayList<String>();

        public CKSelectElement setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public CKSelectElement addItem(String label, String itemId) {
            selectItems.add("[" + label + ", " + itemId + "]");
            if (this.defaultValue == null) {
                defaultValue = itemId;
            }
            return this;
        }

        public CKSelectElement addCallback(String function) {
            elementCallbacks.add(function);
            return this;
        }

        @Override
        public void write(StringBuilder writer, int prefix) {
            if (id == null || label == null) {
                throw new RuntimeException();
            }
            writePrefix(writer, prefix).append("{\n");
            prefix++;
            writePrefix(writer, prefix).append("id : ").append(id).append(",\n");
            writePrefix(writer, prefix).append("label : ").append(label).append(",\n");
            writePrefix(writer, prefix).append("type : 'select',\n");
            writePrefix(writer, prefix).append("style : 'width : 100%;',\n");
            if (defaultValue != null) {
                writePrefix(writer, prefix).append("'default' : ").append(defaultValue).append(",\n");
            }
            writePrefix(writer, prefix).append("items :\n");
            writePrefix(writer, prefix).append("[\n");
            prefix++;
            for (int i = 0; i < selectItems.size(); ++i) {
                writePrefix(writer, prefix).append(selectItems.get(i)).append(i < selectItems.size() - 1 ? ",\n" : "\n");
            }
            prefix--;
            writePrefix(writer, prefix).append("]").append(elementCallbacks.isEmpty() ? "\n" : ",\n");
            for (int i = 0; i < elementCallbacks.size(); ++i) {
                writePrefix(writer, prefix).append(elementCallbacks.get(i)).append(i < elementCallbacks.size() - 1 ? ",\n" : "\n");
            }
            prefix--;
            writePrefix(writer, prefix).append("}");
        }
    }

    private static class CKVboxElement extends CKElementBase<CKVboxElement> {
        List<CKElement> childrens = new ArrayList<CKElement>();

        public CKVboxElement addChildren(CKElement element) {
            this.childrens.add(element);
            return this;
        }

        @Override
        public void write(StringBuilder writer, int prefix) {
            writePrefix(writer, prefix).append("{\n");
            ++prefix;
            writePrefix(writer, prefix).append("id : " + id + ",\n");
            writePrefix(writer, prefix).append("type : 'vbox',\n");
            writePrefix(writer, prefix).append("children : \n");
            writePrefix(writer, prefix).append("[\n");
            ++prefix;
            for (int i = 0; i < childrens.size(); ++i) {
                childrens.get(i).write(writer, prefix);
                writer.append(i < childrens.size() - 1 ? ",\n" : "\n");
            }
            --prefix;
            writePrefix(writer, prefix).append("]\n");
            --prefix;
            writePrefix(writer, prefix).append("}");
        }
    }

    public static String createFtlMethodDialog() throws IOException {
        StringBuilder result = new StringBuilder();
        result.append(IOUtils.readStream(FtlFormat.class.getResourceAsStream("ckeditor.ftl.method.dialog.start")));
        List<MethodTag> tagsList = MethodTag.getEnabled();
        {
            CKSelectElement selectElement = new CKSelectElement();
            selectElement.setId("ELEMENT_TAG_TYPE").setLabel("editor.lang.FreemarkerTags.MethodTitle").setDefaultValue(tagsList.isEmpty() ? null : "'" + tagsList.get(0).id + "'");
            for (MethodTag tagInfo : tagsList) {
                selectElement.addItem("'" + tagInfo.name + "'", "'" + tagInfo.id + "'");
            }
            selectElement.addCallback(/* Setup function is called to set element value (if we want to look at freemarker tag properties) */
            "setup : function( element ){	\n" + "	this.setValue( element.getAttribute( 'ftltagname' ) || '' );\n" + "}\n");
            selectElement.addCallback(/*
                                       * Commit function is called if OK button pressed (selected value must be stored at real freemarker html
                                       * element)
                                       */
            "commit : function( data ){\n" + "	if( this.getValue() )\n" + "		data.element.setAttribute( 'ftltagname', this.getValue() );\n" + "	else\n"
                    + "		data.element.removeAttribute( 'ftltagname' );\n" + "}\n");
            selectElement.addCallback(/*
                                       * onChange is called if selection is changed. We need to show appriciate parameters for currently selected
                                       * freemarker function
                                       */
            "onChange : function(){\n" + "    var test = function(e){\n" + "        if(e.id.indexOf('FtlTagVBox') == e.id.length-10){\n"
                    + "            e.getElement().getParent().getParent().hide();\n" + "        };\n" + "    };\n" + "    this.getDialog().foreach(test);\n"
                    + "    this.getDialog().getContentElement( 'mainTab', this.getValue() + 'FtlTagVBox' ).getElement().getParent().getParent().show();\n" + "}\n");
            selectElement.write(result, 4);
        }
        for (MethodTag tagInfo : tagsList) {
            CKVboxElement box = new CKVboxElement().setId("'" + tagInfo.id + "FtlTagVBox'");
            int paramCounter = 0;
            for (Param param : tagInfo.params) {
                if (param.isCombo() || param.isRichCombo() || param.isVarCombo()) {
                    CKSelectElement selectElement = new CKSelectElement();
                    selectElement.setId("'" + tagInfo.id + "_FtlTagParam_" + paramCounter + "'").setLabel("'" + param.label + "'");
                    for (OptionalValue option : param.optionalValues) {
                        if (option.container) {
                            for (Variable variable : WYSIWYGHTMLEditor.getCurrent().getVariablesList(true)) {
                                if (option.useFilter && !VariableFormatRegistry.isApplicable(variable, option.filterType)) {
                                    continue;
                                }
                                selectElement.addItem("'" + variable.getName() + "'", "'" + variable.getName() + "'");
                            }
                        } else {
                            selectElement.addItem("'" + option.value + "'", "'" + option.name + "'");
                        }
                    }
                    selectElement.addCallback(/* Setup function is called to set element value (if we want to look at freemarker tag properties) */
                    "setup : function( element ){	\n" + "	if (this.id.indexOf(element.getAttribute( 'ftltagname' )) == 0){\n"
                            + "		var paramCount = this.id.charAt(this.id.length - 1);\n" + "		var start = 0; var end = -1;\n" + "		while(paramCount >= 0) {\n"
                            + "			if(end != -1){start = end + 1;}\n" + "			end = element.getAttribute( 'ftltagparams' ).indexOf('|', start);\n" + "			paramCount--;\n" + "		}\n"
                            + "		if(end == -1) end = element.getAttribute( 'ftltagparams' ).length;\n"
                            + "		this.setValue( element.getAttribute( 'ftltagparams' ).substring(start, end) );\n" + "	}\n" + "}\n");
                    selectElement.addCallback(/*
                                               * Commit function is called if OK button pressed (selected value must be stored at real freemarker html
                                               * element)
                                               */
                    "commit : function( data ){\n" + "	if(!this.isVisible()) return;" + "	if(data.element.getAttribute('ftltagparams') != null)"
                            + "		data.element.setAttribute( 'ftltagparams', data.element.getAttribute('ftltagparams') + '|' + this.getValue() );\n" + "	else\n"
                            + "		data.element.setAttribute( 'ftltagparams', this.getValue() );\n" + "}\n");
                    box.addChildren(selectElement);
                } else {
                    CKTextElement text = new CKTextElement().setId("'" + tagInfo.id + "_FtlTagParam_" + paramCounter + "'").setLabel("'" + param.label + "'");
                    box.addChildren(text);
                }
                paramCounter++;
            }
            result.append(",\n");
            box.write(result, 4);
        }
        result.append(IOUtils.readStream(FtlFormat.class.getResourceAsStream("ckeditor.ftl.method.dialog.end")));
        return result.toString();
    }
}
