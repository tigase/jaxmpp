/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
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
package tigase.jaxmpp.core.client;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public abstract class AbstractStanzaHandler implements Runnable {

	protected final Element element;

	protected final SessionObject sessionObject;

	protected final PacketWriter writer;

	public AbstractStanzaHandler(Element element, PacketWriter writer, SessionObject sessionObject) {
		super();
		this.writer = writer;
		this.element = element;
		this.sessionObject = sessionObject;
	}

	protected abstract void process() throws XMLException, XMPPException, JaxmppException;

	@Override
	public void run() {
		try {
			try {
				process();
				// }catch(XMPPException e){
			} catch (Exception e) {
				e.printStackTrace();
				Element errorResult = Processor.createError(element, e);
				if (errorResult != null)
					writer.write(errorResult);
			}
		} catch (JaxmppException e) {
			throw new RuntimeException(e);
			// TODO: handle exception
		}
	}
}