<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="text()|@*|*|processing-instruction()|comment()"> 
        <xsl:copy> 
                <xsl:apply-templates select="text()|@*|*|processing-instruction()|comment()"/> 
        </xsl:copy> 
    </xsl:template> 

	<xsl:template match="container-interceptors[../container-name='Standard Stateless SessionBean' and not(interceptor='ru.runa.jboss.interceptor.CachingLogicInterceptor')]">
	        <xsl:copy> 
			<xsl:element name="interceptor">ru.runa.jboss.interceptor.CachingLogicInterceptor</xsl:element>
                	<xsl:apply-templates select="text()|@*|*|processing-instruction()|comment()"/> 
	        </xsl:copy> 
	</xsl:template>

</xsl:stylesheet>
