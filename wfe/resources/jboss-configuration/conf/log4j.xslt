<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" doctype-system="log4j.dtd"/>

    <xsl:template match="text()|@*|*|processing-instruction()|comment()"> 
        <xsl:copy> 
                <xsl:apply-templates select="text()|@*|*|processing-instruction()|comment()"/> 
        </xsl:copy> 
    </xsl:template> 

	<xsl:template match="log4j:configuration" xmlns:log4j="http://jakarta.apache.org/log4j/">
		<xsl:copy> 
				<xsl:apply-templates select="renderer|appender"/> 

			<xsl:if test="not(category[@name='org.hibernate'])">
				<xsl:comment>This category added by RunaWFE installer</xsl:comment>
<xsl:text>
</xsl:text>
				<xsl:element name="category">
					<xsl:attribute name="name">org.hibernate</xsl:attribute>
					<xsl:element name="priority">
						<xsl:attribute name="value">ERROR</xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:if>
			<xsl:if test="not(category[@name='net.sf.ehcache'])">
				<xsl:comment>This category added by RunaWFE installer</xsl:comment>
<xsl:text>
</xsl:text>
				<xsl:element name="category">
					<xsl:attribute name="name">net.sf.ehcache</xsl:attribute>
					<xsl:element name="priority">
						<xsl:attribute name="value">ERROR</xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:if>
			<xsl:if test="not(category[@name='ru.runa.bpm.bytes'])">
				<xsl:comment>This category added by RunaWFE installer</xsl:comment>
<xsl:text>
</xsl:text>
				<xsl:element name="category">
					<xsl:attribute name="name">ru.runa.bpm.bytes</xsl:attribute>
					<xsl:element name="priority">
						<xsl:attribute name="value">INFO</xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:if>
			<xsl:if test="not(category[@name='ru.runa.commons.validation.ValidationXmlParser'])">
				<xsl:comment>This category added by RunaWFE installer</xsl:comment>
<xsl:text>
</xsl:text>
				<xsl:element name="category">
					<xsl:attribute name="name">ru.runa.commons.validation.ValidationXmlParser</xsl:attribute>
					<xsl:element name="priority">
						<xsl:attribute name="value">WARN</xsl:attribute>
					</xsl:element>
				</xsl:element>
			</xsl:if>
				<xsl:apply-templates select="text()|@*|*[not(local-name()='renderer')][not(local-name()='appender')]|processing-instruction()|comment()"/> 
		</xsl:copy> 
	</xsl:template>

</xsl:stylesheet>
