<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
   xmlns:xalan="http://xml.apache.org/xalan" 
   xmlns:j="urn:jboss:domain:1.3"
   version="2.0"
   exclude-result-prefixes="xalan j">

   <xsl:param name="config"/>
   
   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" xalan:indent-amount="4" standalone="no"/>
   <xsl:strip-space elements="*"/>

   <!-- templates -->

   <xsl:template name="extensions">
        <extension module="org.gatein"/>
   </xsl:template>
   
   <xsl:template name="system-properties">
      <xsl:if test="$config='default'">
         <system-properties>
            <property name="gatein.jcr.config.type" value="local"/>
            <property name="gatein.jcr.index.changefilterclass" value="org.exoplatform.services.jcr.impl.core.query.DefaultChangesFilter"/>
         </system-properties>
      </xsl:if>
      <xsl:if test="$config='clustering'">
         <system-properties>
            <property name="exo.profiles" value="cluster"/>
            <property name="gatein.jcr.config.type" value="cluster"/>
            <property name="gatein.jcr.index.changefilterclass" value="org.exoplatform.services.jcr.impl.core.query.ispn.ISPNIndexChangesFilter"/>
            <property name="gatein.jcr.storage.enabled" value="false"/>
         </system-properties>
      </xsl:if>     
   </xsl:template> 

   <xsl:template name="loggers">
      <logger category="com.google.javascript.jscomp">
         <level name="WARN"/>
      </logger>
   </xsl:template>
   
   <xsl:template name="datasources">
      <xsl:variable name='connection-url-idm'>
         <xsl:choose>
            <xsl:when test="$config='clustering'">jdbc:h2:tcp://localhost/~/jdbcidm_portal</xsl:when>
            <xsl:otherwise>jdbc:h2:file:${jboss.server.data.dir}/gatein/portal/jdbcidm_portal;DB_CLOSE_DELAY=-1</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name='connection-url-jcr'>
         <xsl:choose>
            <xsl:when test="$config='clustering'">jdbc:h2:tcp://localhost/~/jdbcjcr_portal</xsl:when>
            <xsl:otherwise>jdbc:h2:file:${jboss.server.data.dir}/gatein/portal/jdbcjcr_portal;DB_CLOSE_DELAY=-1</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
              
      <datasource jndi-name="java:/jdbcidm_portal" pool-name="IDMPortalDS" enabled="true" use-java-context="true">
         <connection-url><xsl:value-of select="$connection-url-idm"/></connection-url>
         <driver>h2</driver>
         <security>
            <user-name>sa</user-name>
            <password>sa</password>
         </security>
      </datasource>
      <datasource jndi-name="java:/jdbcjcr_portal" pool-name="JCRPortalDS" enabled="true" use-java-context="true">
         <connection-url><xsl:value-of select="$connection-url-jcr"/></connection-url>
         <driver>h2</driver>
         <security>
            <user-name>sa</user-name>
            <password>sa</password>
         </security>
      </datasource>
   </xsl:template>
   
   <xsl:template name="datasources-samples">
      <xsl:if test="$config='default'">   
         <xsl:comment> Uncommented this when deploying gatein-sample-portal </xsl:comment>
         <xsl:comment>
                <![CDATA[<datasource jndi-name="java:/jdbcidm_sample-portal" pool-name="IDMSamplePortalDS" enabled="true" use-java-context="true">
                    <connection-url>jdbc:h2:file:${jboss.server.data.dir}/gatein/sample-portal/jdbcidm_sample_portal;DB_CLOSE_DELAY=-1</connection-url>
                    <driver>h2</driver>
                    <security>
                        <user-name>sa</user-name>
                        <password>sa</password>
                    </security>
                </datasource>]]>
                </xsl:comment>
         <xsl:comment> Uncommented this when deploying gatein-sample-portal </xsl:comment>
         <xsl:comment>
                <![CDATA[<datasource jndi-name="java:/jdbcjcr_sample-portal" pool-name="JCRSamplePortalDS" enabled="true" use-java-context="true">
                    <connection-url>jdbc:h2:file:${jboss.server.data.dir}/gatein/sample-portal/jdbcjcr_sample_portal;DB_CLOSE_DELAY=-1</connection-url>
                    <driver>h2</driver>
                    <security>
                        <user-name>sa</user-name>
                        <password>sa</password>
                    </security>
                </datasource>]]>
                </xsl:comment>
      </xsl:if> 
      <xsl:if test="$config='clustering'">   
         <xsl:comment> Uncommented this when deploying gatein-sample-portal </xsl:comment>
         <xsl:comment>
                <![CDATA[<datasource jndi-name="java:/jdbcidm_sample-portal" pool-name="IDMSamplePortalDS" enabled="true" use-java-context="true">
                    <connection-url>jdbc:h2:tcp://localhost/~/jdbcidm_sample_portal</connection-url>
                    <driver>h2</driver>
                    <security>
                        <user-name>sa</user-name>
                        <password>sa</password>
                    </security>
                </datasource>]]>
                </xsl:comment>
         <xsl:comment> Uncommented this when deploying gatein-sample-portal </xsl:comment>
         <xsl:comment>
                <![CDATA[<datasource jndi-name="java:/jdbcjcr_sample-portal" pool-name="JCRSamplePortalDS" enabled="true" use-java-context="true">
                    <connection-url>jdbc:h2:tcp://localhost/~/jdbcjcr_sample_portal</connection-url>
                    <driver>h2</driver>
                    <security>
                        <user-name>sa</user-name>
                        <password>sa</password>
                    </security>
                </datasource>]]>
                </xsl:comment>
      </xsl:if>
   </xsl:template>
   
   <xsl:template name="deployment-scanner">
      <xsl:attribute name="deployment-timeout">300</xsl:attribute>
   </xsl:template>
   
   <xsl:template name="subsystem">
      <subsystem xmlns="urn:jboss:domain:gatein:1.0">
         <portlet-war-dependencies>
            <dependency name="org.gatein.wci"/>
            <dependency name="org.gatein.pc"/>
            <dependency name="javax.portlet.api"/>
         </portlet-war-dependencies>
      </subsystem>   
   </xsl:template>
   
   <xsl:template name="security-domains">
      <security-domain name="gatein-domain" cache-type="default">
         <authentication>
           <login-module code="org.exoplatform.web.login.FilterDisabledLoginModule" flag="required">
             <module-option name="portalContainerName" value="portal"/>
             <module-option name="realmName" value="gatein-domain"/>
           </login-module>
           <login-module code="org.gatein.security.oauth.jaas.OAuthLoginModule" flag="required">
             <module-option name="portalContainerName" value="portal"/>
             <module-option name="realmName" value="gatein-domain"/>
           </login-module>
            <login-module code="org.gatein.sso.integration.SSODelegateLoginModule" flag="required">
               <module-option name="enabled">
                  <xsl:attribute name="value">#{gatein.sso.login.module.enabled}</xsl:attribute>
               </module-option>
               <module-option name="delegateClassName">
                  <xsl:attribute name="value">#{gatein.sso.login.module.class}</xsl:attribute>
               </module-option>
               <module-option name="portalContainerName" value="portal"/>
               <module-option name="realmName" value="gatein-domain"/>
               <module-option name="password-stacking" value="useFirstPass"/>
            </login-module>
            <login-module code="org.exoplatform.services.security.j2ee.JBossAS7LoginModule" flag="required">
               <module-option name="portalContainerName" value="portal"/>
               <module-option name="realmName" value="gatein-domain"/>
            </login-module>
         </authentication>
      </security-domain>   
   </xsl:template>
   
   <xsl:template name="security-domains-samples">
      <xsl:comment> Uncommented this when deploying gatein-sample-portal </xsl:comment>
      <xsl:comment>
                <![CDATA[<security-domain name="gatein-domain-sample-portal" cache-type="default">
                    <authentication>
                        <login-module code="org.gatein.security.oauth.jaas.OAuthLoginModule" flag="required">
                            <module-option name="portalContainerName" value="sample-portal"/>
                            <module-option name="realmName" value="gatein-domain-sample-portal"/>
                        </login-module>
                        <login-module code="org.gatein.sso.integration.SSODelegateLoginModule" flag="required">
                            <module-option name="enabled" value="#{gatein.sso.login.module.enabled}" />
                            <module-option name="delegateClassName" value="#{gatein.sso.login.module.class}" />
                            <module-option name="portalContainerName" value="sample-portal" />
                            <module-option name="realmName" value="gatein-domain-sample-portal" />
                            <module-option name="password-stacking" value="useFirstPass" />
                        </login-module>
                        <login-module code="org.exoplatform.services.security.j2ee.JBossAS7LoginModule" flag="required">
                            <module-option name="portalContainerName" value="sample-portal"/>
                            <module-option name="realmName" value="gatein-domain-sample-portal"/>
                        </login-module>
                    </authentication>
                </security-domain>]]>
                </xsl:comment>   
   </xsl:template>
   
   <xsl:template name="virtual-server-clustering">
      <xsl:if test="$config='clustering'">   
         <sso cache-container="web" cache-name="sso" reauthenticate="false"/>
      </xsl:if>
   </xsl:template>

   <xsl:template name="cache-container-web-start">
      <xsl:if test="$config='default'">
         <xsl:comment>Uncommented this if you want persistent HTTP sessions among server restarts (WARN: Performance penalty)</xsl:comment>
         <xsl:value-of select="'&#xa;&#09;&lt;!--'" disable-output-escaping="yes" />
      </xsl:if>
   </xsl:template>

   <xsl:template name="cache-container-web-end">
      <xsl:if test="$config='default'">
         <xsl:value-of select="'&#xa;&#09;-->'" disable-output-escaping="yes" />
      </xsl:if>
   </xsl:template>
   
   <!-- matching rules -->   

   <xsl:template match="node()[name(.)='extensions']">
      <xsl:copy>
         <xsl:apply-templates select="node()|@*"/>
         <xsl:call-template name="extensions"/>
      </xsl:copy>
      <xsl:call-template name="system-properties"/>
   </xsl:template>

   <xsl:template match="node()[name(.)='periodic-rotating-file-handler']">
      <xsl:copy>
         <xsl:apply-templates select="node()|@*"/>
      </xsl:copy>
      <xsl:call-template name="loggers"/>
   </xsl:template>

   <xsl:template match="node()[name(.)='drivers']">
      <xsl:call-template name="datasources"/>
      <xsl:call-template name="datasources-samples"/>
      <xsl:copy>
         <xsl:apply-templates select="node()|@*" />
      </xsl:copy>
   </xsl:template>

   <xsl:template match="node()[name(.)='deployment-scanner']">
      <xsl:copy>
         <xsl:call-template name="deployment-scanner"/>
         <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="node()[name(.)='profile']">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
         <xsl:call-template name="subsystem"/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="node()[name(.)='security-domains']">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" />
         <xsl:call-template name="security-domains"/>
         <xsl:call-template name="security-domains-samples"/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="node()[name(.)='virtual-server']">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" />
         <xsl:call-template name="virtual-server-clustering"/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="node()[name(.)='cache-container' and @name='web']">
      <xsl:call-template name="cache-container-web-start"/>
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
      <xsl:call-template name="cache-container-web-end"/>
   </xsl:template>
   
   <xsl:template match="@*|node()">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
   </xsl:template>

</xsl:stylesheet>
