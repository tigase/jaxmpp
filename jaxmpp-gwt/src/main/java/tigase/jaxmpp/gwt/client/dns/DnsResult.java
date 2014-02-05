/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc. <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.gwt.client.dns;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 *
 * @author andrzej
 */
public class DnsResult extends JavaScriptObject {

   protected DnsResult() {}

   public final native String getDomain() /*-{
     return this.domain;
   }-*/;
   
   public final native JsArray<DnsEntry> getC2S() /*-{
     return this.c2s;
   }-*/;        

   public final native JsArray<DnsEntry> getBosh() /*-{
	 return this.bosh;
   }-*/;
   
   public final native JsArray<DnsEntry> getWebSocket() /*-{
	 return this.websocket;
   }-*/;   
   
   public final native String next() /*-{
	   var urls = [];
	   for (var i=0; i<this.websocket.length; i++) {
		   if((!this.websocket[i].failed) && (this.websocket[i].url.indexOf("wss://") == 0)) {
			   urls.push(this.websocket[i].url);
		   }
	   }
	   if (urls.length > 0) {
		   return urls[Math.floor(Math.random()*urls.length)];
	   }
	   for (var i=0; i<this.websocket.length; i++) {
		   if(!this.websocket[i].failed) {
			   urls.push(this.websocket[i].url);
		   }
	   }
	   if (urls.length > 0) {
		   return urls[Math.floor(Math.random()*urls.length)];
	   }
	   for (var i=0; i<this.bosh.length; i++) {
		   if (!this.bosh[i].failed) {
			   urls.push(this.bosh[i].url);
		   }
	   }	
	   if (urls.length > 0) {
		   return urls[Math.floor(Math.random()*urls.length)];
	   }
	   return null;
   }-*/;
   
   public final native boolean hasMore() /*-{
	   for (var i=0; i<this.websocket.length; i++) {
		   if(!this.websocket[i].failed) {
			   return true;
		   }
	   }
	   for (var i=0; i<this.bosh.length; i++) {
		   if (!this.bosh[i].failed) {
			   return true;
		   }
	   }
	   return false;
   }-*/;
   
   public final native void connectionFailed(String url) /*-{
	   for (var i=0; i<this.websocket.length; i++) {
	       if (this.websocket[i].url == url) {
		       this.websocket[i].failed = true;
		   }
	   }
	   for (var i=0; i<this.bosh.length; i++) {
	       if (this.bosh[i].url == url) {
		       this.bosh[i].failed = true;
		   }
	   }
   }-*/;
   
   public final native String getUrlForHost(String host) /*-{
	   for (var i=0; i<this.websocket.length; i++) {
	       if (this.websocket[i].url.indexOf("wss://"+host) >= 0)
		       return this.websocket[i].url;
	   }
	   for (var i=0; i<this.websocket.length; i++) {
	       if (this.websocket[i].url.indexOf("ws://"+host) >= 0)
		       return this.websocket[i].url;
	   }
	   for (var i=0; i<this.bosh.length; i++) {
	       if (this.bosh[i].url.indexOf("://"+host) >= 0) {
		       return this.bosh[i].url;
		   }
	   }
	   return null;
   }-*/;
}
