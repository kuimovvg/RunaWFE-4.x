//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.04 at 12:24:30 PM MSD 
//


package ru.runa.xpdl.generated.jpdl32.impl;

public class SuperStateImpl
    extends ru.runa.xpdl.generated.jpdl32.impl.SuperStateTypeImpl
    implements ru.runa.xpdl.generated.jpdl32.SuperState, com.sun.xml.bind.RIElement, com.sun.xml.bind.JAXBObject, ru.runa.xpdl.generated.jpdl32.impl.runtime.UnmarshallableObject, ru.runa.xpdl.generated.jpdl32.impl.runtime.XMLSerializable, ru.runa.xpdl.generated.jpdl32.impl.runtime.ValidatableObject
{

    public final static java.lang.Class version = (ru.runa.xpdl.generated.jpdl32.impl.JAXBVersion.class);
    private static com.sun.msv.grammar.Grammar schemaFragment;

    private final static java.lang.Class PRIMARY_INTERFACE_CLASS() {
        return (ru.runa.xpdl.generated.jpdl32.SuperState.class);
    }

    public java.lang.String ____jaxb_ri____getNamespaceURI() {
        return "urn:jbpm.org:jpdl-3.2";
    }

    public java.lang.String ____jaxb_ri____getLocalName() {
        return "super-state";
    }

    public ru.runa.xpdl.generated.jpdl32.impl.runtime.UnmarshallingEventHandler createUnmarshaller(ru.runa.xpdl.generated.jpdl32.impl.runtime.UnmarshallingContext context) {
        return new ru.runa.xpdl.generated.jpdl32.impl.SuperStateImpl.Unmarshaller(context);
    }

    public void serializeBody(ru.runa.xpdl.generated.jpdl32.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        context.startElement("urn:jbpm.org:jpdl-3.2", "super-state");
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
        return (ru.runa.xpdl.generated.jpdl32.SuperState.class);
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
+"r\u0000\u001dcom.sun.msv.grammar.ChoiceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\bppsr\u0000 com.s"
+"un.msv.grammar.OneOrMoreExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001ccom.sun.msv.gramma"
+"r.UnaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\u0003expq\u0000~\u0000\u0003xq\u0000~\u0000\u0004sr\u0000\u0011java.lang.Boolean\u00cd"
+" r\u0080\u00d5\u009c\u00fa\u00ee\u0002\u0000\u0001Z\u0000\u0005valuexp\u0000psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq"
+"\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq"
+"\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq"
+"\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\fq\u0000~\u0000\u0012psq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000s"
+"q\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psr\u0000 com.sun.msv.grammar.AttributeExp\u0000\u0000\u0000\u0000\u0000"
+"\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003expq\u0000~\u0000\u0003L\u0000\tnameClassq\u0000~\u0000\u0001xq\u0000~\u0000\u0004q\u0000~\u0000\u0012psr\u00002com.sun.ms"
+"v.grammar.Expression$AnyStringExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0004sq\u0000"
+"~\u0000\u0011\u0001psr\u0000 com.sun.msv.grammar.AnyNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom."
+"sun.msv.grammar.NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpsr\u00000com.sun.msv.gramma"
+"r.Expression$EpsilonExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0004q\u0000~\u0000+psr\u0000#com"
+".sun.msv.grammar.SimpleNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\tlocalNamet\u0000\u0012Lj"
+"ava/lang/String;L\u0000\fnamespaceURIq\u0000~\u00002xq\u0000~\u0000-t\u0000\u0015generated.jpdl3"
+"2.Nodet\u0000+http://java.sun.com/jaxb/xjc/dummy-elementssq\u0000~\u0000\u0000q\u0000"
+"~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000"
+"\u0016generated.jpdl32.Stateq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012"
+"psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000\u0019generated.jpdl32.TaskNo"
+"deq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~"
+"\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000\u001bgenerated.jpdl32.SuperStateq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p"
+"\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000\u001dgen"
+"erated.jpdl32.ProcessStateq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000"
+"~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000#ru.runa.xpdl.generated.jpdl32.Mul"
+"tiinstanceStateq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q"
+"\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000\u001cgenerated.jpdl32.SendMessageq\u0000~"
+"\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~"
+"\u00000sq\u0000~\u00001t\u0000\u001fgenerated.jpdl32.ReceiveMessageq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000"
+"sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000\u0015gene"
+"rated.jpdl32.Forkq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000"
+"\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000\u0015generated.jpdl32.Joinq\u0000~\u00005sq\u0000"
+"~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000"
+"~\u00001t\u0000\u0019generated.jpdl32.Decisionq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000"
+"~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000\u0019generated.jpdl3"
+"2.EndStateq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012p"
+"q\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000\u0019generated.jpdl32.MailNodeq\u0000~\u00005sq\u0000~\u0000\u0000"
+"q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001"
+"t\u0000\u001cgenerated.jpdl32.Descriptionq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000"
+"~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000\u0016generated.jpdl3"
+"2.Eventq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~"
+"\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000!ru.runa.xpdl.generated.jpdl32.ExceptionHandlerq\u0000~\u00005s"
+"q\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000s"
+"q\u0000~\u00001t\u0000\u0016generated.jpdl32.Timerq\u0000~\u00005sq\u0000~\u0000\u0000q\u0000~\u0000\u0012p\u0000sq\u0000~\u0000\fppsq\u0000~"
+"\u0000\u000eq\u0000~\u0000\u0012psq\u0000~\u0000\'q\u0000~\u0000\u0012pq\u0000~\u0000*q\u0000~\u0000.q\u0000~\u00000sq\u0000~\u00001t\u0000\u001bgenerated.jpdl32"
+".Transitionq\u0000~\u00005q\u0000~\u00000sq\u0000~\u0000\fppsq\u0000~\u0000\'q\u0000~\u0000\u0012psr\u0000\u001bcom.sun.msv.gra"
+"mmar.DataExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\u0002dtt\u0000\u001fLorg/relaxng/datatype/Datatyp"
+"e;L\u0000\u0006exceptq\u0000~\u0000\u0003L\u0000\u0004namet\u0000\u001dLcom/sun/msv/util/StringPair;xq\u0000~\u0000"
+"\u0004q\u0000~\u0000\u0012psr\u0000#com.sun.msv.datatype.xsd.StringType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001Z\u0000\r"
+"isAlwaysValidxr\u0000*com.sun.msv.datatype.xsd.BuiltinAtomicType\u0000"
+"\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000%com.sun.msv.datatype.xsd.ConcreteType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002"
+"\u0000\u0000xr\u0000\'com.sun.msv.datatype.xsd.XSDatatypeImpl\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\fn"
+"amespaceUriq\u0000~\u00002L\u0000\btypeNameq\u0000~\u00002L\u0000\nwhiteSpacet\u0000.Lcom/sun/msv"
+"/datatype/xsd/WhiteSpaceProcessor;xpt\u0000 http://www.w3.org/200"
+"1/XMLSchemat\u0000\u0006stringsr\u00005com.sun.msv.datatype.xsd.WhiteSpaceP"
+"rocessor$Preserve\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000,com.sun.msv.datatype.xsd.Whi"
+"teSpaceProcessor\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xp\u0001sr\u00000com.sun.msv.grammar.Expres"
+"sion$NullSetExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0004q\u0000~\u0000\u0012psr\u0000\u001bcom.sun.msv"
+".util.StringPair\u00d0t\u001ejB\u008f\u008d\u00a0\u0002\u0000\u0002L\u0000\tlocalNameq\u0000~\u00002L\u0000\fnamespaceURIq"
+"\u0000~\u00002xpq\u0000~\u0000\u00a9q\u0000~\u0000\u00a8sq\u0000~\u00001t\u0000\u0005asynct\u0000\u0000q\u0000~\u00000sq\u0000~\u0000\'ppq\u0000~\u0000\u00a1sq\u0000~\u00001t\u0000\u0004"
+"nameq\u0000~\u0000\u00b3sq\u0000~\u0000\fppsq\u0000~\u0000\'q\u0000~\u0000\u0012psq\u0000~\u0000\u009eppsr\u0000\"com.sun.msv.datatyp"
+"e.xsd.QnameType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u00a3q\u0000~\u0000\u00a8t\u0000\u0005QNamesr\u00005com.sun.msv"
+".datatype.xsd.WhiteSpaceProcessor$Collapse\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u00abq"
+"\u0000~\u0000\u00aesq\u0000~\u0000\u00afq\u0000~\u0000\u00bcq\u0000~\u0000\u00a8sq\u0000~\u00001t\u0000\u0004typet\u0000)http://www.w3.org/2001/X"
+"MLSchema-instanceq\u0000~\u00000sq\u0000~\u00001t\u0000\u000bsuper-statet\u0000\u0015urn:jbpm.org:jp"
+"dl-3.2sr\u0000\"com.sun.msv.grammar.ExpressionPool\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\bex"
+"pTablet\u0000/Lcom/sun/msv/grammar/ExpressionPool$ClosedHash;xpsr"
+"\u0000-com.sun.msv.grammar.ExpressionPool$ClosedHash\u00d7j\u00d0N\u00ef\u00e8\u00ed\u001c\u0003\u0000\u0003I\u0000"
+"\u0005countB\u0000\rstreamVersionL\u0000\u0006parentt\u0000$Lcom/sun/msv/grammar/Expre"
+"ssionPool;xp\u0000\u0000\u0000<\u0001pq\u0000~\u0000#q\u0000~\u0000\u000bq\u0000~\u0000\nq\u0000~\u0000\u0014q\u0000~\u0000\u009cq\u0000~\u0000\u001eq\u0000~\u0000\u0013q\u0000~\u0000\u001aq\u0000"
+"~\u0000\u0016q\u0000~\u0000\u001dq\u0000~\u0000 q\u0000~\u0000\u0019q\u0000~\u0000\u0015q\u0000~\u0000\tq\u0000~\u0000\u001fq\u0000~\u0000\u0010q\u0000~\u0000\"q\u0000~\u0000\u00b7q\u0000~\u0000\u0098q\u0000~\u0000\u0092q\u0000"
+"~\u0000\u008cq\u0000~\u0000\u0086q\u0000~\u0000\u0080q\u0000~\u0000zq\u0000~\u0000\u0097q\u0000~\u0000\u0091q\u0000~\u0000\u008bq\u0000~\u0000\u0085q\u0000~\u0000\u007fq\u0000~\u0000yq\u0000~\u0000sq\u0000~\u0000mq\u0000"
+"~\u0000gq\u0000~\u0000\u001cq\u0000~\u0000aq\u0000~\u0000[q\u0000~\u0000Uq\u0000~\u0000Oq\u0000~\u0000Iq\u0000~\u0000Cq\u0000~\u0000=q\u0000~\u0000!q\u0000~\u00007q\u0000~\u0000%q\u0000"
+"~\u0000tq\u0000~\u0000nq\u0000~\u0000hq\u0000~\u0000bq\u0000~\u0000\u0018q\u0000~\u0000\\q\u0000~\u0000Vq\u0000~\u0000Pq\u0000~\u0000Jq\u0000~\u0000Dq\u0000~\u0000>q\u0000~\u00008q\u0000"
+"~\u0000&q\u0000~\u0000\u001bq\u0000~\u0000\rq\u0000~\u0000\u0017x"));
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
            return ru.runa.xpdl.generated.jpdl32.impl.SuperStateImpl.this;
        }

        public void enterElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname, org.xml.sax.Attributes __atts)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
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
                    case  3 :
                        revertToParentFromEnterElement(___uri, ___local, ___qname, __atts);
                        return ;
                    case  0 :
                        if (("super-state" == ___local)&&("urn:jbpm.org:jpdl-3.2" == ___uri)) {
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
                    case  2 :
                        if (("super-state" == ___local)&&("urn:jbpm.org:jpdl-3.2" == ___uri)) {
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
                    case  3 :
                        revertToParentFromLeaveElement(___uri, ___local, ___qname);
                        return ;
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
                    case  1 :
                        if (("async" == ___local)&&("" == ___uri)) {
                            spawnHandlerFromEnterAttribute((((ru.runa.xpdl.generated.jpdl32.impl.SuperStateTypeImpl) ru.runa.xpdl.generated.jpdl32.impl.SuperStateImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname);
                            return ;
                        }
                        if (("name" == ___local)&&("" == ___uri)) {
                            spawnHandlerFromEnterAttribute((((ru.runa.xpdl.generated.jpdl32.impl.SuperStateTypeImpl) ru.runa.xpdl.generated.jpdl32.impl.SuperStateImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname);
                            return ;
                        }
                        break;
                    case  3 :
                        revertToParentFromEnterAttribute(___uri, ___local, ___qname);
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
                    case  3 :
                        revertToParentFromLeaveAttribute(___uri, ___local, ___qname);
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
                        case  3 :
                            revertToParentFromText(value);
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
