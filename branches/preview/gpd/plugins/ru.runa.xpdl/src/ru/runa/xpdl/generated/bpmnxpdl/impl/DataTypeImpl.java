//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl.impl;

public class DataTypeImpl
    extends ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl
    implements ru.runa.xpdl.generated.bpmnxpdl.DataType, com.sun.xml.bind.RIElement, com.sun.xml.bind.JAXBObject, ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallableObject, ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.XMLSerializable, ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.ValidatableObject
{

    public final static java.lang.Class version = (ru.runa.xpdl.generated.bpmnxpdl.impl.JAXBVersion.class);
    private static com.sun.msv.grammar.Grammar schemaFragment;

    private final static java.lang.Class PRIMARY_INTERFACE_CLASS() {
        return (ru.runa.xpdl.generated.bpmnxpdl.DataType.class);
    }

    public java.lang.String ____jaxb_ri____getNamespaceURI() {
        return "http://www.wfmc.org/2008/XPDL2.1";
    }

    public java.lang.String ____jaxb_ri____getLocalName() {
        return "DataType";
    }

    public ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallingEventHandler createUnmarshaller(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.UnmarshallingContext context) {
        return new ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.Unmarshaller(context);
    }

    public void serializeBody(ru.runa.xpdl.generated.bpmnxpdl.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        context.startElement("http://www.wfmc.org/2008/XPDL2.1", "DataType");
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
        return (ru.runa.xpdl.generated.bpmnxpdl.DataType.class);
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
+"\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0004exp1q\u0000~\u0000\u0003L\u0000\u0004exp2q\u0000~\u0000\u0003xq\u0000~\u0000\u0004ppsr\u0000\u001dcom.sun.msv.g"
+"rammar.ChoiceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\bppsq\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000\npps"
+"q\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000"
+"\nppsq\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000\nppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\npp"
+"sr\u0000 com.sun.msv.grammar.OneOrMoreExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001ccom.sun.m"
+"sv.grammar.UnaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\u0003expq\u0000~\u0000\u0003xq\u0000~\u0000\u0004sr\u0000\u0011java.lang"
+".Boolean\u00cd r\u0080\u00d5\u009c\u00fa\u00ee\u0002\u0000\u0001Z\u0000\u0005valuexp\u0000psr\u0000 com.sun.msv.grammar.Attri"
+"buteExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003expq\u0000~\u0000\u0003L\u0000\tnameClassq\u0000~\u0000\u0001xq\u0000~\u0000\u0004q\u0000~\u0000\"psr"
+"\u00002com.sun.msv.grammar.Expression$AnyStringExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001"
+"\u0002\u0000\u0000xq\u0000~\u0000\u0004sq\u0000~\u0000!\u0001q\u0000~\u0000&sr\u0000 com.sun.msv.grammar.AnyNameClass\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.sun.msv.grammar.NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpsr\u00000co"
+"m.sun.msv.grammar.Expression$EpsilonExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000"
+"~\u0000\u0004q\u0000~\u0000\'q\u0000~\u0000,sr\u0000#com.sun.msv.grammar.SimpleNameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001"
+"\u0002\u0000\u0002L\u0000\tlocalNamet\u0000\u0012Ljava/lang/String;L\u0000\fnamespaceURIq\u0000~\u0000.xq\u0000~"
+"\u0000)t\u0000\u001cgenerated.bpmnxpdl.BasicTypet\u0000+http://java.sun.com/jaxb"
+"/xjc/dummy-elementssq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq"
+"\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000 ru.runa.xpdl.generated.bpmnxpdl."
+"BasicTypeTypeq\u0000~\u00001sq\u0000~\u0000\nppsq\u0000~\u0000#q\u0000~\u0000\"psr\u0000\u001bcom.sun.msv.gramma"
+"r.DataExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\u0002dtt\u0000\u001fLorg/relaxng/datatype/Datatype;L"
+"\u0000\u0006exceptq\u0000~\u0000\u0003L\u0000\u0004namet\u0000\u001dLcom/sun/msv/util/StringPair;xq\u0000~\u0000\u0004pp"
+"sr\u0000\"com.sun.msv.datatype.xsd.QnameType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000*com.sun"
+".msv.datatype.xsd.BuiltinAtomicType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000%com.sun.ms"
+"v.datatype.xsd.ConcreteType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\'com.sun.msv.dataty"
+"pe.xsd.XSDatatypeImpl\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\fnamespaceUriq\u0000~\u0000.L\u0000\btypeN"
+"ameq\u0000~\u0000.L\u0000\nwhiteSpacet\u0000.Lcom/sun/msv/datatype/xsd/WhiteSpace"
+"Processor;xpt\u0000 http://www.w3.org/2001/XMLSchemat\u0000\u0005QNamesr\u00005c"
+"om.sun.msv.datatype.xsd.WhiteSpaceProcessor$Collapse\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001"
+"\u0002\u0000\u0000xr\u0000,com.sun.msv.datatype.xsd.WhiteSpaceProcessor\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002"
+"\u0000\u0000xpsr\u00000com.sun.msv.grammar.Expression$NullSetExpression\u0000\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0004q\u0000~\u0000\"psr\u0000\u001bcom.sun.msv.util.StringPair\u00d0t\u001ejB\u008f\u008d\u00a0\u0002\u0000"
+"\u0002L\u0000\tlocalNameq\u0000~\u0000.L\u0000\fnamespaceURIq\u0000~\u0000.xpq\u0000~\u0000Gq\u0000~\u0000Fsq\u0000~\u0000-t\u0000\u0004t"
+"ypet\u0000)http://www.w3.org/2001/XMLSchema-instanceq\u0000~\u0000,sq\u0000~\u0000-t\u0000"
+"\tBasicTypet\u0000 http://www.wfmc.org/2008/XPDL2.1sq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\n"
+"ppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000\u001fgenerated."
+"bpmnxpdl.DeclaredTypeq\u0000~\u00001sq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\npp"
+"sq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000#ru.runa.xpdl.generated.bp"
+"mnxpdl.DeclaredTypeTypeq\u0000~\u00001sq\u0000~\u0000\nppsq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000?q\u0000~\u0000Oq\u0000"
+"~\u0000,sq\u0000~\u0000-t\u0000\fDeclaredTypeq\u0000~\u0000Tsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq"
+"\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000\u001dgenerated.bpmnxpdl.SchemaT"
+"ypeq\u0000~\u00001sq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#"
+"q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000!ru.runa.xpdl.generated.bpmnxpdl.SchemaTypeT"
+"ypeq\u0000~\u00001sq\u0000~\u0000\nppsq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000?q\u0000~\u0000Oq\u0000~\u0000,sq\u0000~\u0000-t\u0000\nSchemaTy"
+"peq\u0000~\u0000Tsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000"
+"~\u0000,sq\u0000~\u0000-t\u0000$ru.runa.xpdl.generated.bpmnxpdl.ExternalReferenceq\u0000~\u00001sq\u0000~\u0000\u0000p"
+"p\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000"
+"*q\u0000~\u0000,sq\u0000~\u0000-t\u0000(ru.runa.xpdl.generated.bpmnxpdl.ExternalReferenceTypeq\u0000~\u00001"
+"sq\u0000~\u0000\nppsq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000?q\u0000~\u0000Oq\u0000~\u0000,sq\u0000~\u0000-t\u0000\u0011ExternalReferenc"
+"eq\u0000~\u0000Tsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~"
+"\u0000,sq\u0000~\u0000-t\u0000\u001dgenerated.bpmnxpdl.RecordTypeq\u0000~\u00001sq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u0007"
+"ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq"
+"\u0000~\u0000-t\u0000!ru.runa.xpdl.generated.bpmnxpdl.RecordTypeTypeq\u0000~\u00001sq\u0000~\u0000\nppsq\u0000~\u0000#q"
+"\u0000~\u0000\"pq\u0000~\u0000?q\u0000~\u0000Oq\u0000~\u0000,sq\u0000~\u0000-t\u0000\nRecordTypeq\u0000~\u0000Tsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\np"
+"psq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000\u001cgenerated.b"
+"pmnxpdl.UnionTypeq\u0000~\u00001sq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~"
+"\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000 ru.runa.xpdl.generated.bpmnxp"
+"dl.UnionTypeTypeq\u0000~\u00001sq\u0000~\u0000\nppsq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000?q\u0000~\u0000Oq\u0000~\u0000,sq\u0000~"
+"\u0000-t\u0000\tUnionTypeq\u0000~\u0000Tsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"p"
+"q\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000\"ru.runa.xpdl.generated.bpmnxpdl.EnumerationTypeq\u0000"
+"~\u00001sq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\""
+"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000&ru.runa.xpdl.generated.bpmnxpdl.EnumerationTypeT"
+"ypeq\u0000~\u00001sq\u0000~\u0000\nppsq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000?q\u0000~\u0000Oq\u0000~\u0000,sq\u0000~\u0000-t\u0000\u000fEnumerat"
+"ionTypeq\u0000~\u0000Tsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000"
+"~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000\u001cgenerated.bpmnxpdl.ArrayTypeq\u0000~\u00001sq\u0000~\u0000\u0000pp\u0000s"
+"q\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000"
+"~\u0000,sq\u0000~\u0000-t\u0000 ru.runa.xpdl.generated.bpmnxpdl.ArrayTypeTypeq\u0000~\u00001sq\u0000~\u0000\nppsq\u0000"
+"~\u0000#q\u0000~\u0000\"pq\u0000~\u0000?q\u0000~\u0000Oq\u0000~\u0000,sq\u0000~\u0000-t\u0000\tArrayTypeq\u0000~\u0000Tsq\u0000~\u0000\u0000pp\u0000sq\u0000~"
+"\u0000\nppsq\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000\u001bgenerate"
+"d.bpmnxpdl.ListTypeq\u0000~\u00001sq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\u0007ppsq\u0000~\u0000\u0000pp\u0000sq\u0000~\u0000\nppsq"
+"\u0000~\u0000\u001eq\u0000~\u0000\"psq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000&q\u0000~\u0000*q\u0000~\u0000,sq\u0000~\u0000-t\u0000\u001fgenerated.bpmn"
+"xpdl.ListTypeTypeq\u0000~\u00001sq\u0000~\u0000\nppsq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000?q\u0000~\u0000Oq\u0000~\u0000,sq\u0000"
+"~\u0000-t\u0000\bListTypeq\u0000~\u0000Tsq\u0000~\u0000\nppsq\u0000~\u0000#q\u0000~\u0000\"pq\u0000~\u0000?q\u0000~\u0000Oq\u0000~\u0000,sq\u0000~\u0000-"
+"t\u0000\bDataTypeq\u0000~\u0000Tsr\u0000\"com.sun.msv.grammar.ExpressionPool\u0000\u0000\u0000\u0000\u0000\u0000"
+"\u0000\u0001\u0002\u0000\u0001L\u0000\bexpTablet\u0000/Lcom/sun/msv/grammar/ExpressionPool$Close"
+"dHash;xpsr\u0000-com.sun.msv.grammar.ExpressionPool$ClosedHash\u00d7j\u00d0"
+"N\u00ef\u00e8\u00ed\u001c\u0003\u0000\u0003I\u0000\u0005countB\u0000\rstreamVersionL\u0000\u0006parentt\u0000$Lcom/sun/msv/gra"
+"mmar/ExpressionPool;xp\u0000\u0000\u0000I\u0001pq\u0000~\u0000\u0014q\u0000~\u0000\u001bq\u0000~\u0000\u001aq\u0000~\u0000\u0016q\u0000~\u0000\u0010q\u0000~\u0000\u0017q\u0000"
+"~\u0000\u00c2q\u0000~\u0000\u00b8q\u0000~\u0000\u00b0q\u0000~\u0000\u00a6q\u0000~\u0000\u009eq\u0000~\u0000\u0094q\u0000~\u0000\u008cq\u0000~\u0000\u0082q\u0000~\u0000zq\u0000~\u0000pq\u0000~\u0000hq\u0000~\u0000^q\u0000"
+"~\u0000Vq\u0000~\u00005q\u0000~\u0000\u001dq\u0000~\u0000\u0013q\u0000~\u0000\u00caq\u0000~\u0000\u00d4q\u0000~\u0000\u00dcq\u0000~\u0000\u00bdq\u0000~\u0000\u00abq\u0000~\u0000\u0099q\u0000~\u0000\u0087q\u0000~\u0000uq\u0000"
+"~\u0000cq\u0000~\u0000:q\u0000~\u0000\u00cfq\u0000~\u0000\u00e1q\u0000~\u0000\u00e5q\u0000~\u0000\u0015q\u0000~\u0000\tq\u0000~\u0000\u0011q\u0000~\u0000\u0018q\u0000~\u0000\fq\u0000~\u0000\rq\u0000~\u0000\u00b6q\u0000"
+"~\u0000\u00a4q\u0000~\u0000\u0092q\u0000~\u0000\u0080q\u0000~\u0000nq\u0000~\u0000\\q\u0000~\u00003q\u0000~\u0000\u00c8q\u0000~\u0000\u00daq\u0000~\u0000\u0012q\u0000~\u0000\u000fq\u0000~\u0000\u00c3q\u0000~\u0000\u00b9q\u0000"
+"~\u0000\u00b1q\u0000~\u0000\u00a7q\u0000~\u0000\u009fq\u0000~\u0000\u0095q\u0000~\u0000\u008dq\u0000~\u0000\u0083q\u0000~\u0000{q\u0000~\u0000qq\u0000~\u0000iq\u0000~\u0000_q\u0000~\u0000Wq\u0000~\u00006q\u0000"
+"~\u0000 q\u0000~\u0000\u00cbq\u0000~\u0000\u00d5q\u0000~\u0000\u00ddq\u0000~\u0000\u0019q\u0000~\u0000\u000eq\u0000~\u0000\u000bx"));
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
            return ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this;
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
                        if (("DataType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 1;
                            return ;
                        }
                        break;
                    case  1 :
                        if (("BasicType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("BasicType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("DeclaredType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("DeclaredType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("SchemaType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("SchemaType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("ExternalReference" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("ExternalReference" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("RecordType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("RecordType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("UnionType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("UnionType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("EnumerationType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("EnumerationType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("ArrayType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("ArrayType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("ListType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("ListType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
                            spawnHandlerFromEnterElement((((ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeTypeImpl) ru.runa.xpdl.generated.bpmnxpdl.impl.DataTypeImpl.this).new Unmarshaller(context)), 2, ___uri, ___local, ___qname, __atts);
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
                        if (("DataType" == ___local)&&("http://www.wfmc.org/2008/XPDL2.1" == ___uri)) {
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
                    }
                } catch (java.lang.RuntimeException e) {
                    handleUnexpectedTextException(value, e);
                }
                break;
            }
        }

    }

}
