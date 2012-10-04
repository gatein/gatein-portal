<?xml version="1.0"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-result-prefixes="str">

  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/">
    <xsl:comment>Generated file</xsl:comment>
    <tomcat.download>
      <xsl:for-each select="(//a[./text() = 'zip'])[1]">
        <xsl:element name="url">
          <xsl:value-of select="@href"/>
        </xsl:element>
        <xsl:element name="dir.name">
          <xsl:value-of select="@href"/>
        </xsl:element>
      </xsl:for-each>
      <xsl:for-each select="(//a[./text() = 'md5'])[1]">
        <xsl:element name="url.md5">
          <xsl:value-of select="@href"/>
        </xsl:element>
      </xsl:for-each>
    </tomcat.download>
  </xsl:template>
</xsl:stylesheet>