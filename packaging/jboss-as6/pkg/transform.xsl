<?xml version="1.0"?>
<xsl:stylesheet
      version="1.0"
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
      xmlns:mvn="http://maven.apache.org/POM/4.0.0">
   <xsl:param name="default.jar.context"/>
   <xsl:param name="default.war.context"/>
   <xsl:param name="default.ear.context"/>
   <xsl:param name="lib.context"/>
   <xsl:param name="deploy.context"/>
   <xsl:output method="xml" indent="yes"/>
   <xsl:template match="/">
      <xsl:comment>Generated file</xsl:comment>
      <project name="copy-dependencies">
         <target name="copy-dependencies">
            <xsl:for-each select="//mvn:dependencies/mvn:dependency">
               <xsl:variable name="dest.name">
                  <xsl:for-each select="processing-instruction()[name()='move']">
                     <xsl:value-of select="."/>
                  </xsl:for-each>
               </xsl:variable>

               <xsl:variable name="a_ext">
                  <xsl:for-each select="mvn:type">
                     <xsl:value-of select="text()"/>
                  </xsl:for-each>
               </xsl:variable>
               <xsl:variable name="ext">
                  <xsl:choose>
                     <xsl:when test="$a_ext=''">jar</xsl:when>
                     <xsl:otherwise>
                        <xsl:value-of select="$a_ext"/>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:variable>

               <xsl:variable name="a_context">
                  <xsl:for-each select="processing-instruction()[name()='context']">
                     <xsl:choose>
                        <xsl:when test=".='LIB'">
                           <xsl:value-of select="$lib.context"/>
                        </xsl:when>
                        <xsl:when test=".='DEPLOY'">
                           <xsl:value-of select="$deploy.context"/>
                        </xsl:when>
                        <xsl:otherwise></xsl:otherwise>
                     </xsl:choose>
                  </xsl:for-each>
               </xsl:variable>
               <xsl:variable name="context">
                  <xsl:choose>
                     <xsl:when test="$a_context=''">
                        <xsl:choose>
                           <xsl:when test="$ext='war'">
                              <xsl:value-of select="$default.war.context"/>
                           </xsl:when>
                           <xsl:when test="$ext='ear'">
                              <xsl:value-of select="$default.ear.context"/>
                           </xsl:when>
                           <xsl:otherwise>
                              <xsl:value-of select="$default.jar.context"/>
                           </xsl:otherwise>
                        </xsl:choose>
                     </xsl:when>
                     <xsl:otherwise>
                        <xsl:value-of select="$a_context"/>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:variable>

               <xsl:variable name="expand">
                  <xsl:for-each select="processing-instruction()[name()='expand']">true</xsl:for-each>
               </xsl:variable>

               <xsl:choose>
                  <xsl:when test="$expand=''">
                     <xsl:choose>
                        <xsl:when test="$dest.name=''">
                           <xsl:element name="copy">
                              <xsl:attribute name="todir">
                                 <xsl:value-of select="$context"/>
                              </xsl:attribute>
                              <xsl:element name="fileset">
                                 <xsl:attribute name="refid"><xsl:value-of select="./mvn:groupId"/>:<xsl:value-of
                                       select="./mvn:artifactId"/>:<xsl:value-of select="$ext"/>
                                 </xsl:attribute>
                              </xsl:element>
                           </xsl:element>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:element name="copy">
                              <xsl:attribute name="tofile"><xsl:value-of select="$context"/>/<xsl:value-of select="$dest.name"/>
                              </xsl:attribute>
                              <xsl:element name="fileset">
                                 <xsl:attribute name="refid"><xsl:value-of select="./mvn:groupId"/>:<xsl:value-of
                                       select="./mvn:artifactId"/>:<xsl:value-of select="$ext"/>
                                 </xsl:attribute>
                              </xsl:element>
                           </xsl:element>
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:choose>
                        <xsl:when test="$dest.name=''">
                           <xsl:element name="unjar">
                              <xsl:attribute name="dest"><xsl:value-of select="$context"/>/<xsl:value-of
                                    select="./mvn:artifactId"/>.<xsl:value-of select="$ext"/>
                              </xsl:attribute>
                              <xsl:element name="fileset">
                                 <xsl:attribute name="refid"><xsl:value-of select="./mvn:groupId"/>:<xsl:value-of
                                       select="./mvn:artifactId"/>:<xsl:value-of select="$ext"/>
                                 </xsl:attribute>
                              </xsl:element>
                           </xsl:element>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:element name="unjar">
                              <xsl:attribute name="dest"><xsl:value-of select="$context"/>/<xsl:value-of select="$dest.name"/>
                              </xsl:attribute>
                              <xsl:element name="fileset">
                                 <xsl:attribute name="refid"><xsl:value-of select="./mvn:groupId"/>:<xsl:value-of
                                       select="./mvn:artifactId"/>:<xsl:value-of select="$ext"/>
                                 </xsl:attribute>
                              </xsl:element>
                           </xsl:element>
                        </xsl:otherwise>
                     </xsl:choose>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:for-each>
         </target>
      </project>
   </xsl:template>
</xsl:stylesheet>