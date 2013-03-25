<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="java.util.Locale"%>
<%@ page import="java.util.ResourceBundle"%>
<%@ page trimDirectiveWhitespaces="true" %>
<portlet:defineObjects />

<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(renderRequest.locale)}"/>

<c:set var="urlContribute" value="${renderRequest.getPreferences().getValue('url.contribute', '/#')}" />
<c:set var="urlDocumentation" value="${renderRequest.getPreferences().getValue('url.docs', '/#')}" />
<c:set var="urlJira" value="${renderRequest.getPreferences().getValue('url.jira', '/#')}" />

<div id="id<portlet:namespace/>_gtnResponsiveCommunityPortlet" class="gtnResponsiveCommunityPortlet ">

    <div id="willing">
        <h4>${resourceBundle.getString("contribute.label")}</h4>
        <p>${resourceBundle.getString("contribute.content")}</p>
        <div>
            <a class="btn" href="${urlJira}">${resourceBundle.getString("contribute.jira.link")}</a>
            <span>${resourceBundle.getString("contribute.or")}</span>
            <a href="${urlContribute}">${resourceBundle.getString("contribute.link")}</a>
        </div>
    </div>

    <div id="conversation">
        <h4>${resourceBundle.getString("conversation.label")}</h4>
        <div id="conversation-accordion" class="accordion">
            <div class="accordion-group">
                <a href="#blog-content-mobile" class="accordion-toggle">
                    <i class="icon-blog icon-gray"></i>${resourceBundle.getString("conversation.blog.label")}
                </a>
                <div class="accordion-body collapse in" id="blog-content-mobile">
                    <ul>
                        <c:forEach var="blogArticle" items="${blogRSSBean.feedTitles}">
                            <h6>
                                <a href="${blogArticle.link}">${blogArticle.title}</a>
                            </h6>
                            <div class="info">
                                <span>
                                    <c:forEach var="author" items="${blogArticle.authors}">
                                        <a href="${blogRSSBean.authorUrlPrefix}${author.uri}">${author.name}</a>
                                    </c:forEach>
                                </span>

                                <span>
                                    <fmt:formatDate value="${blogArticle.publishedDate}" pattern="dd MMM" />
                                </span>
                            </div>
                        </c:forEach>
                    </ul>
                    <a href="${blogRSSBean.contentSource}">${resourceBundle.getString("conversation.blog.link")}</a>
                </div>
            </div>

            <div class="accordion-group">
                <a href="#tweets-content-mobile" class="accordion-toggle">
                    <i class="icon-twitter icon-gray"></i>${resourceBundle.getString("conversation.twitter.label")}
                </a>
                <div class="accordion-body collapse" id="tweets-content-mobile">
                    <ul>
                        <c:forEach var="twitterTweet" items="${twitterRSSBean.feedTitles}">
                            <li>
                                <p>
                                    <a href="${twitterTweet.link}">${twitterTweet.title}</a>
                                </p>
                                <div class="info">
                                    <span>
                                        <fmt:formatDate value="${twitterTweet.publishedDate}" pattern="dd MMM" />
                                    </span>
                                </div>
                            </li>
                        </c:forEach>
                    </ul>
                    <a href="${twitterRSSBean.contentSource}">${resourceBundle.getString("conversation.twitter.link")}</a>
                </div>
            </div>
        </div>
    </div>

    <div id="documentation">
        <i class="book"></i>
        <h4>${resourceBundle.getString("documentation.label")}</h4>
        <p>${resourceBundle.getString("documentation.content")} <a href="${urlDocumentation}"> ${resourceBundle.getString("documentation.link")}</a></p>
    </div>
</div>