//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl.impl;

public class FormalParametersTypeImpl implements ru.runa.xpdl.generated.bpmnxpdl.FormalParametersType, com.sun.xml.bind.JAXBObject, ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallableObject, ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.XMLSerializable, ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.ValidatableObject
{

    protected ru.runa.xpdl.generated.bpmnxpdl.AnyType _Extensions;
    protected com.sun.xml.bind.util.ListImpl _Any;
    protected com.sun.xml.bind.util.ListImpl _FormalParameter;
    protected com.sun.xml.bind.util.ListImpl _TC1025FormalParameter;
    public final static java.lang.Class version = (ru.runa.xpdl.generated.bpmnxpdl.impl.JAXBVersion.class);
    private static com.sun.msv.grammar.Grammar schemaFragment;

    private final static java.lang.Class PRIMARY_INTERFACE_CLASS() {
        return (ru.runa.xpdl.generated.bpmnxpdl.FormalParametersType.class);
    }

    public ru.runa.xpdl.generated.bpmnxpdl.AnyType getExtensions() {
        return _Extensions;
    }

    public void setExtensions(ru.runa.xpdl.generated.bpmnxpdl.AnyType value) {
        _Extensions = value;
    }

    protected com.sun.xml.bind.util.ListImpl _getAny() {
        if (_Any == null) {
            _Any = new com.sun.xml.bind.util.ListImpl(new java.util.ArrayList());
        }
        return _Any;
    }

    public java.util.List getAny() {
        return _getAny();
    }

    protected com.sun.xml.bind.util.ListImpl _getFormalParameter() {
        if (_FormalParameter == null) {
            _FormalParameter = new com.sun.xml.bind.util.ListImpl(new java.util.ArrayList());
        }
        return _FormalParameter;
    }

    public java.util.List getFormalParameter() {
        return _getFormalParameter();
    }

    protected com.sun.xml.bind.util.ListImpl _getTC1025FormalParameter() {
        if (_TC1025FormalParameter == null) {
            _TC1025FormalParameter = new com.sun.xml.bind.util.ListImpl(new java.util.ArrayList());
        }
        return _TC1025FormalParameter;
    }

    public java.util.List getTC1025FormalParameter() {
        return _getTC1025FormalParameter();
    }

    public ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallingEventHandler createUnmarshaller(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallingContext context) {
        return new ru.runa.xpdl.generated.bpmnxpdl.impl.FormalParametersTypeImpl.Unmarshaller(context);
    }

