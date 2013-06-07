# GateIn Engine

Implementation of the GateIn Engine for the GateIn 4.0.0 version.

# Instructions

1. Build the project
2. Run the standalone version `portal/web/target/portal.web-4.0.0-SNAPSHOT-war-exec.jar`
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

# Page Layout

The new page layout based on the In [Place Editing](https://community.jboss.org/wiki/InPlaceEditing) proposal has been a bit sketched. The existing container based UI structure is kept however it is used differently. Instead of having a container representing a page component, a container represents a visual zone as per defined in the spec.

Deployment descriptor has been adapted to this and the new 2.0 namespace provides the capabilty to name zones and windows with a name:

- the XML is simpler
- windows are named instead of having a generated UUID like before: so the new URLs are stables (now they were using UUID for scoping the window navigational state)


# Deployment descriptor

A simplified and improved navigation XML format has been introduced allowing to reduce the boiler plate and provide new features.

## Navigation 2.0

The XML for navigation looks like:

    <navigation
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.gatein.org/xml/ns/gatein_objects_2_0 http://www.gatein.org/xml/ns/gatein_objects_2_0"
        xmlns="http://www.gatein.org/xml/ns/gatein_objects_2_0">
      <node>
        <label xml:lang="en">Home</label>
        <label xml:lang="fr">Accueil</label>
        ...
        <page-reference>portal::classic::homepage</page-reference>
        <node>
          <name>demo</name>
          <label>Demo</label>
          <visibility>DISPLAYED</visibility>
          <page-reference>portal::classic::demo</page-reference>
        </node>
        ...
      </node>
    </navigation>

- the navigation now provides a *root* navigation page.

## Page 2.0


The XML for page looks like:

    <page>
      <name>homepage</name>
      <title>Home Page</title>
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

- the layout hierarchy is gone providing flat *zone* containers
- windows (portlet) uses a mandatory *name* element for identifying the window in the page (checked with an XSD unique constraint)

# Todo list (close scope)

* Try to change the `javax.portlet.` prefix to a reserved char among the *RFC3986_PCHAR*:
    * Need to distinguish the origin of the parameter and only consider parameters from the request and not HTTP post. To achieve this,
      Juzu needs to be modified to add the request parameter origin in the `juzu.request.RequestParameter` object.
    * The reserved char must be part of *RFC3986_PCHAR* chars for the *path segment* and *query string parameter name*
* Portlet eventing
* Group and user navigations
* Session scoped parameters
* Header support (http and html)
* Cookie support
