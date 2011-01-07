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

UICalendar = function(calendarId) {
	this.calendarId = calendarId ;
  this.dateField = null ;
  this.datePattern = null;
  this.value = null;
  this.currentDate = null ; 	// Datetime value base of selectedDate for displaying calendar below
  														// if selectedDate is invalid, currentDate deals with system time;
  this.selectedDate = null ; //Datetime value of input date&time field
  this.months ;
}

UICalendar.prototype.init = function(field, isDisplayTime, datePattern, value, monthNames) {
	this.isDisplayTime = isDisplayTime ;
	
	if (this.dateField) {
		this.dateField.parentNode.style.position = '' ;
	}
	this.dateField = field ;
	this.datePattern = datePattern;
	this.value = value;
	
	this.months = new Array();
	this.months = monthNames.split(',');
	this.months.pop();
	
	if (!document.getElementById(this.calendarId)) this.create();
  this.show() ;

	// fix bug for IE 6
  var cld = document.getElementById(this.calendarId);
  if(eXo.core.Browser.isIE6())  {
    var blockClnd = document.getElementById('BlockCaledar') ;
    var iframe = document.getElementById(this.calendarId + 'IFrame') ;
    iframe.style.height = blockClnd.offsetHeight + "px";
  }
  field.parentNode.insertBefore(cld, field) ;
}

UICalendar.prototype.create = function() {
	var clndr = document.createElement("div") ;
	clndr.id = this.calendarId ;
	clndr.style.position = "absolute";
  if (eXo.core.Browser.isIE6()) {
		clndr.innerHTML = "<div class='UICalendarComponent'><iframe id='" + this.calendarId + "IFrame' frameBorder='0' style='position:absolute;height:100%;' scrolling='no'></iframe><div style='position:absolute;'></div></div>" ;
	} else {
		clndr.innerHTML = "<div class='UICalendarComponent'><div style='position: absolute; width: 100%;'></div></div>" ;
	}
	document.body.appendChild(clndr) ;
}

UICalendar.prototype.show = function() {
	document.onclick = new Function('eXo.webui.UICalendar.hide()') ;
	var re = /^(\d{1,2}\/\d{1,2}\/\d{1,4})\s*(\s+\d{1,2}:\d{1,2}:\d{1,2})?$/i ;
  this.selectedDate = new Date() ;


//	if (re.test(this.dateField.value)) {
//	  var dateParts = this.dateField.value.split(" ") ;
//	  var arr = dateParts[0].split("/") ;
//	  this.selectedDate.setMonth(parseInt(arr[0],10) - 1) ;
//	  this.selectedDate.setDate(parseInt(arr[1],10)) ;
//	  this.selectedDate.setFullYear(parseInt(arr[2],10)) ;
//	  if (dateParts.length > 1 && dateParts[dateParts.length - 1] != "") {
//	  	arr = dateParts[dateParts.length - 1].split(":") ;
//	  	this.selectedDate.setHours(arr[0], 10) ;
//	  	this.selectedDate.setMinutes(arr[1], 10) ;
//	  	this.selectedDate.setSeconds(arr[2], 10) ;
//	  }
//	}
	if (this.dateField.value != '') {
		// TODO: tamnd - set selected date to calendar
		var dateFieldValue = this.dateField.value;
		
		var dateIndex = this.datePattern.indexOf("dd");
		var dateValue = parseInt(dateFieldValue.substring(dateIndex,dateIndex+2),10);
		
		var monthIndex = this.datePattern.indexOf("MM");
		var monthValue = parseInt(dateFieldValue.substring(monthIndex,monthIndex+2) - 1,10);
		
		var yearIndex = this.datePattern.indexOf("yyyy");
		var yearValue = parseInt(dateFieldValue.substring(yearIndex,yearIndex+4),10);
		
		var hourIndex = this.datePattern.indexOf("HH");
		var hoursValue = parseInt(dateFieldValue.substring(hourIndex,hourIndex+2),10);
		
		var minuteIndex = this.datePattern.indexOf("mm");
		var minutesValue = parseInt(dateFieldValue.substring(minuteIndex,minuteIndex+2),10);
		
		var secondIndex = this.datePattern.indexOf("ss");
		var secondValue = parseInt(dateFieldValue.substring(secondIndex,secondIndex+2),10);
		
		if(isNaN(secondValue)) { secondValue = "00"; }
		if(isNaN(minutesValue)) { minutesValue = "00"; }
		if(isNaN(hoursValue)) { hoursValue = "00"; }

		var testDate = "MM/dd/yyyy HH:mm:ss";
		testDate = testDate.replace("dd",dateValue);
		testDate = testDate.replace("MM",monthValue+1);
		testDate = testDate.replace("yyyy",yearValue);
		testDate = testDate.replace("HH",hoursValue);
		testDate = testDate.replace("mm",minutesValue);
		testDate = testDate.replace("ss",secondValue);
		
		if (re.test(testDate)) {
			this.selectedDate.setDate(dateValue) ; 
			this.selectedDate.setMonth(monthValue) ;
			this.selectedDate.setFullYear(yearValue) ;
			this.selectedDate.setHours(hoursValue) ;
			this.selectedDate.setMinutes(minutesValue) ;
			this.selectedDate.setSeconds(secondValue) ;
		}
		
	}
	this.currentDate = new Date(this.selectedDate.valueOf()) ;
  var clndr = document.getElementById(this.calendarId) ;
  clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
//  var x = 0 ;
  var y = this.dateField.offsetHeight ;
  var beforeShow = eXo.core.Browser.getBrowserHeight();
  with (clndr.firstChild.style) {
  	display = 'block' ;
//	  left = x + "px" ;
	  top = y + "px" ;
	  if(eXo.core.I18n.isLT()) left = "0px";
	  else right = "0px";
  }
  var posCal = eXo.core.Browser.findPosY(this.dateField) - y;
  var heightCal = document.getElementById('BlockCaledar');
  var afterShow = posCal+heightCal.offsetHeight;
  if(afterShow > beforeShow)	 {
    clndr.firstChild.style.top = -heightCal.offsetHeight + 'px';
  }
	
  eXo.webui.UICalendar.initDragDrop();
  
  var drag = document.getElementById("BlockCaledar");
  var calendar = eXo.core.DOMUtil.findFirstChildByClass(drag, "div", "UICalendar");		
  var primary = eXo.core.DOMUtil.findAncestorById(this.dateField, "UIECMSearch");
  if (primary && eXo.core.Browser.isFF()) {
	calendar = clndr.firstChild;
	calendar.style.top = "0px";
	calendar.style.left = this.dateField.offsetLeft - this.dateField.offsetWidth - 32 + "px";
  }
}

