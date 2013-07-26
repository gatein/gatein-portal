<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page trimDirectiveWhitespaces="true" %>
<portlet:defineObjects/>

<c:set var="resourceBundle" value="${portletConfig.getResourceBundle(resourceRequest.locale)}"/>

<c:choose>
  <c:when test="${vimeoRSSBean.valid}">
    <ul>
      <c:forEach var="vimeoItem" items="${vimeoRSSBean.feedTitles}">
        <li>
          <h6><a href="${vimeoItem.link}">${vimeoItem.title}</a></h6>

          <div class="info">
            <span>
              <fmt:formatDate value="${vimeoItem.publishedDate}" pattern="dd MMM"/>
            </span>
          </div>
        </li>
      </c:forEach>
    </ul>
    <a href="${vimeoRSSBean.contentSource}">${resourceBundle.getString("conversation.vimeo.link")}</a>
  </c:when>
  <c:otherwise>
    <div class="errorPane">
      <p>${resourceBundle.getString("conversation.io.error")}</p>
      <a href="${vimeoRSSBean.sourceIO}">${vimeoRSSBean.sourceIO}</a>
    </div>
  </c:otherwise>
</c:choose>