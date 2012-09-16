<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="text()|@*|*|processing-instruction()|comment()"> 
        <xsl:copy> 
                <xsl:apply-templates select="text()|@*|*|processing-instruction()|comment()"/> 
        </xsl:copy> 
    </xsl:template> 

	<xsl:template match="server/mbean[@name='jboss.web:service=WebServer']/attribute[@name='UseJBossWebLoader']">
		<xsl:element name="attribute">
			<xsl:attribute name="name">UseJBossWebLoader</xsl:attribute>
			<xsl:text>true</xsl:text>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
