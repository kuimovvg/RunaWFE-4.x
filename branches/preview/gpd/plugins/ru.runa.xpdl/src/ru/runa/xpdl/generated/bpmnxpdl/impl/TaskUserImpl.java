//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl.impl;

public class TaskUserImpl
    extends ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl
    implements ru.runa.xpdl.generated.bpmnxpdl.TaskUser, com.sun.xml.bind.RIElement, com.sun.xml.bind.JAXBObject, ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallableObject, ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.XMLSerializable, ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.ValidatableObject
{

    public final static java.lang.Class version = (ru.runa.xpdl.generated.bpmnxpdl.impl.JAXBVersion.class);
    private static com.sun.msv.grammar.Grammar schemaFragment;

    private final static java.lang.Class PRIMARY_INTERFACE_CLASS() {
        return (ru.runa.xpdl.generated.bpmnxpdl.TaskUser.class);
    }

    public java.lang.String ____jaxb_ri____getNamespaceURI() {
        return "http://www.wfmc.org/2008/XPDL2.1";
    }

    public java.lang.String ____jaxb_ri____getLocalName() {
        return "TaskUser";
    }

    public ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallingEventHandler createUnmarshaller(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallingContext context) {
        return new ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.Unmarshaller(context);
    }

    public void serializeBody(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        context.startElement("http://www.wfmc.org/2008/XPDL2.1", "TaskUser");
        super.serializeURIs(context);
        context.endNamespaceDecls();
        super.serializeAttributes(context);
        context.endAttributes();
        super.serializeBody(context);
        context.endElement();
    }

    public void serializeAttributes(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
    }

    public void serializeURIs(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
    }

    public java.lang.Class getPrimaryInterface() {
        return (ru.runa.xpdl.generated.bpmnxpdl.TaskUser.class);
    }

    public com.sun.msv.verifier.DocumentDeclaration createRawValidator() {
        if (schemaFragment == null) {
            schemaFragment = com.sun.xml.bind.validator.SchemaDeserializer.deserialize((
 "\u00ac\u00ed\u0000\u0005sr\u0000\'com.sun.msv.grammar.trex.ElementPattern\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000"
+"\tnameClasst\u0000\u001fLcom/sun/msv/grammar/NameClass;xr\u0000\u001ecom.sun.msv."
+"grammar.ElementExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002Z\u0000\u001aignoreUndeclaredAttributesL\u0000"
+"\fcontentModelt\u0000 Lcom/sun/msv/grammar/Expression;xr\u0000\u001ecom.sun."
+"msv.grammar.Expression\u00f8\u0018\u0082\u00e8N5~O\u0002\u0000\u0002L\u0000\u0013epsilonReducibilityt\u0000\u0013Lj"
+"ava/lang/Boolean;L\u0000\u000bexpandedExpq\u0000~\u0000\u0003xppp\u0000sr\u0000\u001fcom.sun.msv.gra"
+"mmar.SequenceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.sun.msv.grammar.BinaryExp"
+"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0004exp1q\u0000~\u0000\u0003L\u0000\u0004exp2q\u0000~\u0000\u0003xq\u0000~\u0000\u0004ppsq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0007pps"
+"q\u0000~\u0000\u0007ppsq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0007ppsr\u0000\u001dcom.sun.msv.grammar.ChoiceExp\u0000\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\bppsq\u0000~\u0000\u000fsr\u0000\u0011java.lang.Boolean\u00cd r\u0080\u00d5\u009c\u00fa\u00ee\u0002\u0000\u0001Z\u0000\u0005valu"
+"exp\u0000psq\u0000~\u0000\u0000q\u0000~\u0000\u0013p\u0000sq\u0000~\u0000\u000fppsr\u0000 com.sun.msv.grammar.OneOrMoreE"
+"xp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001ccom.sun.msv.grammar.UnaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\u0003e"
+"xpq\u0000~\u0000\u0003xq\u0000~\u0000\u0004q\u0000~\u0000\u0013psr\u0000 com.sun.msv.grammar.AttributeExp\u0000\u0000\u0000\u0000\u0000"
+"\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003expq\u0000~\u0000\u0003L\u0000\tnameClassq\u0000~\u0000\u0001xq\u0000~\u0000\u0004q\u0000~\u0000\u0013psr\u00002com.sun.ms"
+"v.grammar.Expression$AnyStringExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0004sq\u0000"
+"~\u0000\u0012\u0001q\u0000~\u0000\u001csr\u0000 com.sun.msv.grammar.AnyNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001d"
+"com.sun.msv.grammar.NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpsr\u00000com.sun.msv.gr"
+"ammar.Expression$EpsilonExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0004q\u0000~\u0000\u001dq\u0000~\u0000"
+"\"sr\u0000#com.sun.msv.grammar.SimpleNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\tlocalN"
+"amet\u0000\u0012Ljava/lang/String;L\u0000\fnamespaceURIq\u0000~\u0000$xq\u0000~\u0000\u001ft\u0000\u001dgenerat"
+"ed.bpmnxpdl.Performerst\u0000+http://java.sun.com/jaxb/xjc/dummy-"
+"elementssq\u0000~\u0000\u0000q\u0000~\u0000\u0013p\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0016q\u0000~\u0000\u0013psq"
+"\u0000~\u0000\u0019q\u0000~\u0000\u0013pq\u0000~\u0000\u001cq\u0000~\u0000 q\u0000~\u0000\"sq\u0000~\u0000#t\u0000!ru.runa.xpdl.generated.bpmnxpdl.Perform"
+"ersTypeq\u0000~\u0000\'sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0019q\u0000~\u0000\u0013psr\u0000\u001bcom.sun.msv.grammar.Data"
+"Exp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\u0002dtt\u0000\u001fLorg/relaxng/datatype/Datatype;L\u0000\u0006exce"
+"ptq\u0000~\u0000\u0003L\u0000\u0004namet\u0000\u001dLcom/sun/msv/util/StringPair;xq\u0000~\u0000\u0004ppsr\u0000\"co"
+"m.sun.msv.datatype.xsd.QnameType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000*com.sun.msv.d"
+"atatype.xsd.BuiltinAtomicType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000%com.sun.msv.data"
+"type.xsd.ConcreteType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\'com.sun.msv.datatype.xsd"
+".XSDatatypeImpl\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\fnamespaceUriq\u0000~\u0000$L\u0000\btypeNameq\u0000~"
+"\u0000$L\u0000\nwhiteSpacet\u0000.Lcom/sun/msv/datatype/xsd/WhiteSpaceProces"
+"sor;xpt\u0000 http://www.w3.org/2001/XMLSchemat\u0000\u0005QNamesr\u00005com.sun"
+".msv.datatype.xsd.WhiteSpaceProcessor$Collapse\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000"
+",com.sun.msv.datatype.xsd.WhiteSpaceProcessor\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpsr"
+"\u00000com.sun.msv.grammar.Expression$NullSetExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000"
+"\u0000xq\u0000~\u0000\u0004q\u0000~\u0000\u0013psr\u0000\u001bcom.sun.msv.util.StringPair\u00d0t\u001ejB\u008f\u008d\u00a0\u0002\u0000\u0002L\u0000\tlo"
+"calNameq\u0000~\u0000$L\u0000\fnamespaceURIq\u0000~\u0000$xpq\u0000~\u0000=q\u0000~\u0000<sq\u0000~\u0000#t\u0000\u0004typet\u0000)"
+"http://www.w3.org/2001/XMLSchema-instanceq\u0000~\u0000\"sq\u0000~\u0000#t\u0000\nPerfo"
+"rmerst\u0000 http://www.wfmc.org/2008/XPDL2.1q\u0000~\u0000\"sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0000q"
+"\u0000~\u0000\u0013p\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0016q\u0000~\u0000\u0013psq\u0000~\u0000\u0019q\u0000~\u0000\u0013pq\u0000~\u0000\u001c"
+"q\u0000~\u0000 q\u0000~\u0000\"sq\u0000~\u0000#t\u0000\u001egenerated.bpmnxpdl.MessageTypeq\u0000~\u0000\'sq\u0000~\u0000\u000f"
+"ppsq\u0000~\u0000\u0019q\u0000~\u0000\u0013pq\u0000~\u00005q\u0000~\u0000Eq\u0000~\u0000\"sq\u0000~\u0000#t\u0000\tMessageInq\u0000~\u0000Jq\u0000~\u0000\"sq\u0000"
+"~\u0000\u000fppsq\u0000~\u0000\u0000q\u0000~\u0000\u0013p\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0016q\u0000~\u0000\u0013psq\u0000~\u0000"
+"\u0019q\u0000~\u0000\u0013pq\u0000~\u0000\u001cq\u0000~\u0000 q\u0000~\u0000\"sq\u0000~\u0000#q\u0000~\u0000Sq\u0000~\u0000\'sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0019q\u0000~\u0000\u0013pq\u0000"
+"~\u00005q\u0000~\u0000Eq\u0000~\u0000\"sq\u0000~\u0000#t\u0000\nMessageOutq\u0000~\u0000Jq\u0000~\u0000\"sq\u0000~\u0000\u000fppsq\u0000~\u0000\u000fq\u0000~\u0000"
+"\u0013psq\u0000~\u0000\u0000q\u0000~\u0000\u0013p\u0000sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0016q\u0000~\u0000\u0013psq\u0000~\u0000\u0019q\u0000~\u0000\u0013pq\u0000~\u0000\u001cq\u0000~\u0000 q\u0000~"
+"\u0000\"sq\u0000~\u0000#t\u0000&ru.runa.xpdl.generated.bpmnxpdl.WebServiceOperationq\u0000~\u0000\'sq\u0000~\u0000\u0000"
+"q\u0000~\u0000\u0013p\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0016q\u0000~\u0000\u0013psq\u0000~\u0000\u0019q\u0000~\u0000\u0013pq\u0000~\u0000"
+"\u001cq\u0000~\u0000 q\u0000~\u0000\"sq\u0000~\u0000#t\u0000*ru.runa.xpdl.generated.bpmnxpdl.WebServiceOperationTy"
+"peq\u0000~\u0000\'sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0019q\u0000~\u0000\u0013pq\u0000~\u00005q\u0000~\u0000Eq\u0000~\u0000\"sq\u0000~\u0000#t\u0000\u0013WebServic"
+"eOperationq\u0000~\u0000Jq\u0000~\u0000\"sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0016q\u0000~\u0000\u0013psq\u0000~\u0000\u0000q\u0000~\u0000\u0013p\u0000sq\u0000~\u0000\u0019p"
+"pq\u0000~\u0000\u001csr\u0000\'com.sun.msv.grammar.DifferenceNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002"
+"L\u0000\u0003nc1q\u0000~\u0000\u0001L\u0000\u0003nc2q\u0000~\u0000\u0001xq\u0000~\u0000\u001fq\u0000~\u0000 sr\u0000#com.sun.msv.grammar.Cho"
+"iceNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003nc1q\u0000~\u0000\u0001L\u0000\u0003nc2q\u0000~\u0000\u0001xq\u0000~\u0000\u001fsr\u0000&com.s"
+"un.msv.grammar.NamespaceNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\fnamespaceURIq"
+"\u0000~\u0000$xq\u0000~\u0000\u001ft\u0000\u0000sq\u0000~\u0000\u0080q\u0000~\u0000Jsq\u0000~\u0000\u0080q\u0000~\u0000\'q\u0000~\u0000\"sq\u0000~\u0000\u000fppsq\u0000~\u0000\u0019q\u0000~\u0000\u0013p"
+"sq\u0000~\u00002ppsr\u0000)com.sun.msv.datatype.xsd.EnumerationFacet\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
+"\u0001\u0002\u0000\u0001L\u0000\u0006valuest\u0000\u000fLjava/util/Set;xr\u00009com.sun.msv.datatype.xsd."
+"DataTypeWithValueConstraintFacet\"\u00a7Ro\u00ca\u00c7\u008aT\u0002\u0000\u0000xr\u0000*com.sun.msv.d"
+"atatype.xsd.DataTypeWithFacet\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0005Z\u0000\fisFacetFixedZ\u0000\u0012ne"
+"edValueCheckFlagL\u0000\bbaseTypet\u0000)Lcom/sun/msv/datatype/xsd/XSDa"
+"tatypeImpl;L\u0000\fconcreteTypet\u0000\'Lcom/sun/msv/datatype/xsd/Concr"
+"eteType;L\u0000\tfacetNameq\u0000~\u0000$xq\u0000~\u00009q\u0000~\u0000Jpq\u0000~\u0000@\u0000\u0000sr\u0000$com.sun.msv."
+"datatype.xsd.NmtokenType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\"com.sun.msv.datatype."
+"xsd.TokenType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000#com.sun.msv.datatype.xsd.StringT"
+"ype\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001Z\u0000\risAlwaysValidxq\u0000~\u00007q\u0000~\u0000<t\u0000\u0007NMTOKENq\u0000~\u0000@\u0000q\u0000~"
+"\u0000\u0092t\u0000\u000benumerationsr\u0000\u0011java.util.HashSet\u00baD\u0085\u0095\u0096\u00b8\u00b74\u0003\u0000\u0000xpw\f\u0000\u0000\u0000\u0010?@\u0000\u0000"
+"\u0000\u0000\u0000\u0003t\u0000\u000bUnspecifiedt\u0000\u0005Othert\u0000\nWebServicexq\u0000~\u0000Bsq\u0000~\u0000Ct\u0000\u000fNMTOKE"
+"N-derivedq\u0000~\u0000Jsq\u0000~\u0000#t\u0000\u000eImplementationq\u0000~\u0000\u0082q\u0000~\u0000\"sq\u0000~\u0000\u000fppsq\u0000~\u0000"
+"\u0019q\u0000~\u0000\u0013pq\u0000~\u00005q\u0000~\u0000Eq\u0000~\u0000\"sq\u0000~\u0000#t\u0000\bTaskUserq\u0000~\u0000Jsr\u0000\"com.sun.msv."
+"grammar.ExpressionPool\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\bexpTablet\u0000/Lcom/sun/msv/"
+"grammar/ExpressionPool$ClosedHash;xpsr\u0000-com.sun.msv.grammar."
+"ExpressionPool$ClosedHash\u00d7j\u00d0N\u00ef\u00e8\u00ed\u001c\u0003\u0000\u0003I\u0000\u0005countB\u0000\rstreamVersion"
+"L\u0000\u0006parentt\u0000$Lcom/sun/msv/grammar/ExpressionPool;xp\u0000\u0000\u0000$\u0001pq\u0000~\u0000"
+"\u0018q\u0000~\u0000,q\u0000~\u0000Pq\u0000~\u0000]q\u0000~\u0000hq\u0000~\u0000pq\u0000~\u0000\u0085q\u0000~\u0000\u000eq\u0000~\u0000\u0015q\u0000~\u0000+q\u0000~\u0000Oq\u0000~\u0000\\q\u0000~\u0000"
+"gq\u0000~\u0000Kq\u0000~\u0000Xq\u0000~\u0000oq\u0000~\u0000\fq\u0000~\u0000)q\u0000~\u0000Mq\u0000~\u0000Zq\u0000~\u0000mq\u0000~\u0000xq\u0000~\u0000\nq\u0000~\u0000\u000bq\u0000~\u0000"
+"\tq\u0000~\u00000q\u0000~\u0000Tq\u0000~\u0000`q\u0000~\u0000tq\u0000~\u0000\u009eq\u0000~\u0000\rq\u0000~\u0000\u0010q\u0000~\u0000\u0011q\u0000~\u0000eq\u0000~\u0000dq\u0000~\u0000yx"));
        }
        return new com.sun.msv.verifier.regexp.REDocumentDeclaration(schemaFragment);
    }

    public class Unmarshaller
        extends ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.AbstractUnmarshallingEventHandlerImpl
    {


        public Unmarshaller(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallingContext context) {
            super(context, "----");
        }

        protected Unmarshaller(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallingContext context, int startState) {
            this(context);
            state = startState;
        }

        public java.lang.Object owner() {
            return ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this;
        }

        public void enterElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname, org.xml.sax.Attributes __atts)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        revertToParentFromEnterElement(___uri, ___local, ___qname, __atts);
                        return ;
                    case  0 :
                        if (("TaskUser" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 1;
                            return ;
                        }
                        break;
                    case  1 :
                        attIdx = context.getAttribute("", "Implementation");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().enterElement(___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("Performers" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("Performers" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("MessageIn" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("MessageOut" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("WebServiceOperation" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("WebServiceOperation" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (!(("" == ___uri)||("http://www.wfmc.org/2008/XPDL2.1" == ___uri))) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                        return ;
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
                    case  3 :
                        revertToParentFromLeaveElement(___uri, ___local, ___qname);
                        return ;
                    case  1 :
                        attIdx = context.getAttribute("", "Implementation");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveElement(___uri, ___local, ___qname);
                            return ;
                        }
                        spawnHandlerFromLeaveElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname);
                        return ;
                    case  2 :
                        if (("TaskUser" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            context.popAttributes();
                            state = 3;
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
                        revertToParentFromEnterAttribute(___uri, ___local, ___qname);
                        return ;
                    case  1 :
                        if (("Implementation" == ___local)&&("" == ___uri)) {
                            spawnHandlerFromEnterAttribute((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname);
                            return ;
                        }
                        spawnHandlerFromEnterAttribute((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname);
                        return ;
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
                        revertToParentFromLeaveAttribute(___uri, ___local, ___qname);
                        return ;
                    case  1 :
                        attIdx = context.getAttribute("", "Implementation");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveAttribute(___uri, ___local, ___qname);
                            return ;
                        }
                        spawnHandlerFromLeaveAttribute((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname);
                        return ;
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
                            revertToParentFromText(value);
                            return ;
                        case  1 :
                            attIdx = context.getAttribute("", "Implementation");
                            if (attIdx >= 0) {
                                context.consumeAttribute(attIdx);
                                context.getCurrentHandler().text(value);
                                return ;
                            }
                            spawnHandlerFromText((((ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.TaskUserImpl.this).new Unmarshaller(context)), 2, value);
                            return ;
                    }
                } catch (java.lang.RuntimeException e) {
                    handleUnexpectedTextException(value, e);
                }
                break;
            }
        }

    }

}
