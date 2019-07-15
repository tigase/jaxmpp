package tigase.jaxmpp.core.client.xmpp.modules.omemo;

import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;

import java.util.List;

public interface JaXMPPSignalProtocolStore
		extends SignalProtocolStore, OMEMOSessionsProvider {

	List<PreKeyRecord> loadPreKeys();

	List<Integer> getSubDevice(String name);

}
