<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<li class="menuelement">
  <c:choose>
    <c:when test="${node.URI != null}">
      <a href="${node.URI}">${node.name}</a>
    </c:when>
    <c:otherwise>
      <div class="menucategory">${node.name}
        <div class="menuarrow"></div>
      </div>
    </c:otherwise>
  </c:choose>	

  <c:if test="${node.children.size() > 0}">
    <ul class="submenu">
    <c:forEach var="child" items="${node.children}">
	  <c:set var="node" value="${child}" scope="request"/>
	  <jsp:include page="node.jsp"/> 
    </c:forEach>
    </ul>
  </c:if>
</li>
