<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="text()|@*|*|processing-instruction()|comment()"> 
        <xsl:copy> 
                <xsl:apply-templates select="text()|@*|*|processing-instruction()|comment()"/> 
        </xsl:copy> 
    </xsl:template> 

	<xsl:template match="server/mbean[@name='jboss:service=TransactionManager']/attribute[@name='TransactionTimeout']">
		<xsl:choose>
			<xsl:when test="text() &lt; 12000">
				<xsl:element name="attribute">
					<xsl:attribute name="name">TransactionTimeout</xsl:attribute>
					<xsl:text>12000</xsl:text>
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy> 
						<xsl:apply-templates select="text()|@*|*|processing-instruction()|comment()"/> 
				</xsl:copy> 
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="server/mbean/attribute[@name='URLComparator']">
		<xsl:element name="attribute">
			<xsl:attribute name="name">URLComparator</xsl:attribute>
			<xsl:text>ru.runa.jboss.DeploymentSorter.RunaDeploymentSorter</xsl:text>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
