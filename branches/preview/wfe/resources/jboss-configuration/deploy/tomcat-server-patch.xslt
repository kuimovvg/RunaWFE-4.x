<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="text()|@*|*|processing-instruction()|comment()"> 
        <xsl:copy> 
                <xsl:apply-templates select="text()|@*|*|processing-instruction()|comment()"/> 
        </xsl:copy> 
    </xsl:template> 

	<xsl:template match="Service/Connector[@port='8080']">
		<xsl:element name="Connector">
			<xsl:attribute name="port">8080</xsl:attribute>
			<xsl:attribute name="address">${jboss.bind.address}</xsl:attribute>
			<xsl:attribute name="maxThreads">500</xsl:attribute>
			<xsl:attribute name="minSpareThreads">25</xsl:attribute>
			<xsl:attribute name="maxSpareThreads">75</xsl:attribute>
			<xsl:attribute name="enableLookups">false</xsl:attribute>
			<xsl:attribute name="redirectPort">8443</xsl:attribute>
			<xsl:attribute name="acceptCount">100</xsl:attribute>
			<xsl:attribute name="connectionTimeout">20000</xsl:attribute>
			<xsl:attribute name="disableUploadTimeout">true</xsl:attribute>
			<xsl:attribute name="URIEncoding">UTF-8</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<xsl:template match="Service/Engine[@name='jboss.web']/Realm[@className='org.jboss.web.tomcat.security.JBossSecurityMgrRealm']">
		<xsl:element name="Realm">
			<xsl:attribute name="className">org.jboss.web.tomcat.security.JBossSecurityMgrRealm</xsl:attribute>
			<xsl:attribute name="certificatePrincipal">org.jboss.security.auth.certs.SubjectDNMapping</xsl:attribute>
		</xsl:element>
	</xsl:template>

	<xsl:template match="Service/Engine[@name='jboss.web']/Host[@name='localhost']">
		<xsl:element name="Host">
			<xsl:attribute name="name">localhost</xsl:attribute>
			<xsl:attribute name="autoDeploy">false</xsl:attribute>
			<xsl:attribute name="deployOnStartup">false</xsl:attribute>
			<xsl:attribute name="deployXML">false</xsl:attribute>
			<xsl:element name="DefaultContext">
				<xsl:attribute name="cookies">true</xsl:attribute>
				<xsl:attribute name="crossContext">true</xsl:attribute>
				<xsl:attribute name="override">true</xsl:attribute>
			</xsl:element>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
