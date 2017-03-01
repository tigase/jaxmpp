/*
 * PacketWriterAware.java
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

package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.PacketWriter;

/**
 * Interface should be implemented by module if {@linkplain PacketWriter} should
 * be injected to this module.
 */
public interface PacketWriterAware {

	/**
	 * Set {@linkplain PacketWriter}.
	 *
	 * @param packetWriter {@linkplain PacketWriter}
	 */
	void setPacketWriter(PacketWriter packetWriter);

}
