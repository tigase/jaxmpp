package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.PacketWriter;

/**
 * Interface should be implemented by module if {@linkplain PacketWriter} should
 * be injected to this module.
 * 
 */
public interface PacketWriterAware {

	/**
	 * Set {@linkplain PacketWriter}.
	 * 
	 * @param packetWriter
	 *            {@linkplain PacketWriter}
	 */
	void setPacketWriter(PacketWriter packetWriter);

}
