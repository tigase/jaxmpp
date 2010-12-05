package tigase.jaxmpp.core.client.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent;

public class Observable {

	private final List<Listener<? extends BaseEvent>> everythingListener = new ArrayList<Listener<? extends BaseEvent>>();

	private final Map<EventType, List<Listener<? extends BaseEvent>>> listeners = new HashMap<EventType, List<Listener<? extends BaseEvent>>>();

	private final Logger log = Logger.getLogger(this.getClass().getName());

	public void addListener(final EventType eventType, Listener<? extends BaseEvent> listener) {
		List<Listener<? extends BaseEvent>> lst = listeners.get(eventType);
		if (lst == null) {
			lst = new ArrayList<Listener<? extends BaseEvent>>();
			listeners.put(eventType, lst);
		}
		lst.add(listener);
	}

	public void addListener(Listener<? extends BaseEvent> listener) {
		this.everythingListener.add(listener);
	}

	public void fireEvent(final EventType eventType) {
		fireEvent(eventType, new BaseEvent(eventType));
	}

	@SuppressWarnings("unchecked")
	public void fireEvent(final EventType eventType, final BaseEvent event) {
		try {
			// if (log.isLoggable(Level.FINEST))
			// log.finest("Fire event " + eventType);
			List<Listener<? extends BaseEvent>> lst = listeners.get(eventType);
			if (lst != null) {
				event.setHandled(true);
				for (Listener<? extends BaseEvent> listener : lst) {
					((Listener<BaseEvent>) listener).handleEvent(event);
				}
			}
			if (!everythingListener.isEmpty()) {
				event.setHandled(true);
				for (Listener<? extends BaseEvent> listener : this.everythingListener) {
					((Listener<BaseEvent>) listener).handleEvent(event);
				}
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Problem on notifint observers", e);
		}
	}

	public void fireEvent(PresenceEvent event) {
		fireEvent(event.getType(), event);
	}

	public void removeAllListeners() {
		everythingListener.clear();
		listeners.clear();
	}

	public void removeListener(final EventType eventType, Listener<? extends BaseEvent> listener) {
		List<Listener<? extends BaseEvent>> lst = listeners.get(eventType);
		if (lst != null) {
			lst.remove(listener);
			if (lst.isEmpty()) {
				listeners.remove(eventType);
			}
		}
	}

}
