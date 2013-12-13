# Roadmap

## Implemented

- Stateless navigation (browser back button, bookmarkability, load balancing & failover friendly, multitab navigation, reduce heap consumption
- URL maping public render parameter to simple parameters (provide manufacturable URLs)
- Zone based layouts
- Persistence agnostic (in memory persistence for testing, document oriented NoSQL)
- Content provider (asynchronous rendering, content as first class citizen of the platform)
- Markdown content provider (content provider example)
- Single war file deployment
- Reactive page (parallel and asynchronous rendering)
- AppZu live applications (simplify development and deployment)
- Arquillian Extension
- Typesafe config support

## Beta 1

- Simple public API (navigations)
- AppZu navigation portlet using public API
- Simple page creation
- JavaScript modules

## Unscheduled features

- Handle sites (for now a single site)
- Bigpipe rendering
- AppZu improvements
- Make all most GateIn apps as AppZu apps
- Complete public API
- Templates (page, site)
- Content registry
- Eventing (portlet events, portal events)
- Responsive support
- Gadget support
- WSRP support
- Expose MOP via webdav
- New "webapp" site type (provide transient descriptor based sites used as templates or for dev)
- Navigation links (associate a nav with a remote URL, a content, etc...)
- Navigation scoped layouts (associate a subtree with a specific layout)
- Theme support Less based


# Instructions

1. Build the project
2. Run it : `portal/packaging/target/portal.packaging-4.0.0-SNAPSHOT-war-exec.jar` or `portal/packaging/target/portal.packaging-4.0.0-SNAPSHOT-tomcat-distrib.tar.gz`
3. Go on localhost

# Page controller

## State design

The page controller provides default support for stateless navigation, it means that the entire navigatonal state of a page
 is maintained in URL. This approach differs from the previous implementations that were maintaining this state (window parameters,
 portlet mode, public render parameters, etc...) in the session of the portal. This brings the following benefits:

* Bookmarkability of the page: the engine provides links that will restore the state of the page.
* Browser back button support
* Multitab navigation: an user can open several tab on the same page without creating conflicts between applications based
  on render parameters stored in portal session
* Replication of navigational parameters on a cluster

However there is a slight drawback, when user navigates to another page, the parameters are lost as they are only maintained
on the same page. We will introduce *window session scoped state* to declare that a window will have its state stored in the
portal session, this should be configured in the deployment descriptor of the application.

## Reactive pages

Page rendering has been improved and provides a new implementation for the rendering based on the Servlet 3.0 features: the new rendering is now parallelized
instead of being sequential. This provides noticeable improvements when portlets have a long rendering time and the page rendering time can be improved in such
situations.

## URL encoding

### Goals

* Provide stateless support for render parameters
* Respect the related specification RFC3986
* Providing human readable / manufacturable URLs
* Leverage public render parameters

### URL encoding design

The controller uses the following:

1. the query string can encode some chars using the native char or using the percent encoding. This is leveraged to remove ambiguity when encoding the parametrs of a portlet, in our case the the used chars are `:` and `,`
2. controller encodes special parameters using the `javax.portlet.` prefix that a portlet cannot use per the spec. Note that this could be changed to a shorter value using a reserved char prefix such as `:` (so a parameter named `:foo` would be encoded as `%3Afoo`). Let's call this value *prefix*.

Encoding a parameter map in single value follows this process

1. Encode the parameter map (it can be done in JavaScript using jQuery `serialize`): `{a=b,c=[d,e]}` -> `a=b&c=d&c=e`
2. Substitute `=` by `:` and `&` by `,`: `a=b&c=d&c=e` -> `a:b,c:d,c:e`

This encodes the parameters in a way it can be used in a single query tring parameter and can be decoded safely.

### Render URLs

1. Window parameters: `${prefix}p.${name}=${parameters}` 
2. Window state: `${prefix}w.${name}=${window_state}`
3. Portlet mode: `${prefix}m.${name}=${portlet_mode}`
4. Public render parameters: as regular parameters

Where name is the unique name of the portlet on the page.

This design provides default support for public render parameters, it means that a portlet on a page declaring a public render parameter can received the regular query string parameters: if a portlet binds to the `foo` parameter, the URL `/mypage?foo=bar` will address this parameter.


### Action URLs

1. Action type: `${prefix}a=action`
2. Action target: `${prefix}t=${name}`
3. Action parameters: as regular parameters
4. Action window state: `${prefix}w=${window_state}`
5. Action portlet mode: `${prefix}m=${portlet_mode}`
6. Window parameters: `${prefix}p.${name}=${parameters}` 
7. Window state: `${prefix}w.${name}=${window_state}`
8. Portlet mode: `${prefix}m.${name}=${portlet_mode}`
9. Public parameters: `${prefix}p=${parameters}`

### Resource URLs

1. Action type: `${prefix}a=resource`
2. Resource target: `${prefix}t=${name}`
3. Resource parameters: as regular parameters
4. Resource id: `${prefix}r=${id}`
5. Window parameters: `${prefix}p.${name}=${parameters}` 
6. Window state: `${prefix}w.${name}=${window_state}`
7. Portlet mode: `${prefix}m.${name}=${portlet_mode}`
8. Public parameters: `${prefix}p=${parameters}`

Note that some parameters are omitted according to the resource URL cache level:

1. PAGE cache level : all parameters are present
2. PORTLET cache level : only the target window parameter is present
3. FULL cache level : only type (1), target (2), resource parameters (3) and resource id (4)

# Page composition

The new page composition based on the In [Place Editing](https://community.jboss.org/wiki/InPlaceEditing) proposal has been a bit sketched. The existing container based UI structure is kept however it is used differently. Instead of having a container representing a page component, a container represents a visual zone as per defined in the spec.

Deployment descriptor has been adapted to this and the new 2.0 namespace provides the capability to name zones and windows with a name:

- the XML is simpler
- windows are named instead of having a generated UUID like before: so the new URLs are stables (now they were using UUID for scoping the window navigational state)

## Templating

This section defines how templating occurs for producing a *web page*.

### Nested templates

GateIn 3.x introduced the notion of nested templates to provide elements related to the hierarchical entities of a page:

* The server : **shared layout**
* The site : **site layout**
* The page : **page layout**

Those concepts are conserved in GateIn 4 since they proved to be simple and powerful.

## Layout engine

GateIn 4 defines a layout engine that is a simple decoupling between the controller and the underlying templating technology. The layout engine defines the contract for rendering a page in GateIn.

At runtime GateIn does not make any assumption about the template itself, the goal here is to provide as much as freedom in the templates as we recognize that the base GateIn templates may not cover all the possible use cases.

The layout rendering is based on the *Edit In Place* specification known as *zone based layout*. Each layout is clearly identified by the original spec and the underlying physical template should conform to it.

To provide this degree of independance, the physical layout for a specific layout can be configured.

### Layout blending

When GateIn renders a page, it will assemble the three layouts to produce the final page.

### Layout zones

The *Edit In Place* specification defines a naming for the various zones of a template which are natural numbers from **1 to n**.

Extra zones are used and defined for providing more flexibility:

* **header** zone : rendered by default in the site layout
* **footer** zone : rendered by default in the site layout
* etc…

Keep in mind that the structural pages (whether it is the page layout, the site layour or the shared layout) have no restriction upon the zones to be used. So normally a page layout may contain windows scoped in the header and a site layout may contain windows for the zone *1*.

As for now we have not yet defined how the windows from different layout can be assembled in a page, or even interacted with. We have just identified that this can be something powerful to use that provides extra flexiblity.

## Grid composition

todo.

# Deployment descriptor

A simplified and improved navigation XML format has been introduced with the following goals:

- reduce  boiler plate
- harmonize XML, i.e use the same term everywhere and make consistent
    - replace `title` and `label` by `display-name`
    - replace `page-reference` by `page-ref` to be similar to `application-ref` and `portlet-ref`
    - etc...
- deliver new features

## Navigation 2.0

The XML for navigation looks like:

    <navigation
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.gatein.org/xml/ns/gatein_objects_2_0 http://www.gatein.org/xml/ns/gatein_objects_2_0"
        xmlns="http://www.gatein.org/xml/ns/gatein_objects_2_0">
      <node>
        <display-name xml:lang="en">Home</display-name>
        <display-name xml:lang="fr">Accueil</display-name>
        ...
        <page-ref>portal::classic::homepage</page-ref>
        <node>
          <name>demo</name>
          <label>Demo</label>
          <visibility>DISPLAYED</visibility>
          <page-ref>portal::classic::demo</page-ref>
        </node>
        ...
      </node>
    </navigation>

- `label` is replaced by `display-name`
- the navigation now provides a *root* navigation page.

## Page 2.0


The XML for page looks like:

    <page>
      <name>homepage</name>
      <display-name>Home Page</display-name>
      <access-permission>Everyone</access-permission>
      <edit-permission>*:/platform/administrators</edit-permission>
      <zone>
        <id>1</id>
        <portlet>
          <name>home</name>
          <access-permission>Everyone</access-permission>
          <application-ref>portal</application-ref>
          <portlet-ref>HomePortlet</portlet-ref>
        </portlet>
      </zone>
    </page>

- `title` is replaced by `display-name`
- the layout hierarchy is gone providing flat *zone* containers
- windows (portlet) uses a mandatory *name* element for identifying the window in the page (checked with an XSD unique constraint)

## Site 2.0

The XML for site looks like

    <site
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.gatein.org/xml/ns/gatein_objects_2_0 http://www.gatein.org/xml/ns/gatein_objects_2_0"
        xmlns="http://www.gatein.org/xml/ns/gatein_objects_2_0">
      <name>classic</name>
      <display-name>Classic</display-name>
      <description>GateIn default portal</description>
      <access-permission>Everyone</access-permission>
      <edit-permission>*:/platform/administrators</edit-permission>
      <locale>en</locale>
      <properties>
        <entry key="sessionAlive">onDemand</entry>
        <entry key="showPortletInfo">0</entry>
      </properties>
      <zone>
        <id>header</id>
        <portlet>
          <name>navigation</name>
          <access-permission>Everyone</access-permission>
          <application-ref>portal</application-ref>
          <portlet-ref>NavigationPortlet</portlet-ref>
        </portlet>
      </zone>
    </site>

- `label` is replaced by `display-name`
- new zone based layout

# Model Object for Portal (MOP)

## Storage

Since GateIn 4.x provides multiple storage implementations for persisting the MOP. In addition of the Java Content Repository
store (developed in the GateIn 3.x series) we do provide additional storage

### RAM Persistence

RAM persistence was developed for testing purposes of GateIn with several goals:

* zero configuration
* transient
* very fast

This store does not write any single data on the disk that helps to achieve some of the goals above.

### MongoDB Persistence

Documented oriented databases are a good fit for the MOP storage.

#### Usage

* The Mongo client (GAV org.mongodb:mongo-java-driver) must be in the classpath of the `portal.war` archive (`/lib`
of Tomcat does the job perfectly)
* Run GateIn with the *mongo* profile

For example:

    cp mongo-java-driver-2.11.1.jar apache-tomcat-7.0.40/lib/
    cp commons-compress-1.3.jar apache-tomcat-7.0.40/lib/
    cp guava-11.0.1.jar apache-tomcat-7.0.40/lib/
    cd apache-tomcat-7.0.40/bin
    export CATALINA_OPTS=-Dexo.profiles=mongo
    ./catalina.sh run

#### Configuration

Mongo host and ports can be configured:

* `gatein.mop.mongo.host` property for the host
* `gatein.mop.mongo.port` property for the port

# Login module

## Done

* Classic login/logout (use user/password of portal database)
* OAuth: Enable user login with Facebook, twitter and google
* SSO: only tested with CAS, JOSSO and OpenAM server


# Configuration

## Property based configuration

Since GateIn 4, we integrated [Typesafe Config](https://github.com/typesafehub/config) library and follow its standard behavior
to organize portal and services configuration. The `GateInConfigurator` component service is currently taking care of the
integration, load config and propagates its content to the `PropertyManager`.

By default, it loads the following in precedence:
- `system properties`
- `application.{conf, json, properties}` (all resources on classpath with this name)
- `reference.conf` (all resources on classpath with this name)

The idea is that libraries and services should ship with a `reference.conf` (default configuration) in their jar. Portal
application provides a default `application.conf` (not yet implemented), or you can configure to load your own configuration
instead.

Since GateIn 4 relies on Typesafe Config, you can learn more about the configuration mechanism in
the [documentation](https://github.com/typesafehub/config#standard-behavior).

## Tomcat Jaas configuration

Tomcat authentication can be [configured via JAAS](http://tomcat.apache.org/tomcat-7.0-doc/realm-howto.html#JAASRealm). However
this configuration requires a JVM property to be set for using the correct configuration file.

To work around this, GateIn 4 autoconfigures a JVM property to a default JAAS configuration:
- when the system property `java.security.auth.login.config` is not set a default file is autoconfigured
    - from `conf/jaas.conf` of the server
    - otherwise from the `conf/jaas.conf` from the classpath (that is provided by default)
- when the system property is set then this value is used without any extra work

# Todo

## General todo

* Try to change the `javax.portlet.` prefix to a reserved char among the *RFC3986_PCHAR*:
    * Need to distinguish the origin of the parameter and only consider parameters from the request and not HTTP post. To achieve this,
      Juzu needs to be modified to add the request parameter origin in the `juzu.request.RequestParameter` object.
    * The reserved char must be part of *RFC3986_PCHAR* chars for the *path segment* and *query string parameter name*
* Portlet eventing
* Group and user navigations
* Session scoped parameters
* Http Header support
* Cookie support

## Config todo

* study config [merge](https://github.com/typesafehub/config#merging-config-trees)

## Security todo

* Now, we hard-code login and logout link: should get these link programmaticaly
* How to build initURL when login and logout (the page should redirect to when login and logout success)
* Should use resource bundle in all of text (now is hard-code)
* In login module: `requestContext.getSecurityContext().getRemoteUser()` alway return null when user logged in or not
* In login module: have `ServletAccessValve` class, this class need in jar in lib folder of tomcat. But with current packaging, we not put any jar into lib folder.
  I put a new Filter `OAuthPreFilter` to do as `ServletAccessValve` class.
* When config SSO with JOSSO, have a config refer to configuration.properties file:
  `gatein.sso.josso.properties.file=file:${jboss.home.dir}/standalone/configuration/gatein/configuration.properties`
  Should not use this config
* In SSO: have some auth callback to gatein (for verify username/password with user/password at gatein database)
  This call back work base REST. But now can not port REST.war from 3.5 to gatein 4. Because it does not work (In REST context can not to get PortalContainer because it's loaded by other classloader).
  So now, when config sso call back with HTTP POST method will not work
* Need to test SSO with: SPNEGO, SAML2 and Cluster SSO.
