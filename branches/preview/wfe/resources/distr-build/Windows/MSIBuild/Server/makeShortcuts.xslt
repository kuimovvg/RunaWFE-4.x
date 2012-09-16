<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns="http://schemas.microsoft.com/wix/2003/01/wi" xmlns:src="http://schemas.microsoft.com/wix/2003/01/wi">
  <xsl:output method="xml" />

  <xsl:template match="@*|node()|text()">
     <xsl:copy>
        <xsl:apply-templates select="@*|node()|text()"/>
     </xsl:copy>
  </xsl:template>

  <xsl:template match="src:Directory[@LongName='wfe-server-jboss' or @LongName='wfe-server-config' or @LongName='wfe-botstation-config']">
    <xsl:apply-templates select="node()|text()"/>
  </xsl:template>

</xsl:stylesheet>
