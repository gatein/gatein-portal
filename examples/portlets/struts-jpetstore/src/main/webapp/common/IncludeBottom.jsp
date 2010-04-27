</div>

<div id="Footer">


  <div id="Banner">
    <logic:present name="accountBean" scope="session">
      <logic:equal name="accountBean" property="authenticated" value="true">
        <logic:equal name="accountBean" property="account.bannerOption" value="true">
          <%-- bean:write filter="false" name="accountBean" property="account.bannerTag"/ --%>
          <html:img src="${accountBean.account.bannerSource}"/>
        </logic:equal>
      </logic:equal>
    </logic:present>
  </div>

</div>

</body>
</html>