UICalendar.prototype.onTabOut =function(event) {
  var keyCode = event.keyCode;
  //identify the tab key:
  if (keyCode == 9) {
    eXo.webui.UICalendar.hide();
  }
};

UICalendar.prototype.hide = function() {
  if (this.dateField) {
    document.getElementById(this.calendarId).firstChild.style.display = 'none' ;
//		this.dateField.parentNode.style.position = '' ;
    this.dateField.blur();
    this.dateField = null ;
  }
  document.onclick = null ;
  //document.onmousedown = null;
}

/* TODO: Move HTML code to a javascript template file (.jstmpl) */
UICalendar.prototype.renderCalendar = function() {
  var dayOfMonth = 1 ;
  var validDay = 0 ;
  var startDayOfWeek = this.getDayOfWeek(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, dayOfMonth) ;
  var daysInMonth = this.getDaysInMonth(this.currentDate.getFullYear(), this.currentDate.getMonth()) ;
  var clazz = null;
	var table = '<div id="BlockCaledar" class="BlockCalendar" onclick="event.cancelBubble = true">' ;
	table += 		'<div class="UICalendar" onmousedown="event.cancelBubble = true">' ;
	table += 		'	<table class="MonthYearBox">' ;
	table += 		'	  <tr>' ;
	table += 		'			<td class="MonthButton"><a class="PreviousMonth" href="javascript:eXo.webui.UICalendar.changeMonth(-1);" title="Previous Month"></a></td>' ;
	table += 		'			<td class="YearButton"><a class="PreviousYear" href="javascript:eXo.webui.UICalendar.changeYear(-1);" title="Previous Year"></a></td>' ;
	table += 		'			<td><font color="#f89302">' + this.months[this.currentDate.getMonth()] + '</font> - ' + this.currentDate.getFullYear() + '</td>' ;
	table += 		'			<td class="YearButton"><a class="NextYear" href="javascript:eXo.webui.UICalendar.changeYear(1);" title="Next Year"></a></td>' ;
	table += 		'			<td class="MonthButton"><a class="NextMonth" href="javascript:eXo.webui.UICalendar.changeMonth(1);" title="Next Month"></a></td>' ;
	table += 		'		</tr>' ;
	table += 		'	</table>' ;
	table += 		'	<div style="margin-top: 6px;padding: 0px 5px;">' ;
	table += 		'		<table>' ;
	table += 		'			<tr>' ;
	table += 		'				<td><font color="red">S</font></td><td>M</td><td>T</td><td>W</td><td>T</td><td>F</td><td>S</td>' ;
	table += 		'			</tr>' ;
	table += 		'		</table>' ;
	table += 		'	</div>' ;
	table += 		'	<div class="CalendarGrid">' ;
	table += 		'	<table>' ;
  for (var week=0; week < 6; week++) {
    table += "<tr>";
    for (var dayOfWeek=0; dayOfWeek < 7; dayOfWeek++) {
      if (week == 0 && startDayOfWeek == dayOfWeek) {
        validDay = 1;
      } else if (validDay == 1 && dayOfMonth > daysInMonth) {
        validDay = 0;
      }
      if (validDay) {
        if (dayOfMonth == this.selectedDate.getDate() && this.currentDate.getFullYear() == this.selectedDate.getFullYear() && this.currentDate.getMonth() == this.selectedDate.getMonth()) {
          clazz = 'Current';
        } else if (dayOfWeek == 0 || dayOfWeek == 6) {
          clazz = 'Weekend';
        } else {
          clazz = 'Weekday';
        }

        table = table + "<td><a class='"+clazz+"' href=\"javascript:eXo.webui.UICalendar.setDate("+this.currentDate.getFullYear()+","+(this.currentDate.getMonth() + 1)+","+dayOfMonth+")\">"+dayOfMonth+"</a></td>" ;
        dayOfMonth++ ;
      } else {
        table = table + "<td class='empty'><div>&nbsp;</div></td>" ;
      }
    }
    table += "</tr>" ;
  }		
	table += 		'		</table>' ;
	table += 		'	</div>' ;
	if (this.isDisplayTime) {
		table += 		'	<div class="CalendarTimeBox">' ;
		table += 		'		<div class="CalendarTimeBoxR">' ;
		table += 		'			<div class="CalendarTimeBoxM"><span><input class="InputTime" size="2" maxlength="2" value="' +
								((this.currentDate.getHours())>9 ? this.currentDate.getHours() : "0"+this.currentDate.getHours()) + 
								'" onkeyup="eXo.webui.UICalendar.setHour(this)" >:<input size="2" class="InputTime" maxlength="2" value="' + 
								((this.currentDate.getMinutes())>9 ? this.currentDate.getMinutes() : "0"+this.currentDate.getMinutes()) + 
								'" onkeyup = "eXo.webui.UICalendar.setMinus(this)">:<input size="2" class="InputTime" maxlength="2" value="' + 
								((this.currentDate.getSeconds())>9 ? this.currentDate.getSeconds() : "0"+this.currentDate.getSeconds()) + 
								'" onkeyup = "eXo.webui.UICalendar.setSeconds(this)"></span></div>' ;
		table += 		'		</div>' ;
		table += 		'	</div>' ;
	}
	table += 		'</div>' ;
	table += 		'</div>' ;
	return table ;
}

