<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns="http://schemas.microsoft.com/wix/2003/01/wi" xmlns:src="http://schemas.microsoft.com/wix/2003/01/wi">
  <xsl:output method="xml" />

  <xsl:template match="@*|node()|text()">
     <xsl:copy>
        <xsl:apply-templates select="@*|node()|text()"/>
     </xsl:copy>
  </xsl:template>

  <xsl:template match="src:File[@Name='runa-gpd.exe' or @LongName='runa-gpd.exe']">
     <xsl:copy>
        <xsl:apply-templates select="@*|node()|text()"/>
	<xsl:element name="Shortcut">
          <xsl:attribute name="Id">startmenuRunaWFEGPD20</xsl:attribute>
          <xsl:attribute name="Directory">ProgramMenuDir</xsl:attribute>
          <xsl:attribute name="Name">GPDLink</xsl:attribute>
          <xsl:attribute name="LongName">$(loc.RunaWFE_Client_MenuGPDName)</xsl:attribute>
          <xsl:attribute name="Description">$(loc.RunaWFE_Client_MenuGPDDescription)</xsl:attribute>
          <xsl:attribute name="WorkingDirectory"><xsl:value-of select="../../@Id"/></xsl:attribute>
          <xsl:attribute name="IconIndex">0</xsl:attribute>
          <xsl:attribute name="Icon">RunaWFE_GPD.ico</xsl:attribute>
          <xsl:attribute name="Show">normal</xsl:attribute>
        </xsl:element>
     </xsl:copy>
  </xsl:template>

  <xsl:template match="src:File[(@Name='run.bat' or @LongName='run.bat') and ../../@Name='bin']">
     <xsl:copy>
        <xsl:apply-templates select="@*|node()|text()"/>
	<xsl:element name="Shortcut">
          <xsl:attribute name="Id">startmenuRunaWFESimulatorStart</xsl:attribute>
          <xsl:attribute name="Directory">ProgramMenuDir</xsl:attribute>
          <xsl:attribute name="Name">StartSim</xsl:attribute>
          <xsl:attribute name="LongName">$(loc.RunaWFE_Client_MenuStartSimulatorLinkName)</xsl:attribute>
          <xsl:attribute name="Description">$(loc.RunaWFE_Client_MenuStartSimulatorLinkDescription)</xsl:attribute>
          <xsl:attribute name="WorkingDirectory"><xsl:value-of select="../../@Id"/></xsl:attribute>
          <xsl:attribute name="IconIndex">0</xsl:attribute>
          <xsl:attribute name="Icon">RunaWFE_Simulator.ico</xsl:attribute>
          <xsl:attribute name="Show">normal</xsl:attribute>
        </xsl:element>
     </xsl:copy>
  </xsl:template>

  <xsl:template match="src:File[@Name='shutdown.bat' or @LongName='shutdown.bat']">
     <xsl:copy>
        <xsl:apply-templates select="@*|node()|text()"/>
	<xsl:element name="Shortcut">
          <xsl:attribute name="Id">startmenuRunaWFESimulatorStop</xsl:attribute>
          <xsl:attribute name="Directory">ProgramMenuDir</xsl:attribute>
          <xsl:attribute name="Name">StopSim</xsl:attribute>
          <xsl:attribute name="LongName">$(loc.RunaWFE_Client_MenuStopSimulatorLinkName)</xsl:attribute>
          <xsl:attribute name="Description">$(loc.RunaWFE_Client_MenuStopSimulatorLinkDescription)</xsl:attribute>
          <xsl:attribute name="WorkingDirectory"><xsl:value-of select="../../@Id"/></xsl:attribute>
          <xsl:attribute name="IconIndex">0</xsl:attribute>
          <xsl:attribute name="Icon">RunaWFE_Simulator.ico</xsl:attribute>
          <xsl:attribute name="Show">normal</xsl:attribute>
          <xsl:attribute name="Arguments"> -S</xsl:attribute>
        </xsl:element>
     </xsl:copy>
  </xsl:template>

  <xsl:template match="src:File[@Name='runa_tasks.exe' or @LongName='runa_tasks.exe']">
     <xsl:copy>
        <xsl:apply-templates select="@*|node()|text()"/>
	<xsl:element name="Shortcut">
          <xsl:attribute name="Id">startmenuRunaWFERTN20</xsl:attribute>
          <xsl:attribute name="Directory">ProgramMenuDir</xsl:attribute>
          <xsl:attribute name="Name">RTN</xsl:attribute>
          <xsl:attribute name="LongName">$(loc.RunaWFE_Client_MenuRTNLinkLongName)</xsl:attribute>
          <xsl:attribute name="Description">$(loc.RunaWFE_Client_MenuRTNLinkDescription)</xsl:attribute>
          <xsl:attribute name="WorkingDirectory"><xsl:value-of select="../../@Id"/></xsl:attribute>
          <xsl:attribute name="IconIndex">0</xsl:attribute>
          <xsl:attribute name="Icon">RunaWFE_RTN.ico</xsl:attribute>
          <xsl:attribute name="Show">normal</xsl:attribute>
        </xsl:element>
	<xsl:element name="Shortcut">
          <xsl:attribute name="Id">startupmenuRunaWFERTN20</xsl:attribute>
          <xsl:attribute name="Directory">StartupFolder</xsl:attribute>
          <xsl:attribute name="Name">RTN</xsl:attribute>
          <xsl:attribute name="LongName">$(loc.RunaWFE_Client_MenuRTNLinkLongName)</xsl:attribute>
          <xsl:attribute name="Description">$(loc.RunaWFE_Client_MenuRTNLinkDescription)</xsl:attribute>
          <xsl:attribute name="WorkingDirectory"><xsl:value-of select="../../@Id"/></xsl:attribute>
          <xsl:attribute name="IconIndex">0</xsl:attribute>
          <xsl:attribute name="Icon">RunaWFE_RTN.ico</xsl:attribute>
          <xsl:attribute name="Show">normal</xsl:attribute>
        </xsl:element>
     </xsl:copy>
  </xsl:template>

  <xsl:template match="src:File[@Name='bot-invoker.bat' or @LongName='bot-invoker.bat']">
     <xsl:copy>
        <xsl:apply-templates select="@*|node()|text()"/>
	<xsl:element name="Shortcut">
          <xsl:attribute name="Id">startmenuRunaWFEBotStart</xsl:attribute>
          <xsl:attribute name="Directory">ProgramMenuDir</xsl:attribute>
          <xsl:attribute name="Name">BotStart</xsl:attribute>
          <xsl:attribute name="LongName">$(loc.RunaWFE_Client_MenuBotStartLinkName)</xsl:attribute>
          <xsl:attribute name="Description">$(loc.RunaWFE_Client_MenuBotStartLinkDescription)</xsl:attribute>
          <xsl:attribute name="WorkingDirectory"><xsl:value-of select="../../@Id"/></xsl:attribute>
          <xsl:attribute name="IconIndex">0</xsl:attribute>
          <xsl:attribute name="Icon">RunaWFE_Simulator.ico</xsl:attribute>
          <xsl:attribute name="Show">normal</xsl:attribute>
          <xsl:attribute name="Arguments"> start</xsl:attribute>
        </xsl:element>
	<xsl:element name="Shortcut">
          <xsl:attribute name="Id">startmenuRunaWFEBotStop</xsl:attribute>
          <xsl:attribute name="Directory">ProgramMenuDir</xsl:attribute>
          <xsl:attribute name="Name">BotStop</xsl:attribute>
          <xsl:attribute name="LongName">$(loc.RunaWFE_Client_MenuBotStopLinkName)</xsl:attribute>
          <xsl:attribute name="Description">$(loc.RunaWFE_Client_MenuBotStopLinkDescription)</xsl:attribute>
          <xsl:attribute name="WorkingDirectory"><xsl:value-of select="../../@Id"/></xsl:attribute>
          <xsl:attribute name="IconIndex">0</xsl:attribute>
          <xsl:attribute name="Icon">RunaWFE_Simulator.ico</xsl:attribute>
          <xsl:attribute name="Show">normal</xsl:attribute>
          <xsl:attribute name="Arguments"> stop</xsl:attribute>
        </xsl:element>
     </xsl:copy>
  </xsl:template>

</xsl:stylesheet>