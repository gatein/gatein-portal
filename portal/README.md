# GateIn Engine

Implementation of the GateIn Engine for the GateIn 4.0.0 version.

# Page controller

## State design

The page controller provides default support for stateless navigation, it means that the entire navigatonal state of a page
 is maintained in URL. This approach differs from the previous implementations that were maintaining this state (window parameters,
 portlet mode, public render parameters, etc...) in the session of the portal. This brings the following benefits:

* Bookmarkability of the page: the engine provides links that will restore the state of the page.
* Browser back button support
* Harmless multitab navigation: an user can open several tab on the same page without creating conflicts between applications based
  on render parameters

However there is a slight drawback, when user navigates to another page, the parameters are lost as they are only maintained
on the same page. We will introduce *window session scoped state* to declare that a window will have its state stored in the
portal session, this should be configured in the deployment descriptor of the application.

## URL encoding

To achieve the stateless design the page controller encodes the state of the page in URLs aiming at:

* Respect the related specification RFC3986
* Providing human readable / manufacturable URLs

## Todo list

* Try to change the `javax.portlet.` prefix to a reserved char among the *RFC3986_PCHAR*:
    * Need to distinguish the origin of the parameter and only consider parameters from the request and not HTTP post. To achieve this,
      Juzu needs to be modified to add the request parameter origin in the `juzu.request.RequestParameter` object.
    * The reserved char must be part of *RFC3986_PCHAR* chars for the *path segment* and *query string parameter name*
* Public render parameters
* Portlet eventing
* Resource serving
* Group and user navigations
* Session scoped parameters
* Header support (http and html)
* Cookie support

