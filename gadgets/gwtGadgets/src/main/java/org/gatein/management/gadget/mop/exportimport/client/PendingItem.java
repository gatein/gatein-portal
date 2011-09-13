/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.management.gadget.mop.exportimport.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import java.io.Serializable;

/**
 * {@code PendingItem}
 * <p>
 * Tree item representing a pending item (loading in progress)
 * </p>
 * Created on Dec 29, 2010, 1:25:04 PM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @version 1.0
 */
public class PendingItem extends TreeItem implements Serializable
{

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   /**
    * Create a new instance of {@code PendingItem}
    */
   public PendingItem()
   {
      super("Loading sub-tree...");
      this.setStyleName("gwt-TreeItem-pending");
   }

   /**
    * Constructs a tree item with the given HTML.
    *
    * @param html the item's HTML
    */
   public PendingItem(String html)
   {
      this();
   }

   /**
    * Constructs a tree item with the given HTML.
    *
    * @param html the item's HTML
    */
   public PendingItem(SafeHtml html)
   {
      super(html);
      this.setText("Loading sub-tree...");
   }

   /**
    * Constructs a tree item with the given <code>Widget</code>.
    *
    * @param widget the item's widget
    */
   public PendingItem(Widget widget)
   {
      super(widget);
      this.setText("Loading sub-tree...");
   }
}
