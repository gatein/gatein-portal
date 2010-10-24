<?xml version="1.0"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:foo="http://maven.apache.org/POM/4.0.0">
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/" >
    <project name="foo">
      <target name="foo">
        <copy todir="target/tomcat/libs">
          <xsl:for-each select="//foo:dependencies/foo:dependency[count(foo:type)=0 or foo:type/text()='jar']">
            <xsl:element name="fileset">
              <xsl:attribute name="refid"><xsl:value-of select="./foo:groupId"/>:<xsl:value-of select="./foo:artifactId"/>:jar</xsl:attribute>
            </xsl:element>
          </xsl:for-each>
        </copy>
        <xsl:for-each select="//foo:dependencies/foo:dependency[foo:type/text()='war']">
          <xsl:element name="copy">
            <xsl:attribute name="tofile">target/tomcat/webapps/<xsl:value-of select="./comment()"/></xsl:attribute>
            <xsl:element name="fileset">
              <xsl:attribute name="refid"><xsl:value-of select="./foo:groupId"/>:<xsl:value-of select="./foo:artifactId"/>:war</xsl:attribute>
            </xsl:element>
          </xsl:element>
        </xsl:for-each>
      </target>
    </project>
  </xsl:template>
</xsl:stylesheet>