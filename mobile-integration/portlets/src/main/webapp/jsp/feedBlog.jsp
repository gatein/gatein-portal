<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page trimDirectiveWhitespaces="true" %>
<portlet:defineObjects/>

<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(resourceRequest.locale)}"/>

<c:choose>
  <c:when test="${blogRSSBean.valid}">
    <ul>
      <c:forEach var="blogArticle" items="${blogRSSBean.feedTitles}">
        <li>
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
            <fmt:formatDate value="${blogArticle.publishedDate}" pattern="dd MMM"/>
          </span>
          </div>
        </li>
      </c:forEach>
    </ul>
    <a href="${blogRSSBean.contentSource}">${resourceBundle.getString("conversation.blog.link")}</a>
  </c:when>
  <c:otherwise>
    <div class="errorPane">
      <p>${resourceBundle.getString("conversation.io.error")}</p>
      <a href="${blogRSSBean.sourceIO}">${blogRSSBean.sourceIO}</a>
    </div>
  </c:otherwise>
</c:choose>