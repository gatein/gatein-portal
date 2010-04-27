<%--
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ page contentType="text/html" %>
<%@ taglib uri="http://portals.apache.org/bridges/struts/tags-portlet-html" prefix="html" %>
    <TABLE background="<html:rewrite href="images/bkg-topbar.gif"/>" border=0 cellSpacing=0
           cellPadding=5 width="100%">
      <TR>
        <TD>
          <html:link href="shop/index.shtml">
            <html:img border="0" src="images/logo-topbar.gif"/>
          </html:link>
        </TD>
        <TD align=right>
          <html:link href="shop/viewCart.shtml">
            <html:img border="0" imageName="img_cart" src="images/cart.gif"/>
          </html:link>
          <html:img border="0" src="images/separator.gif"/>
          <html:link href="shop/signonForm.shtml">
            <html:img border="0" imageName="img_signin" src="images/sign-in.gif"/>
          </html:link>
          <html:img border="0" src="images/separator.gif"/>
          <html:link href="help.shtml">
            <html:img border="0" imageName="img_help" src="images/help.gif"/>
          </html:link>
        </TD>
        <TD align=left valign=bottom>
          <html:form method="post" action="/shop/searchProducts.shtml">
            <input name=keyword size=14>
            <html:image border="0" src="images/search.gif"/>
          </html:form>
        </TD>
      </TR>
    </TABLE>

    <TABLE border=0 cellSpacing=0 height="85%" width="100%">
      <TR>
        <TD vAlign=top>
          <!doctype html public "-//w3c//dtd html 4.0 transitional//en">
          <CENTER>
            <H1>JPetStore Demo</H1>
            <H3>By <a href="mailto:clinton.begin@ibatis.com">Clinton Begin</a>
            </H3>
          </CENTER>

          The JPetStore Demo is an online pet store. Like most e-stores, you
          can browse and search the product catalog, choose items to add to a
          shopping cart, amend the shopping cart, and order the items in the
          shopping cart. You can perform many of these actions without
          registering with or logging into the application. However, before
          you can order items you must log in (sign in) to the application. In
          order to sign in, you must have an account with the application,
          which is created when you register (sign up) with the application.

          <P><A href="#SigningUp">Signing Up</A>

          <BR>
          <A href="#SigningIn">Signing In</A>

          <BR>
          <A href="#Catalog">Working with the Product Catalog</A>

          <BR>&nbsp;&nbsp;&nbsp;
            <A href="#CatalogBrowsing">Browsing the Catalog</A>

          <BR>&nbsp;&nbsp;&nbsp;
            <A href="#CatalogSearching">Searching the Catalog</A>

          <BR>
          <A href="#ShoppingCart">Working with the Shopping Cart</A>

          <BR>&nbsp;&nbsp;&nbsp;
            <A href="#ShoppingCartAdd">Adding and Removing Items</A>

          <BR>&nbsp;&nbsp;&nbsp;
            <A href="#ShoppingCartUpdate">Updating the Quantity of an Item</A>

          <BR>&nbsp;&nbsp;&nbsp;
            <A href="#Ordering">Ordering Items</A>

          <BR>
          <A href="#OrderReview">Reviewing an Order</A>

          <BR>
          <A href="#Issues">Known Issues</A>

          <H2><A name=SigningUp></A>Signing Up</H2>
          To sign up, click the Sign-in link at the right end of the banner.
          Next, click the New User link in the resulting page. Among other
          information, the signup page requires you to provide a user
          identifier and password. This information is used to identify your
          account and must be provided when signing in.

          <H2><A name=SigningIn></A>Signing In</H2>
          You sign in to the application by clicking the Sign-in link at the
          right end of the banner, filling in the user identifier and
          password, and clicking the Submit button.

          <P>You will also be redirected to the signin page when you try to
          place an order and you have not signed in. Once you have signed in,
          you can return to your shopping session by clicking the shopping
          cart icon at the right end of the banner.

          <BR>&nbsp;

          <H2><A name=Catalog></A>Working with the Product Catalog</H2>
          This section describes how to browse and search the product catalog.

          <H4><A name=CatalogBrowsing></A>Browsing the Catalog</H4>
          The pet store catalog is organized hierarchically as follows:
          categories, products, items.

          <P>You list the pets in a category by clicking on the category name
          in the left column of the main page, or by clicking on the picture
          representing the category.

          <P>Once you select a category, the pet store will display a list of
          products within a category. Selecting a product displays a list of
          items and their prices. Selecting a product item displays a text and
          visual description of the item and the number of that item in stock.

          <H4><A name=CatalogSearching></A>Searching the Catalog</H4>
          You search for products by typing the product name in search field
          in the middle of the banner.

          <H2><A name=ShoppingCart></A>Working with the Shopping Cart</H2>

          <H4><A name=ShoppingCartAdd></A>Adding and Removing Items</H4>
          You add an item to your shopping cart by clicking the Add to Cart
          button to the right of an item. This action also displays your
          shopping cart.

          <P>You can remove the item by clicking the Remove button to the left
          of the item.

          <P>To continue shopping, you select a product category from the list
          under the banner.

          <H4><A name=ShoppingCartUpdate></A>Updating the Quantity of&nbsp; an
          Item</H4>
          You adjust the quantity of an item by typing the quantity in the
          item's Quantity field in the shopping cart and clicking the Update
          button.

          <P>If the quantity of items requested is greater than that in stock,
          the In Stock field in the shopping cart will show that the item is
          backordered.

          <H4><A name=Ordering></A>Ordering Items</H4>
          You order the items in the shopping cart by selecting the Proceed to
          Checkout button. The pet store will display a read-only list of the
          shopping cart contents. To proceed with the checkout, click the
          Continue button.

          <P>If you have not signed in, the application will display the
          signin page, where you will need to provide your account name and
          password. Otherwise, the application will display a page requesting
          payment and shipping information. When you have filled in the
          required information, you click the Submit button, and the
          application will display a read-only page containing your billing
          and shipping address.&nbsp; If you need to change any information,
          click your browser's Back button and enter the correct information.
          To complete the order, you click the Continue button.

          <H2><A name=OrderReview></A>Reviewing An Order</H2>
          The final screen contains your order information.

          <P>The application can be set up to send email confirmation of
          orders.&nbsp; This option can only be set when the application is
          deployed. See the installation instructions for further information.

          <H2><A name=Issues></A>Known Issues</H2>

          <ul>
            <li>A bit of a state problem with account modification and
                validation. If the account is modified (submitted), but fails
                validation, the state of the active account is modified in
                memory (not in database).  A separate "working copy" of the
                account form is needed.  This is a simple fix, but time is
                short right now, so it will be fixed in the next release.
          </ul>
        </TD>
      </TR>
      <TR>
        <TD vAlign=bottom></TD>
      </TR>
      <TR>
        <TD vAlign=bottom>
          <TABLE border=0 cellSpacing=0 width="100%"
            <TR>
              <TD align=middle>
                <FONT color=black size=+1>Implementation by
                  <a href="mailto:clinton.begin@ibatis.com">Clinton Begin</a>
                </FONT>
              </TD>
            </TR>
          </TABLE>
        </TD>
      </TR>
    </TABLE>

    <P>&nbsp;</P>

    <P align="center">
      <a href="http://www.ibatis.com">
        <html:img border="0" align="center" src="images/poweredby.gif"/>
      </a>
    </P>

