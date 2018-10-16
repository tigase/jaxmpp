/*
 * DnsEntry.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2017 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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
import com.google.gwt.core.client.JsArrayString;

/**
 * @author andrzej
 */
public class DnsEntry
		extends JavaScriptObject {

	protected DnsEntry() {
	}

	public final native String getHost() /*-{
        return this.host;
    }-*/;

	public final native JsArrayString getIps() /*-{
        return this.ip;
    }-*/;

	public final native int getPort() /*-{
        return this.port;
    }-*/;

	public final native String getUrl() /*-{
        return this.url;
    }-*/;
}
