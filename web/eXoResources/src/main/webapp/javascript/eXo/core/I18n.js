/**
 * Copyright (C) 2009 eXo Platform SAS.
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

eXo.core.I18n = {
  
  /**
   * initialize some information as language, orientation, etc of I18n object                       $
   */
  init : function() {
    var html = document.getElementsByTagName('html')[0];
    var lang = html.getAttribute('xml:lang') || html.getAttribute('lang') || "en";
    var dir = html.getAttribute('dir') || "lt";
    this.lang = lang;
    this.dir = dir;
    this.orientation = "rtl" == dir ? "rt" : "lt";
    this.lt = this.orientation == "lt";
  },
  
  /**
   * return language
   */
  getLanguage : function() {
    return this.lang;
  },
  
  /**
   * return orientation (right to left, left to right), 
   * some languages (such as Arabic) used "right to left" view
   */
  getOrientation : function() {
    return this.orientation;
  },
  
  /**
   * return directory
   */
  getDir : function() {
    return !this.lt;
  },
  
  /**
   * return "left to"(lt) state
   */
  isLT : function() {
    return this.lt;
  },
  
  /**
   * return "right to" state
   */
  isRT : function() {
    return !this.lt;
  }
};

eXo.core.I18n.init();
