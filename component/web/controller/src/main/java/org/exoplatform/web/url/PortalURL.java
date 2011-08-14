/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.web.url;

import org.exoplatform.web.controller.QualifiedName;
import org.gatein.common.util.ParameterMap;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * An URL for a resource managed by the portal.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PortalURL<R, U extends PortalURL<R, U>>
{

   /** . */
   private static final ParameterMap.AccessMode ACCES_MODE = ParameterMap.AccessMode.get(false, false);

   /** . */
   private boolean schemeUse;

   /** . */
   private boolean authorityUse;

   /** . */
   protected Boolean ajax;

   /** . */
   protected String confirm;

   /** . */
   protected ParameterMap queryParams;

   /** . */
   protected MimeType mimeType;

   /** . */
   private Locale locale;

   /** . */
   private final URLContext context;

   /**
    * Create a portal URL instance.
    *
    * @param context the url context
    * @throws NullPointerException if the context is null
    */
   public PortalURL(URLContext context) throws NullPointerException
   {
      if (context == null)
      {
         throw new NullPointerException("No context");
      }

      //
      this.authorityUse = false;
      this.schemeUse = false;
      this.context = context;
      this.ajax = null;
      this.locale = null;
      this.confirm = null;
      this.queryParams = null;
      this.mimeType = null;
   }

   /**
    * Returns the current scheme use.
    *
    * @return the scheme use
    */
   public boolean getSchemeUse()
   {
      return schemeUse;
   }

   /**
    * Control the scheme generation (<code>http</code> or <code>https</code> according to the request) of the URL.
    * If the scheme is enabled, the authority property will be set to true.
    *
    * @param schemeUse the scheme use value
    */
   public void setSchemeUse(boolean schemeUse)
   {
      if (schemeUse)
      {
         this.authorityUse = true;
      }
      this.schemeUse = schemeUse;
   }

   /**
    * Returns the current authority use.
    *
    * @return the authority use
    */
   public boolean getAuthorityUse()
   {
      return authorityUse;
   }

   /**
    * Control the authority generation (the server name appended with the port when its value is different than 80) of the
    * URL. If the authority is disabled, the scheme property will be set to false.
    *
    * @param authorityUse the authority use value
    */
   public void setAuthorityUse(boolean authorityUse)
   {
      if (!authorityUse)
      {
         schemeUse = false;
      }
      this.authorityUse = authorityUse;
   }

   /**
    * Returns the ajax mode.
    *
    * @return the ajax mode
    */
   public final Boolean getAjax()
   {
      return ajax;
   }

   /**
    * Update the ajax mode.
    *
    * @param ajax the new ajax mode
    * @return this object
    */
   public final U setAjax(Boolean ajax)
   {
      this.ajax = ajax;
      return (U)this;
   }

   /**
    * Returns the confirm message.
    *
    * @return the confirm message
    */
   public final String getConfirm()
   {
      return confirm;
   }

   /**
    * Updates the confirm message.
    *
    * @param confirm the new confirm message
    * @return this object
    */
   public final U setConfirm(String confirm)
   {
      this.confirm = confirm;
      return (U)this;
   }

   /**
    * Returns the current resource associated with this URL.
    *
    * @return the resource
    */
   public abstract R getResource();

   /**
    * Set a new resource on this URL.
    *
    * @param resource the new resource
    * @return this object
    */
   public abstract U setResource(R resource);

   /**
    * Returns the set of parameter names provided this url.
    *
    * @return the parameter names
    */
   public abstract Set<QualifiedName> getParameterNames();

   /**
    * Returns a specified parameter value or null when it is not available.
    *
    * @param parameterName the parameter name
    * @return the parameter value
    */
   public abstract String getParameterValue(QualifiedName parameterName);

   /**
    * Returns the current mime type that this URL will be generated for, or null if none is set (which means
    * there is no guarantees about the mime type that will be used as target but it's likely to be {@link MimeType#XHTML}}).
    *
    * @return the current mime type
    */
   public final MimeType getMimeType()
   {
      return mimeType;
   }

   /**
    * Set the mime type on this URL. The mime type will be used when URL is generated to encode the URL for the specified
    * mime type.
    *
    * @param mimeType the new mime type
    */
   public void setMimeType(MimeType mimeType)
   {
      this.mimeType = mimeType;
   }

   public Locale getLocale()
   {
      return locale;
   }

   public void setLocale(Locale locale)
   {
      this.locale = locale;
   }

   /**
    * Returns the query parameters. Null can be returned.
    *
    * @return the query parameters
    */
   public Map<String, String[]> getQueryParameters()
   {
      return queryParams;
   }

   /**
    * Returns a query parameter value or null if it does not exist.
    *
    * @param parameterName the parameter name
    * @return the parameter value
    * @throws NullPointerException if the parameter name is null
    */
   public String getQueryParameterValue(String parameterName) throws NullPointerException
   {
      if (parameterName == null)
      {
         throw new NullPointerException("No null parameter name");
      }
      else if (queryParams == null)
      {
         return null;
      }
      else
      {
         String[] parameterValues = queryParams.get(parameterName);
         return parameterValues != null ? parameterValues[0] : null;
      }
   }

   /**
    * Update the parameter value when the parameterValue argument is not null or remove it when the parameterValue
    * argument is null.
    *
    * @param parameterName the parameter name
    * @param parameterValue the parameter value
    * @throws NullPointerException if the parameter value is null
    */
   public final void setQueryParameterValue(String parameterName, String parameterValue) throws NullPointerException
   {
      if (parameterName == null)
      {
         throw new NullPointerException("No null parameter name");
      }
      if (parameterValue == null)
      {
         if (queryParams != null)
         {
            queryParams.remove(parameterName);
         }
      }
      else
      {
         if (queryParams == null)
         {
            queryParams = new ParameterMap(ACCES_MODE);
         }
         queryParams.setValue(parameterName, parameterValue);
      }
   }

   /**
    * Returns a query parameter value or null if it does not exist.
    *
    * @param parameterName the parameter name
    * @return the parameter value
    * @throws NullPointerException if the parameter name is null
    */
   public final String[] getQueryParameterValues(String parameterName) throws NullPointerException
   {
      if (parameterName == null)
      {
         throw new NullPointerException("No null parameter name");
      }
      return queryParams != null ? queryParams.getValues(parameterName) : null;
   }

   /**
    * Update the parameter value when the parameterValues argument is not null or remove it when the parameterValues
    * argument is null.
    *
    * @param parameterName the parameter name
    * @param parameterValues the parameter value
    * @throws NullPointerException if the parameter value is null
    */
   public final void setQueryParameterValues(String parameterName, String[] parameterValues) throws NullPointerException
   {
      if (parameterName == null)
      {
         throw new NullPointerException("No null parameter name");
      }
      if (parameterValues == null)
      {
         if (queryParams != null)
         {
            queryParams.remove(parameterName);
         }
      }
      else
      {
         if (queryParams == null)
         {
            queryParams = new ParameterMap(ACCES_MODE);
         }
         queryParams.setValues(parameterName, parameterValues);
      }
   }

   /**
    * Generates the URL value.
    *
    * @return the URL value
    */
   public final String toString()
   {
      PortalURL _this = this;
      return context.render(_this);
   }
}
