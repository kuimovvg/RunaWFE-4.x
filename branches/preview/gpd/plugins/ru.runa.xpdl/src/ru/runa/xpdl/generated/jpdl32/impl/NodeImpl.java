//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.04 at 12:24:30 PM MSD 
//


package ru.runa.xpdl.generated.jpdl32.impl;

public class NodeImpl
    extends ru.runa.xpdl.generated.jpdl32.impl.NodeTypeImpl
    implements ru.runa.xpdl.generated.jpdl32.Node, com.sun.xml.bind.RIElement, com.sun.xml.bind.JAXBObject, ru.runa.xpdl.generated.jpdl32.impl.runtime.UnmarshallableObject, ru.runa.xpdl.generated.jpdl32.impl.runtime.XMLSerializable, ru.runa.xpdl.generated.jpdl32.impl.runtime.ValidatableObject
{

    public final static java.lang.Class version = (ru.runa.xpdl.generated.jpdl32.impl.JAXBVersion.class);
    private static com.sun.msv.grammar.Grammar schemaFragment;

    private final static java.lang.Class PRIMARY_INTERFACE_CLASS() {
        return (ru.runa.xpdl.generated.jpdl32.Node.class);
    }

    public java.lang.String ____jaxb_ri____getNamespaceURI() {
        return "urn:jbpm.org:jpdl-3.2";
    }

    public java.lang.String ____jaxb_ri____getLocalName() {
        return "node";
    }

    public ru.runa.xpdl.generated.jpdl32.impl.runtime.UnmarshallingEventHandler createUnmarshaller(ru.runa.xpdl.generated.jpdl32.impl.runtime.UnmarshallingContext context) {
        return new ru.runa.xpdl.generated.jpdl32.impl.NodeImpl.Unmarshaller(context);
    }

    public void serializeBody(ru.runa.xpdl.generated.jpdl32.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        context.startElement("urn:jbpm.org:jpdl-3.2", "node");
        super.serializeURIs(context);
        context.endNamespaceDecls();
        super.serializeAttributes(context);
        context.endAttributes();
        super.serializeBody(context);
        context.endElement();
    }

    public void serializeAttributes(ru.runa.xpdl.generated.jpdl32.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
    }

    public void serializeURIs(ru.runa.xpdl.generated.jpdl32.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
    }

    public java.lang.Class getPrimaryInterface() {
        return (ru.runa.xpdl.generated.jpdl32.Node.class);
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
+"q\u0000~\u0000\u0007ppsr\u0000\u001dcom.sun.msv.grammar.ChoiceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\bpps"
+"q\u0000~\u0000\rsr\u0000\u0011java.lang.Boolean\u00cd r\u0080\u00d5\u009c\u00fa\u00ee\u0002\u0000\u0001Z\u0000\u0005valuexp\u0000psq\u0000~\u0000\rq\u0000~\u0000\u0011"
+"psq\u0000~\u0000\rq\u0000~\u0000\u0011psq\u0000~\u0000\rq\u0000~\u0000\u0011psq\u0000~\u0000\rq\u0000~\u0000\u0011psq\u0000~\u0000\rq\u0000~\u0000\u0011psq\u0000~\u0000\rq\u0000~\u0000\u0011"
+"psq\u0000~\u0000\rq\u0000~\u0000\u0011psq\u0000~\u0000\rq\u0000~\u0000\u0011psq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\rppsr\u0000 com.sun.ms"
+"v.grammar.OneOrMoreExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001ccom.sun.msv.grammar.Una"
+"ryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\u0003expq\u0000~\u0000\u0003xq\u0000~\u0000\u0004q\u0000~\u0000\u0011psr\u0000 com.sun.msv.gramm"
+"ar.AttributeExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003expq\u0000~\u0000\u0003L\u0000\tnameClassq\u0000~\u0000\u0001xq\u0000~\u0000\u0004"
+"q\u0000~\u0000\u0011psr\u00002com.sun.msv.grammar.Expression$AnyStringExpression"
+"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0004sq\u0000~\u0000\u0010\u0001psr\u0000 com.sun.msv.grammar.AnyNameClas"
+"s\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.sun.msv.grammar.NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpsr"
+"\u00000com.sun.msv.grammar.Expression$EpsilonExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000"
+"\u0000xq\u0000~\u0000\u0004q\u0000~\u0000#psr\u0000#com.sun.msv.grammar.SimpleNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001"
+"\u0002\u0000\u0002L\u0000\tlocalNamet\u0000\u0012Ljava/lang/String;L\u0000\fnamespaceURIq\u0000~\u0000*xq\u0000~"
+"\u0000%t\u0000\u0017generated.jpdl32.Actiont\u0000+http://java.sun.com/jaxb/xjc/"
+"dummy-elementssq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000"
+"~\u0000\u0011psq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000\u001bgenerated.jpdl32.Act"
+"ionTypeq\u0000~\u0000-sq\u0000~\u0000\rppsq\u0000~\u0000\u001fq\u0000~\u0000\u0011psr\u0000\u001bcom.sun.msv.grammar.Data"
+"Exp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\u0002dtt\u0000\u001fLorg/relaxng/datatype/Datatype;L\u0000\u0006exce"
+"ptq\u0000~\u0000\u0003L\u0000\u0004namet\u0000\u001dLcom/sun/msv/util/StringPair;xq\u0000~\u0000\u0004ppsr\u0000\"co"
+"m.sun.msv.datatype.xsd.QnameType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000*com.sun.msv.d"
+"atatype.xsd.BuiltinAtomicType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000%com.sun.msv.data"
+"type.xsd.ConcreteType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\'com.sun.msv.datatype.xsd"
+".XSDatatypeImpl\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\fnamespaceUriq\u0000~\u0000*L\u0000\btypeNameq\u0000~"
+"\u0000*L\u0000\nwhiteSpacet\u0000.Lcom/sun/msv/datatype/xsd/WhiteSpaceProces"
+"sor;xpt\u0000 http://www.w3.org/2001/XMLSchemat\u0000\u0005QNamesr\u00005com.sun"
+".msv.datatype.xsd.WhiteSpaceProcessor$Collapse\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000"
+",com.sun.msv.datatype.xsd.WhiteSpaceProcessor\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpsr"
+"\u00000com.sun.msv.grammar.Expression$NullSetExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000"
+"\u0000xq\u0000~\u0000\u0004q\u0000~\u0000\u0011psr\u0000\u001bcom.sun.msv.util.StringPair\u00d0t\u001ejB\u008f\u008d\u00a0\u0002\u0000\u0002L\u0000\tlo"
+"calNameq\u0000~\u0000*L\u0000\fnamespaceURIq\u0000~\u0000*xpq\u0000~\u0000Cq\u0000~\u0000Bsq\u0000~\u0000)t\u0000\u0004typet\u0000)"
+"http://www.w3.org/2001/XMLSchema-instanceq\u0000~\u0000(sq\u0000~\u0000)t\u0000\u0006actio"
+"nt\u0000\u0015urn:jbpm.org:jpdl-3.2sq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq"
+"\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000\u0017generated.jpdl32.Scriptq\u0000~"
+"\u0000-sq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\u001fq\u0000"
+"~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000\u001bgenerated.jpdl32.ScriptTypeq\u0000~\u0000-"
+"sq\u0000~\u0000\rppsq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000;q\u0000~\u0000Kq\u0000~\u0000(sq\u0000~\u0000)t\u0000\u0006scriptq\u0000~\u0000Psq\u0000~\u0000"
+"\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000"
+")t\u0000\u001cgenerated.jpdl32.CreateTimerq\u0000~\u0000-sq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\u0007ppsq"
+"\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)"
+"t\u0000 ru.runa.xpdl.generated.jpdl32.CreateTimerTypeq\u0000~\u0000-sq\u0000~\u0000\rppsq\u0000~\u0000\u001fq\u0000~\u0000\u0011p"
+"q\u0000~\u0000;q\u0000~\u0000Kq\u0000~\u0000(sq\u0000~\u0000)t\u0000\fcreate-timerq\u0000~\u0000Psq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\r"
+"ppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000\u001cgenerated."
+"jpdl32.CancelTimerq\u0000~\u0000-sq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\rp"
+"psq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000 ru.runa.xpdl.generated.j"
+"pdl32.CancelTimerTypeq\u0000~\u0000-sq\u0000~\u0000\rppsq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000;q\u0000~\u0000Kq\u0000~\u0000"
+"(sq\u0000~\u0000)t\u0000\fcancel-timerq\u0000~\u0000Psq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011p"
+"sq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000\u0015generated.jpdl32.Mailq\u0000~"
+"\u0000-sq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\u001fq\u0000"
+"~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000\u0019generated.jpdl32.MailTypeq\u0000~\u0000-sq"
+"\u0000~\u0000\rppsq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000;q\u0000~\u0000Kq\u0000~\u0000(sq\u0000~\u0000)t\u0000\u0004mailq\u0000~\u0000Pq\u0000~\u0000(sq\u0000~"
+"\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\rq\u0000~\u0000\u0011psq\u0000~\u0000\rq\u0000~\u0000\u0011psq\u0000~\u0000\rq\u0000~\u0000\u0011psq\u0000~\u0000\rq\u0000"
+"~\u0000\u0011psq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q"
+"\u0000~\u0000(sq\u0000~\u0000)t\u0000\u001cgenerated.jpdl32.Descriptionq\u0000~\u0000-sq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000s"
+"q\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000\u0016gener"
+"ated.jpdl32.Eventq\u0000~\u0000-sq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000"
+"\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000!ru.runa.xpdl.generated.jpdl32.ExceptionHan"
+"dlerq\u0000~\u0000-sq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q"
+"\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000\u0016generated.jpdl32.Timerq\u0000~\u0000-sq\u0000~\u0000\u0000q\u0000~\u0000\u0011p\u0000sq"
+"\u0000~\u0000\rppsq\u0000~\u0000\u001cq\u0000~\u0000\u0011psq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000\"q\u0000~\u0000&q\u0000~\u0000(sq\u0000~\u0000)t\u0000\u001bgenera"
+"ted.jpdl32.Transitionq\u0000~\u0000-q\u0000~\u0000(sq\u0000~\u0000\rppsq\u0000~\u0000\u001fq\u0000~\u0000\u0011psq\u0000~\u00008q\u0000~"
+"\u0000\u0011psr\u0000#com.sun.msv.datatype.xsd.StringType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001Z\u0000\risAl"
+"waysValidxq\u0000~\u0000=q\u0000~\u0000Bt\u0000\u0006stringsr\u00005com.sun.msv.datatype.xsd.Wh"
+"iteSpaceProcessor$Preserve\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000E\u0001q\u0000~\u0000Hsq\u0000~\u0000Iq\u0000~\u0000\u00c2"
+"q\u0000~\u0000Bsq\u0000~\u0000)t\u0000\u0005asynct\u0000\u0000q\u0000~\u0000(sq\u0000~\u0000\u001fppq\u0000~\u0000\u00bfsq\u0000~\u0000)t\u0000\u0004nameq\u0000~\u0000\u00c8sq"
+"\u0000~\u0000\rppsq\u0000~\u0000\u001fq\u0000~\u0000\u0011pq\u0000~\u0000;q\u0000~\u0000Kq\u0000~\u0000(sq\u0000~\u0000)t\u0000\u0004nodeq\u0000~\u0000Psr\u0000\"com.s"
+"un.msv.grammar.ExpressionPool\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\bexpTablet\u0000/Lcom/s"
+"un/msv/grammar/ExpressionPool$ClosedHash;xpsr\u0000-com.sun.msv.g"
+"rammar.ExpressionPool$ClosedHash\u00d7j\u00d0N\u00ef\u00e8\u00ed\u001c\u0003\u0000\u0003I\u0000\u0005countB\u0000\rstream"
+"VersionL\u0000\u0006parentt\u0000$Lcom/sun/msv/grammar/ExpressionPool;xp\u0000\u0000\u0000"
+">\u0001pq\u0000~\u0000\u009eq\u0000~\u0000\tq\u0000~\u0000\u0016q\u0000~\u0000\u0012q\u0000~\u0000\u000bq\u0000~\u0000\u0015q\u0000~\u0000\u0019q\u0000~\u0000\u009aq\u0000~\u0000\u0017q\u0000~\u0000\u009bq\u0000~\u0000\u0018q\u0000"
+"~\u0000\u0013q\u0000~\u0000\u0099q\u0000~\u0000\fq\u0000~\u0000\u0014q\u0000~\u0000\u009dq\u0000~\u0000\u0095q\u0000~\u0000\u0083q\u0000~\u0000qq\u0000~\u0000_q\u0000~\u00006q\u0000~\u0000\u00ccq\u0000~\u0000\u00bdq\u0000"
+"~\u0000\u00b9q\u0000~\u0000\u00b3q\u0000~\u0000\u00adq\u0000~\u0000\u00a7q\u0000~\u0000\u00a1q\u0000~\u0000\u0091q\u0000~\u0000\u00b8q\u0000~\u0000\u00b2q\u0000~\u0000\u00acq\u0000~\u0000\u00a6q\u0000~\u0000\u00a0q\u0000~\u0000\u0090q\u0000"
+"~\u0000\u0088q\u0000~\u0000~q\u0000~\u0000vq\u0000~\u0000lq\u0000~\u0000dq\u0000~\u0000Zq\u0000~\u0000Rq\u0000~\u00001q\u0000~\u0000\u001bq\u0000~\u0000\u0089q\u0000~\u0000\u007fq\u0000~\u0000\u009cq\u0000"
+"~\u0000wq\u0000~\u0000mq\u0000~\u0000eq\u0000~\u0000[q\u0000~\u0000Sq\u0000~\u00002q\u0000~\u0000\u001eq\u0000~\u0000\nq\u0000~\u0000\u008eq\u0000~\u0000|q\u0000~\u0000jq\u0000~\u0000Xq\u0000"
+"~\u0000/q\u0000~\u0000\u000eq\u0000~\u0000\u000fx"));
        }
        return new com.sun.msv.verifier.regexp.REDocumentDeclaration(schemaFragment);
    }

    public class Unmarshaller
        extends ru.runa.xpdl.generated.jpdl32.impl.runtime.AbstractUnmarshallingEventHandlerImpl
    {


        public Unmarshaller(ru.runa.xpdl.generated.jpdl32.impl.runtime.UnmarshallingContext context) {
            super(context, "----");
        }

        protected Unmarshaller(ru.runa.xpdl.generated.jpdl32.impl.runtime.UnmarshallingContext context, int startState) {
            this(context);
            state = startState;
        }

        public java.lang.Object owner() {
            return ru.runa.xpdl.generated.jpdl32.impl.NodeImpl.this;
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
                    case  1 :
                        attIdx = context.getAttribute("", "async");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().enterElement(___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        attIdx = context.getAttribute("", "name");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().enterElement(___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        break;
                    case  0 :
                        if (("node" == ___local)&&("urn:jbpm.org:jpdl-3.2" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 1;
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
                    case  3 :
                        revertToParentFromLeaveElement(___uri, ___local, ___qname);
                        return ;
                    case  2 :
                        if (("node" == ___local)&&("urn:jbpm.org:jpdl-3.2" == ___uri)) {
                            context.popAttributes();
                            state = 3;
                            return ;
                        }
                        break;
                    case  1 :
                        attIdx = context.getAttribute("", "async");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveElement(___uri, ___local, ___qname);
                            return ;
                        }
                        attIdx = context.getAttribute("", "name");
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
                        revertToParentFromEnterAttribute(___uri, ___local, ___qname);
                        return ;
                    case  1 :
                        if (("async" == ___local)&&("" == ___uri)) {
                            spawnHandlerFromEnterAttribute((((ru.runa.xpdl.generated.jpdl32.impl.NodeTypeImpl) ru.runa.xpdl.generated.jpdl32.impl.NodeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname);
                            return ;
                        }
                        if (("name" == ___local)&&("" == ___uri)) {
                            spawnHandlerFromEnterAttribute((((ru.runa.xpdl.generated.jpdl32.impl.NodeTypeImpl) ru.runa.xpdl.generated.jpdl32.impl.NodeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname);
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
                        revertToParentFromLeaveAttribute(___uri, ___local, ___qname);
                        return ;
                    case  1 :
                        attIdx = context.getAttribute("", "async");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveAttribute(___uri, ___local, ___qname);
                            return ;
                        }
                        attIdx = context.getAttribute("", "name");
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
                            revertToParentFromText(value);
                            return ;
                        case  1 :
                            attIdx = context.getAttribute("", "async");
                            if (attIdx >= 0) {
                                context.consumeAttribute(attIdx);
                                context.getCurrentHandler().text(value);
                                return ;
                            }
                            attIdx = context.getAttribute("", "name");
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