UICalendar.prototype.changeMonth = function(change) {
	this.currentDate.setDate(1);
	this.currentDate.setMonth(this.currentDate.getMonth() + change) ;
  var clndr = document.getElementById(this.calendarId) ;
  clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
  
  eXo.webui.UICalendar.initDragDrop();
}

UICalendar.prototype.initDragDrop = function() {
	  var drag = document.getElementById("BlockCaledar");
	  var component =  eXo.core.DOMUtil.findAncestorByClass(drag, "UICalendarComponent");
	  var calendar = eXo.core.DOMUtil.findFirstChildByClass(drag, "div", "UICalendar");
	  var innerWidth = drag.offsetWidth;
	  
	  eXo.core.DragDrop2.init(drag, component);	  
	  component.onDragStart = function() {
		  if(eXo.core.Browser.isIE7()) drag.style.height = calendar.offsetHeight + "px";
		  drag.style.width = innerWidth + "px";
	  }
	  
//	  var calendar = eXo.core.DOMUtil.findFirstChildByClass(drag, "div", "UICalendar");
//	  var innerWidth = drag.offsetWidth;
//	  drag.onmousedown = function(evt) {
//		  var event = evt || window.event;
//		  event.cancelBubble = true;
//		  drag.style.position = "absolute";
//		  if(eXo.core.Browser.isIE7()) drag.style.height = calendar.offsetHeight + "px";
//		  drag.style.width = innerWidth + "px";
//		  eXo.core.DragDrop2.init(drag, component);		  
//	  }
}

UICalendar.prototype.changeYear = function(change) {
  this.currentDate.setFullYear(this.currentDate.getFullYear() + change) ;
  this.currentDay = 0 ;
  var clndr = document.getElementById(this.calendarId) ;
  clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
  
  eXo.webui.UICalendar.initDragDrop();
}

