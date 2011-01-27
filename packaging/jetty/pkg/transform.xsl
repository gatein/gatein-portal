<?xml version="1.0"?>
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mvn="http://maven.apache.org/POM/4.0.0">
  <xsl:param name="lib.dir"/>
  <xsl:param name="webapps.dir"/>
  <xsl:output method="xml" indent="yes"/>
  <xsl:template match="/" >
    <xsl:comment>Generated file</xsl:comment>
    <project name="copy-dependencies">
      <target name="copy-dependencies">
        <xsl:element name="copy">
          <xsl:attribute name="todir"><xsl:value-of select="$lib.dir"/></xsl:attribute>
          <xsl:for-each select="//mvn:dependencies/mvn:dependency[count(mvn:type)=0 or mvn:type/text()='jar']">
            <xsl:element name="fileset">
              <xsl:attribute name="refid"><xsl:value-of select="./mvn:groupId"/>:<xsl:value-of select="./mvn:artifactId"/>:jar</xsl:attribute>
            </xsl:element>
          </xsl:for-each>
        </xsl:element>
        <xsl:for-each select="//mvn:dependencies/mvn:dependency[mvn:type/text()='war']">
          <xsl:variable name="webapps.name">
            <xsl:for-each select="processing-instruction()[name()='rename']">
              <xsl:value-of select="."/>
            </xsl:for-each>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="$webapps.name=''">
              <xsl:element name="copy">
                <xsl:attribute name="todir"><xsl:value-of select="$webapps.dir"/></xsl:attribute>
                <xsl:element name="fileset">
                  <xsl:attribute name="refid"><xsl:value-of select="./mvn:groupId"/>:<xsl:value-of select="./mvn:artifactId"/>:war</xsl:attribute>
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="copy">
                <xsl:attribute name="tofile"><xsl:value-of select="$webapps.dir"/>/<xsl:value-of select="$webapps.name"/></xsl:attribute>
                <xsl:element name="fileset">
                  <xsl:attribute name="refid"><xsl:value-of select="./mvn:groupId"/>:<xsl:value-of select="./mvn:artifactId"/>:war</xsl:attribute>
                </xsl:element>
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>

        </xsl:for-each>
      </target>
    </project>
  </xsl:template>
</xsl:stylesheet>