    public void serializeBody(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx2 = 0;
        final int len2 = ((_Any == null)? 0 :_Any.size());
        int idx3 = 0;
        final int len3 = ((_FormalParameter == null)? 0 :_FormalParameter.size());
        int idx4 = 0;
        final int len4 = ((_TC1025FormalParameter == null)? 0 :_TC1025FormalParameter.size());
        if ((((_TC1025FormalParameter == null)? 0 :_TC1025FormalParameter.size())>= 1)&&(((_FormalParameter == null)? 0 :_FormalParameter.size()) == 0)) {
            while (idx4 != len4) {
                if (_TC1025FormalParameter.get(idx4) instanceof javax.xml.bind.Element) {
                    context.childAsBody(((com.sun.xml.bind.JAXBObject) _TC1025FormalParameter.get(idx4 ++)), "TC1025FormalParameter");
                } else {
                    context.startElement("http://www.wfmc.org/2002/XPDL1.0", "TC1025FormalParameter");
                    int idx_0 = idx4;
                    context.childAsURIs(((com.sun.xml.bind.JAXBObject) _TC1025FormalParameter.get(idx_0 ++)), "TC1025FormalParameter");
                    context.endNamespaceDecls();
                    int idx_1 = idx4;
                    context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _TC1025FormalParameter.get(idx_1 ++)), "TC1025FormalParameter");
                    context.endAttributes();
                    context.childAsBody(((com.sun.xml.bind.JAXBObject) _TC1025FormalParameter.get(idx4 ++)), "TC1025FormalParameter");
                    context.endElement();
                }
            }
        } else {
            if ((((_TC1025FormalParameter == null)? 0 :_TC1025FormalParameter.size()) == 0)&&(((_FormalParameter == null)? 0 :_FormalParameter.size())>= 1)) {
                while (idx3 != len3) {
                    if (_FormalParameter.get(idx3) instanceof javax.xml.bind.Element) {
                        context.childAsBody(((com.sun.xml.bind.JAXBObject) _FormalParameter.get(idx3 ++)), "FormalParameter");
                    } else {
                        context.startElement("http://www.wfmc.org/2008/XPDL2.1", "FormalParameter");
                        int idx_2 = idx3;
                        context.childAsURIs(((com.sun.xml.bind.JAXBObject) _FormalParameter.get(idx_2 ++)), "FormalParameter");
                        context.endNamespaceDecls();
                        int idx_3 = idx3;
                        context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _FormalParameter.get(idx_3 ++)), "FormalParameter");
                        context.endAttributes();
                        context.childAsBody(((com.sun.xml.bind.JAXBObject) _FormalParameter.get(idx3 ++)), "FormalParameter");
                        context.endElement();
                    }
                }
            }
        }
        if (_Extensions!= null) {
            context.startElement("http://www.wfmc.org/2008/XPDL2.1", "Extensions");
            context.childAsURIs(((com.sun.xml.bind.JAXBObject) _Extensions), "Extensions");
            context.endNamespaceDecls();
            context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _Extensions), "Extensions");
            context.endAttributes();
            context.childAsBody(((com.sun.xml.bind.JAXBObject) _Extensions), "Extensions");
            context.endElement();
            while (idx2 != len2) {
                context.childAsBody(((com.sun.xml.bind.JAXBObject) _Any.get(idx2 ++)), "Any");
            }
        }
    }

    public void serializeAttributes(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx2 = 0;
        final int len2 = ((_Any == null)? 0 :_Any.size());
        int idx3 = 0;
        final int len3 = ((_FormalParameter == null)? 0 :_FormalParameter.size());
        int idx4 = 0;
        final int len4 = ((_TC1025FormalParameter == null)? 0 :_TC1025FormalParameter.size());
        if ((((_TC1025FormalParameter == null)? 0 :_TC1025FormalParameter.size())>= 1)&&(((_FormalParameter == null)? 0 :_FormalParameter.size()) == 0)) {
            while (idx4 != len4) {
                if (_TC1025FormalParameter.get(idx4) instanceof javax.xml.bind.Element) {
                    context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _TC1025FormalParameter.get(idx4 ++)), "TC1025FormalParameter");
                } else {
                    idx4 += 1;
                }
            }
        } else {
            if ((((_TC1025FormalParameter == null)? 0 :_TC1025FormalParameter.size()) == 0)&&(((_FormalParameter == null)? 0 :_FormalParameter.size())>= 1)) {
                while (idx3 != len3) {
                    if (_FormalParameter.get(idx3) instanceof javax.xml.bind.Element) {
                        context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _FormalParameter.get(idx3 ++)), "FormalParameter");
                    } else {
                        idx3 += 1;
                    }
                }
            }
        }
        if (_Extensions!= null) {
            while (idx2 != len2) {
                idx2 += 1;
            }
        }
    }

    public void serializeURIs(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx2 = 0;
        final int len2 = ((_Any == null)? 0 :_Any.size());
        int idx3 = 0;
        final int len3 = ((_FormalParameter == null)? 0 :_FormalParameter.size());
        int idx4 = 0;
        final int len4 = ((_TC1025FormalParameter == null)? 0 :_TC1025FormalParameter.size());
        if ((((_TC1025FormalParameter == null)? 0 :_TC1025FormalParameter.size())>= 1)&&(((_FormalParameter == null)? 0 :_FormalParameter.size()) == 0)) {
            while (idx4 != len4) {
                if (_TC1025FormalParameter.get(idx4) instanceof javax.xml.bind.Element) {
                    context.childAsURIs(((com.sun.xml.bind.JAXBObject) _TC1025FormalParameter.get(idx4 ++)), "TC1025FormalParameter");
                } else {
                    idx4 += 1;
                }
            }
        } else {
            if ((((_TC1025FormalParameter == null)? 0 :_TC1025FormalParameter.size()) == 0)&&(((_FormalParameter == null)? 0 :_FormalParameter.size())>= 1)) {
                while (idx3 != len3) {
                    if (_FormalParameter.get(idx3) instanceof javax.xml.bind.Element) {
                        context.childAsURIs(((com.sun.xml.bind.JAXBObject) _FormalParameter.get(idx3 ++)), "FormalParameter");
                    } else {
                        idx3 += 1;
                    }
                }
            }
        }
        if (_Extensions!= null) {
            while (idx2 != len2) {
                idx2 += 1;
            }
        }
    }

    public java.lang.Class getPrimaryInterface() {
        return (ru.runa.xpdl.generated.bpmnxpdl.FormalParametersType.class);
    }

    public com.sun.msv.verifier.DocumentDeclaration createRawValidator() {
        if (schemaFragment == null) {
            schemaFragment = com.sun.xml.bind.validator.SchemaDeserializer.deserialize((
 "\u00ac\u00ed\u0000\u0005sr\u0000\u001fcom.sun.msv.grammar.SequenceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.su"
+"n.msv.grammar.BinaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0004exp1t\u0000 Lcom/sun/msv/gra"
+"mmar/Expression;L\u0000\u0004exp2q\u0000~\u0000\u0002xr\u0000\u001ecom.sun.msv.grammar.Expressi"
+"on\u00f8\u0018\u0082\u00e8N5~O\u0002\u0000\u0002L\u0000\u0013epsilonReducibilityt\u0000\u0013Ljava/lang/Boolean;L\u0000\u000b"
+"expandedExpq\u0000~\u0000\u0002xpppsr\u0000\u001dcom.sun.msv.grammar.ChoiceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
+"\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0001ppsq\u0000~\u0000\u0006ppsr\u0000 com.sun.msv.grammar.OneOrMoreExp\u0000\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001ccom.sun.msv.grammar.UnaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\u0003expq\u0000~\u0000"
+"\u0002xq\u0000~\u0000\u0003sr\u0000\u0011java.lang.Boolean\u00cd r\u0080\u00d5\u009c\u00fa\u00ee\u0002\u0000\u0001Z\u0000\u0005valuexp\u0000psq\u0000~\u0000\u0006q\u0000~"
+"\u0000\rpsr\u0000\'com.sun.msv.grammar.trex.ElementPattern\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\t"
+"nameClasst\u0000\u001fLcom/sun/msv/grammar/NameClass;xr\u0000\u001ecom.sun.msv.g"
+"rammar.ElementExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002Z\u0000\u001aignoreUndeclaredAttributesL\u0000\f"
+"contentModelq\u0000~\u0000\u0002xq\u0000~\u0000\u0003q\u0000~\u0000\rp\u0000sq\u0000~\u0000\u0006ppsq\u0000~\u0000\tq\u0000~\u0000\rpsr\u0000 com.su"
+"n.msv.grammar.AttributeExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003expq\u0000~\u0000\u0002L\u0000\tnameClass"
+"q\u0000~\u0000\u0010xq\u0000~\u0000\u0003q\u0000~\u0000\rpsr\u00002com.sun.msv.grammar.Expression$AnyStrin"
+"gExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003sq\u0000~\u0000\f\u0001q\u0000~\u0000\u0018sr\u0000 com.sun.msv.gram"
+"mar.AnyNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.sun.msv.grammar.NameClass"
+"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpsr\u00000com.sun.msv.grammar.Expression$EpsilonExpre"
+"ssion\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003q\u0000~\u0000\u0019q\u0000~\u0000\u001esr\u0000#com.sun.msv.grammar.Simp"
+"leNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\tlocalNamet\u0000\u0012Ljava/lang/String;L\u0000\fna"
+"mespaceURIq\u0000~\u0000 xq\u0000~\u0000\u001bt\u0000(ru.runa.xpdl.generated.bpmnxpdl.TC1025FormalParam"
+"etert\u0000+http://java.sun.com/jaxb/xjc/dummy-elementssq\u0000~\u0000\u000fq\u0000~\u0000"
+"\rp\u0000sq\u0000~\u0000\u0000ppsq\u0000~\u0000\u000fpp\u0000sq\u0000~\u0000\u0006ppsq\u0000~\u0000\tq\u0000~\u0000\rpsq\u0000~\u0000\u0015q\u0000~\u0000\rpq\u0000~\u0000\u0018q\u0000~"
+"\u0000\u001cq\u0000~\u0000\u001esq\u0000~\u0000\u001ft\u0000,ru.runa.xpdl.generated.bpmnxpdl.TC1025FormalParameterType"
+"q\u0000~\u0000#sq\u0000~\u0000\u0006ppsq\u0000~\u0000\u0015q\u0000~\u0000\rpsr\u0000\u001bcom.sun.msv.grammar.DataExp\u0000\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\u0002dtt\u0000\u001fLorg/relaxng/datatype/Datatype;L\u0000\u0006exceptq\u0000~\u0000\u0002"
+"L\u0000\u0004namet\u0000\u001dLcom/sun/msv/util/StringPair;xq\u0000~\u0000\u0003ppsr\u0000\"com.sun.m"
+"sv.datatype.xsd.QnameType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000*com.sun.msv.datatype"
+".xsd.BuiltinAtomicType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000%com.sun.msv.datatype.xs"
+"d.ConcreteType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\'com.sun.msv.datatype.xsd.XSData"
+"typeImpl\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\fnamespaceUriq\u0000~\u0000 L\u0000\btypeNameq\u0000~\u0000 L\u0000\nwh"
+"iteSpacet\u0000.Lcom/sun/msv/datatype/xsd/WhiteSpaceProcessor;xpt"
+"\u0000 http://www.w3.org/2001/XMLSchemat\u0000\u0005QNamesr\u00005com.sun.msv.da"
+"tatype.xsd.WhiteSpaceProcessor$Collapse\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000,com.su"
+"n.msv.datatype.xsd.WhiteSpaceProcessor\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpsr\u00000com.s"
+"un.msv.grammar.Expression$NullSetExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003"
+"q\u0000~\u0000\rpsr\u0000\u001bcom.sun.msv.util.StringPair\u00d0t\u001ejB\u008f\u008d\u00a0\u0002\u0000\u0002L\u0000\tlocalName"
+"q\u0000~\u0000 L\u0000\fnamespaceURIq\u0000~\u0000 xpq\u0000~\u00009q\u0000~\u00008sq\u0000~\u0000\u001ft\u0000\u0004typet\u0000)http://"
+"www.w3.org/2001/XMLSchema-instanceq\u0000~\u0000\u001esq\u0000~\u0000\u001ft\u0000\u0015TC1025Formal"
+"Parametert\u0000 http://www.wfmc.org/2002/XPDL1.0q\u0000~\u0000\u001esq\u0000~\u0000\tppsq\u0000"
+"~\u0000\u0006ppsq\u0000~\u0000\u000fpp\u0000sq\u0000~\u0000\u0006ppsq\u0000~\u0000\tq\u0000~\u0000\rpsq\u0000~\u0000\u0015q\u0000~\u0000\rpq\u0000~\u0000\u0018q\u0000~\u0000\u001cq\u0000~\u0000"
+"\u001esq\u0000~\u0000\u001ft\u0000\"ru.runa.xpdl.generated.bpmnxpdl.FormalParameterq\u0000~\u0000#sq\u0000~\u0000\u000fpp\u0000sq"
+"\u0000~\u0000\u0000ppsq\u0000~\u0000\u000fpp\u0000sq\u0000~\u0000\u0006ppsq\u0000~\u0000\tq\u0000~\u0000\rpsq\u0000~\u0000\u0015q\u0000~\u0000\rpq\u0000~\u0000\u0018q\u0000~\u0000\u001cq\u0000~"
+"\u0000\u001esq\u0000~\u0000\u001ft\u0000&ru.runa.xpdl.generated.bpmnxpdl.FormalParameterTypeq\u0000~\u0000#sq\u0000~\u0000\u0006"
+"ppsq\u0000~\u0000\u0015q\u0000~\u0000\rpq\u0000~\u00001q\u0000~\u0000Aq\u0000~\u0000\u001esq\u0000~\u0000\u001ft\u0000\u000fFormalParametert\u0000 http"
+"://www.wfmc.org/2008/XPDL2.1sq\u0000~\u0000\u0006ppsq\u0000~\u0000\u0000q\u0000~\u0000\rpsq\u0000~\u0000\u000fq\u0000~\u0000\rp"
+"\u0000sq\u0000~\u0000\u0000ppsq\u0000~\u0000\u000fpp\u0000sq\u0000~\u0000\u0006ppsq\u0000~\u0000\tq\u0000~\u0000\rpsq\u0000~\u0000\u0015q\u0000~\u0000\rpq\u0000~\u0000\u0018q\u0000~\u0000\u001c"
+"q\u0000~\u0000\u001esq\u0000~\u0000\u001ft\u0000\u001agenerated.bpmnxpdl.AnyTypeq\u0000~\u0000#sq\u0000~\u0000\u0006ppsq\u0000~\u0000\u0015q"
+"\u0000~\u0000\rpq\u0000~\u00001q\u0000~\u0000Aq\u0000~\u0000\u001esq\u0000~\u0000\u001ft\u0000\nExtensionsq\u0000~\u0000[sq\u0000~\u0000\u0006ppsq\u0000~\u0000\tq\u0000"
+"~\u0000\rpsq\u0000~\u0000\u000fq\u0000~\u0000\rp\u0000sq\u0000~\u0000\u0015ppq\u0000~\u0000\u0018sr\u0000\'com.sun.msv.grammar.Differ"
+"enceNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003nc1q\u0000~\u0000\u0010L\u0000\u0003nc2q\u0000~\u0000\u0010xq\u0000~\u0000\u001bq\u0000~\u0000\u001csr\u0000"
+"#com.sun.msv.grammar.ChoiceNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003nc1q\u0000~\u0000\u0010L\u0000"
+"\u0003nc2q\u0000~\u0000\u0010xq\u0000~\u0000\u001bsr\u0000&com.sun.msv.grammar.NamespaceNameClass\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\fnamespaceURIq\u0000~\u0000 xq\u0000~\u0000\u001bt\u0000\u0000sq\u0000~\u0000rq\u0000~\u0000[sq\u0000~\u0000rq\u0000~\u0000#q"
+"\u0000~\u0000\u001eq\u0000~\u0000\u001esr\u0000\"com.sun.msv.grammar.ExpressionPool\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000"
+"\bexpTablet\u0000/Lcom/sun/msv/grammar/ExpressionPool$ClosedHash;x"
+"psr\u0000-com.sun.msv.grammar.ExpressionPool$ClosedHash\u00d7j\u00d0N\u00ef\u00e8\u00ed\u001c\u0003\u0000"
+"\u0003I\u0000\u0005countB\u0000\rstreamVersionL\u0000\u0006parentt\u0000$Lcom/sun/msv/grammar/Ex"
+"pressionPool;xp\u0000\u0000\u0000\u001b\u0001pq\u0000~\u0000\u0014q\u0000~\u0000(q\u0000~\u0000Kq\u0000~\u0000Sq\u0000~\u0000bq\u0000~\u0000\bq\u0000~\u0000\u0013q\u0000~\u0000"
+"\'q\u0000~\u0000Jq\u0000~\u0000Rq\u0000~\u0000aq\u0000~\u0000%q\u0000~\u0000Pq\u0000~\u0000_q\u0000~\u0000kq\u0000~\u0000\\q\u0000~\u0000jq\u0000~\u0000,q\u0000~\u0000Wq\u0000~\u0000"
+"fq\u0000~\u0000\u000eq\u0000~\u0000Hq\u0000~\u0000]q\u0000~\u0000\u0007q\u0000~\u0000\u000bq\u0000~\u0000Gq\u0000~\u0000\u0005x"));
        }
        return new com.sun.msv.verifier.regexp.REDocumentDeclaration(schemaFragment);
    }

    public class Unmarshaller
        extends ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.AbstractUnmarshallingEventHandlerImpl
    {


        public Unmarshaller(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallingContext context) {
            super(context, "----------");
        }

        protected Unmarshaller(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallingContext context, int startState) {
            this(context);
            state = startState;
        }

        public java.lang.Object owner() {
            return ru.runa.xpdl.generated.bpmnxpdl.impl.FormalParametersTypeImpl.this;
        }

        public void enterElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname, org.xml.sax.Attributes __atts)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        if (("TC1025FormalParameter" == ___local)&&("http://www.wfmc.org/2002/XPDL1.0" == ___uri)) {
                            _getTC1025FormalParameter().add(((ru.runa.xpdl.generated.bpmnxpdl.impl.TC1025FormalParameterImpl) spawnChildFromEnterElement((ru.runa.xpdl.generated.bpmnxpdl.impl.TC1025FormalParameterImpl.class), 3, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("TC1025FormalParameter" == ___local)&&("http://www.wfmc.org/2002/XPDL1.0" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 1;
                            return ;
                        }
                        if (("FormalParameter" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            _getFormalParameter().add(((ru.runa.xpdl.generated.bpmnxpdl.impl.FormalParameterImpl) spawnChildFromEnterElement((ru.runa.xpdl.generated.bpmnxpdl.impl.FormalParameterImpl.class), 3, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("FormalParameter" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 8;
                            return ;
                        }
                        if (("Extensions" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            context.pushAttributes(__atts, true);
                            state = 4;
                            return ;
                        }
                        state = 7;
                        continue outer;
                    case  0 :
                        if (("TC1025FormalParameter" == ___local)&&("http://www.wfmc.org/2002/XPDL1.0" == ___uri)) {
                            _getTC1025FormalParameter().add(((ru.runa.xpdl.generated.bpmnxpdl.impl.TC1025FormalParameterImpl) spawnChildFromEnterElement((ru.runa.xpdl.generated.bpmnxpdl.impl.TC1025FormalParameterImpl.class), 3, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("TC1025FormalParameter" == ___local)&&("http://www.wfmc.org/2002/XPDL1.0" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 1;
                            return ;
                        }
                        if (("FormalParameter" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            _getFormalParameter().add(((ru.runa.xpdl.generated.bpmnxpdl.impl.FormalParameterImpl) spawnChildFromEnterElement((ru.runa.xpdl.generated.bpmnxpdl.impl.FormalParameterImpl.class), 3, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("FormalParameter" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 8;
                            return ;
                        }
                        state = 3;
                        continue outer;
                    case  8 :
                        attIdx = context.getAttribute("", "Id");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().enterElement(___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        break;
                    case  4 :
                        if (true) {
                            _Extensions = ((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl) spawnChildFromEnterElement((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl.class), 5, ___uri, ___local, ___qname, __atts));
                            return ;
                        }
                        _Extensions = ((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl) spawnChildFromEnterElement((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl.class), 5, ___uri, ___local, ___qname, __atts));
                        return ;
                    case  7 :
                        if (!(("" == ___uri)||("http://www.wfmc.org/2008/XPDL2.1" == ___uri))) {
                            java.lang.Object co = spawnWildcard(7, ___uri, ___local, ___qname, __atts);
                            if (co!= null) {
                                _getAny().add(co);
                            }
                            return ;
                        }
                        revertToParentFromEnterElement(___uri, ___local, ___qname, __atts);
                        return ;
                    case  6 :
                        if (!(("" == ___uri)||("http://www.wfmc.org/2008/XPDL2.1" == ___uri))) {
                            java.lang.Object co = spawnWildcard(7, ___uri, ___local, ___qname, __atts);
                            if (co!= null) {
                                _getAny().add(co);
                            }
                            return ;
                        }
                        state = 7;
                        continue outer;
                    case  1 :
                        attIdx = context.getAttribute("", "Id");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().enterElement(___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        break;
                }
                super.enterElement(___uri, ___local, ___qname, __atts);
                break;
            }
        }

        public void leaveElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  2 :
                        if (("TC1025FormalParameter" == ___local)&&("http://www.wfmc.org/2002/XPDL1.0" == ___uri)) {
                            context.popAttributes();
                            state = 3;
                            return ;
                        }
                        break;
                    case  3 :
                        state = 7;
                        continue outer;
                    case  0 :
                        state = 3;
                        continue outer;
                    case  8 :
                        attIdx = context.getAttribute("", "Id");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveElement(___uri, ___local, ___qname);
                            return ;
                        }
                        break;
                    case  4 :
                        _Extensions = ((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl) spawnChildFromLeaveElement((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl.class), 5, ___uri, ___local, ___qname));
                        return ;
                    case  9 :
                        if (("FormalParameter" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            context.popAttributes();
                            state = 3;
                            return ;
                        }
                        break;
                    case  5 :
                        if (("Extensions" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            context.popAttributes();
                            state = 6;
                            return ;
                        }
                        break;
                    case  7 :
                        revertToParentFromLeaveElement(___uri, ___local, ___qname);
                        return ;
                    case  6 :
                        state = 7;
                        continue outer;
                    case  1 :
                        attIdx = context.getAttribute("", "Id");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveElement(___uri, ___local, ___qname);
                            return ;
                        }
                        break;
                }
                super.leaveElement(___uri, ___local, ___qname);
                break;
            }
        }

        public void enterAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        state = 7;
                        continue outer;
                    case  0 :
                        state = 3;
                        continue outer;
                    case  8 :
                        if (("Id" == ___local)&&("" == ___uri)) {
                            _getFormalParameter().add(((ru.runa.xpdl.generated.bpmnxpdl.impl.FormalParameterTypeImpl) spawnChildFromEnterAttribute((ru.runa.xpdl.generated.bpmnxpdl.impl.FormalParameterTypeImpl.class), 9, ___uri, ___local, ___qname)));
                            return ;
                        }
                        break;
                    case  4 :
                        _Extensions = ((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl) spawnChildFromEnterAttribute((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl.class), 5, ___uri, ___local, ___qname));
                        return ;
                    case  7 :
                        revertToParentFromEnterAttribute(___uri, ___local, ___qname);
                        return ;
                    case  6 :
                        state = 7;
                        continue outer;
                    case  1 :
                        if (("Id" == ___local)&&("" == ___uri)) {
                            _getTC1025FormalParameter().add(((ru.runa.xpdl.generated.bpmnxpdl.impl.TC1025FormalParameterTypeImpl) spawnChildFromEnterAttribute((ru.runa.xpdl.generated.bpmnxpdl.impl.TC1025FormalParameterTypeImpl.class), 2, ___uri, ___local, ___qname)));
                            return ;
                        }
                        break;
                }
                super.enterAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void leaveAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        state = 7;
                        continue outer;
                    case  0 :
                        state = 3;
                        continue outer;
                    case  8 :
                        attIdx = context.getAttribute("", "Id");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveAttribute(___uri, ___local, ___qname);
                            return ;
                        }
                        break;
                    case  4 :
                        _Extensions = ((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl) spawnChildFromLeaveAttribute((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl.class), 5, ___uri, ___local, ___qname));
                        return ;
                    case  7 :
                        revertToParentFromLeaveAttribute(___uri, ___local, ___qname);
                        return ;
                    case  6 :
                        state = 7;
                        continue outer;
                    case  1 :
                        attIdx = context.getAttribute("", "Id");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveAttribute(___uri, ___local, ___qname);
                            return ;
                        }
                        break;
                }
                super.leaveAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void handleText(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                try {
                    switch (state) {
                        case  3 :
                            state = 7;
                            continue outer;
                        case  0 :
                            state = 3;
                            continue outer;
                        case  8 :
                            attIdx = context.getAttribute("", "Id");
                            if (attIdx >= 0) {
                                context.consumeAttribute(attIdx);
                                context.getCurrentHandler().text(value);
                                return ;
                            }
                            break;
                        case  4 :
                            _Extensions = ((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl) spawnChildFromText((ru.runa.xpdl.generated.bpmnxpdl.impl.AnyTypeImpl.class), 5, value));
                            return ;
                        case  7 :
                            revertToParentFromText(value);
                            return ;
                        case  6 :
                            state = 7;
                            continue outer;
                        case  1 :
                            attIdx = context.getAttribute("", "Id");
                            if (attIdx >= 0) {
                                context.consumeAttribute(attIdx);
                                context.getCurrentHandler().text(value);
                                return ;
                            }
                            break;
                    }
                } catch (java.lang.RuntimeException e) {
                    handleUnexpectedTextException(value, e);
                }
                break;
            }
        }

    }

}