UICalendar.prototype.setDate = function(year, month, day) {	
  if (this.dateField) {
    if (month < 10) month = "0" + month ;
    if (day < 10) day = "0" + day ;
    var dateString = this.datePattern ;
    dateString = dateString.replace("dd",day);
    dateString = dateString.replace("MM",month);
    dateString = dateString.replace("yyyy",year);

    this.currentHours = this.currentDate.getHours() ;
    this.currentMinutes = this.currentDate.getMinutes() ;
    this.currentSeconds = this.currentDate.getSeconds() ;
    if(this.isDisplayTime) {
    	if(typeof(this.currentHours) != "string") hour = this.currentHours.toString() ;
			if(typeof(this.currentMinutes) != "string") minute = this.currentMinutes.toString() ;
			if(typeof(this.currentSeconds) != "year") second = this.currentSeconds.toString() ;
			
			while(hour.length < 2) { hour = "0" + hour ; }
			while(minute.length < 2) { minute = "0" + minute ; }
			while(second.length < 2) { second = "0" + second ; }
	
    	dateString = dateString.replace("HH",hour);
    	dateString = dateString.replace("mm",minute);
    	dateString = dateString.replace("ss",second);
    }
    this.dateField.value = dateString ;
    this.hide() ;
  }
  return ;
}

UICalendar.prototype.getDateTimeString = function() {
	if(!this.currentDate) return this.datePattern;
	var year = "" + this.currentDate.getFullYear();
	var month = "" + (this.currentDate.getMonth() + 1);
	if(month.length < 2) month = "0" + month;
	var day = "" + this.currentDate.getDate();
	if(day.length < 2) day = "0" + day;
	var hour = "" + this.currentDate.getHours();
	if(hour.length < 2) hour = "0" + hour;
	var minute = "" + this.currentDate.getMinutes();
	if(minute.length < 2) minute = "0" + minute;
	var second = "" + this.currentDate.getSeconds();
	if(second.length < 2) second = "0" + second;
	
	var dateString = this.datePattern.trim();
	if(!this.isDisplayTime) {
		var ptStrings = dateString.split(" ");
		for(var i=0; i<ptStrings.length; i++) {
			if(ptStrings[i].indexOf("yyyy") >= 0) {
				dateString = ptStrings[i];
				break;
			}
		}
	}
	
	dateString = dateString.replace("dd",day);
  dateString = dateString.replace("MM",month);
  dateString = dateString.replace("yyyy",year);
  if(this.isDisplayTime) {
  	dateString = dateString.replace("HH",hour);
  	dateString = dateString.replace("mm",minute);
  	dateString = dateString.replace("ss",second);
  }
  
  return dateString;
}

UICalendar.prototype.setSeconds = function(object) {
	if(this.dateField) {
		var seconds = object.value;
		if(isNaN(seconds)) return;
		if (seconds >= 60) {
			object.value = seconds.substring(0,1);
			return;
		}
		if(seconds.length < 2) seconds = "0" + seconds;
		this.currentDate.setSeconds(seconds);
		this.currentDay = this.currentDate.getDate();
		this.currentMonth = this.currentDate.getMonth() + 1;
		this.currentYear = this.currentDate.getFullYear();
		this.dateField.value = this.getDateTimeString();
	}
	return;
}

UICalendar.prototype.setMinus = function(object) {
	if(this.dateField) {
		var minus = object.value;
		if(isNaN(minus)) return;
		if(minus >= 60){
			object.value = minus.substring(0,1);
			return;
		}
		if(minus.length < 2) minus = "0" + minus;
		this.currentDate.setMinutes(minus);
		this.currentDay = this.currentDate.getDate();
		this.currentMonth = this.currentDate.getMonth() + 1;
		this.currentYear = this.currentDate.getFullYear();
		this.dateField.value = this.getDateTimeString();
	}
	return;
}

UICalendar.prototype.setHour = function(object) {
	if(this.dateField) {
		var hour = object.value;
		if(isNaN(hour)) return;
		if (hour >= 24){
			object.value = hour.substring(0,1);	
			return;
		}
		if(hour.length < 2) hour = "0" + hour;
		this.currentDate.setHours(hour);
		this.currentDay = this.currentDate.getDate();
		this.currentMonth = this.currentDate.getMonth() + 1;
		this.currentYear = this.currentDate.getFullYear();
		this.dateField.value = this.getDateTimeString();
	}
	return;
}

UICalendar.prototype.clearDate = function() {
  this.dateField.value = '' ;
  this.hide() ;
}

UICalendar.prototype.getDayOfWeek = function(year, month, day) {
  var date = new Date(year, month - 1, day) ;
  return date.getDay() ;
}

UICalendar.prototype.getDaysInMonth = function(year, month) {
	return [31, ((!(year % 4 ) && ( (year % 100 ) || !( year % 400 ) ))? 29:28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
}

eXo.webui.UICalendar = new UICalendar('UICalendarControl') ;