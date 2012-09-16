<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:src="http://schemas.microsoft.com/wix/2003/01/wi" xmlns="http://schemas.microsoft.com/wix/2003/01/wi">
  <xsl:output method="xml" indent="no"/>

  <xsl:template match="/">
    <xsl:text>
</xsl:text>
    <xsl:element name="Wix" namespace="http://schemas.microsoft.com/wix/2003/01/wi">
    <xsl:text>
  </xsl:text>
      <xsl:element name="Fragment">
        <xsl:apply-templates select="@*|node()|text()"/>
    <xsl:text>
  </xsl:text>
      </xsl:element>
    <xsl:text>
</xsl:text>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*|node()|text()">
     <xsl:apply-templates select="@*|node()|text()"/>
  </xsl:template>

  <xsl:template match="src:Directory[name(..)='DirectoryRef']">
    <xsl:text>
    </xsl:text>
    <xsl:element name="ComponentGroup">
      <xsl:choose>
        <xsl:when test="starts-with(@LongName, 'wfe-server-jboss')">
          <xsl:attribute name="Id">JBOSS_ComponentsGroup</xsl:attribute>
        </xsl:when>
        <xsl:when test="starts-with(@LongName, 'wfe-botstation-config')">
          <xsl:attribute name="Id">BOTCONF_ComponentsGroup</xsl:attribute>
        </xsl:when>
        <xsl:when test="starts-with(@LongName, 'wfe-server-config')">
          <xsl:attribute name="Id">SERVCONF_ComponentsGroup</xsl:attribute>
        </xsl:when>
      </xsl:choose>
      <xsl:apply-templates select="@*|node()|text()"/>
    <xsl:text>
    </xsl:text>
    </xsl:element>
  </xsl:template>

  <xsl:template match="src:Component">
    <xsl:text>
      </xsl:text>
    <xsl:element name="ComponentRef">
      <xsl:attribute name="Id"><xsl:value-of select="@Id"/></xsl:attribute>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>