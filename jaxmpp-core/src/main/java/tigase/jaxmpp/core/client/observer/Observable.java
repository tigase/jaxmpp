package tigase.jaxmpp.core.client.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;

/**
 * Object for registering {@linkplain Listener listeners} and fire
 * {@linkplain BaseEvent events}.
 * 
 * <pre>
 * Observable observable = new Observable(null);
 * observable.addListener(ResourceBinderModule.ResourceBindSuccess, new Listener&lt;ResourceBinderModule.ResourceBindEvent&gt;() {
 * 	public void handleEvent(ResourceBindEvent be) {
 * 	}
 * });
 * observable.fireEvent(new ResourceBinderModule.ResourceBindEvent(ResourceBinderModule.ResourceBindSuccess));
 * </pre>
 * 
 * @author bmalkow
 * 
 */
public class Observable {

	private final List<Listener<? extends BaseEvent>> everythingListener = new ArrayList<Listener<? extends BaseEvent>>();

	private final Map<EventType, List<Listener<? extends BaseEvent>>> listeners = new HashMap<EventType, List<Listener<? extends BaseEvent>>>();

	private final Logger log = Logger.getLogger(this.getClass().getName());

	private final Observable parent;

	/**
	 * Creates new instance of Observable.
	 */
	public Observable() {
		this(null);
	}

	/**
	 * Creates new instance of Observable.
	 * 
	 * @param parent
	 *            parent observable object. All events will be sent also to
	 *            parent.
	 */
	public Observable(Observable parent) {
		this.parent = parent;
	}

	/**
	 * Adds a listener bound by the given event type.
	 * 
	 * @param eventType
	 *            type of event
	 * @param listener
	 *            the listener
	 */
	public void addListener(final EventType eventType, Listener<? extends BaseEvent> listener) {
		synchronized (this.listeners) {
			List<Listener<? extends BaseEvent>> lst = listeners.get(eventType);
			if (lst == null) {
				lst = new ArrayList<Listener<? extends BaseEvent>>();
				listeners.put(eventType, lst);
			}
			lst.add(listener);
		}
	}

	/**
	 * Add a listener bound by the all event types.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void addListener(Listener<? extends BaseEvent> listener) {
		synchronized (this.everythingListener) {
			this.everythingListener.add(listener);
		}
	}

	/**
	 * Fires an event.
	 * 
	 * @param event
	 *            event
	 * @throws JaxmppException
	 */
	public void fireEvent(BaseEvent event) throws JaxmppException {
		fireEvent(event.getType(), event);
	}

	/**
	 * Fires an event.
	 * 
	 * @param eventType
	 *            type of event
	 * @param event
	 *            event
	 */
	@SuppressWarnings("unchecked")
	public void fireEvent(final EventType eventType, final BaseEvent event) throws JaxmppException {
		try {
			// if (log.isLoggable(Level.FINEST))
			// log.finest("Fire event " + eventType);
			synchronized (this.listeners) {
				List<Listener<? extends BaseEvent>> lst = listeners.get(eventType);
				if (lst != null) {
					event.setHandled(true);
					for (Listener<? extends BaseEvent> listener : lst) {
						((Listener<BaseEvent>) listener).handleEvent(event);
					}
				}
			}
			synchronized (everythingListener) {
				if (!everythingListener.isEmpty()) {
					event.setHandled(true);
					for (Listener<? extends BaseEvent> listener : this.everythingListener) {
						((Listener<BaseEvent>) listener).handleEvent(event);
					}
				}
			}
		} catch (JaxmppException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			// log.log(Level.WARNING, "Problem on notifint observers", e);
			throw new JaxmppException(e);
		}
		if (parent != null) {
			parent.fireEvent(eventType, event);
		}
	}

	/**
	 * Fires {@linkplain BaseEvent BaseEvent}.
	 * 
	 * @param eventType
	 * @throws JaxmppException
	 */
	public void fireEvent(final EventType eventType, final SessionObject sessionObject) throws JaxmppException {
		fireEvent(eventType, new BaseEvent(eventType, sessionObject));
	}

	/**
	 * Removes all listeners.
	 */
	public void removeAllListeners() {
		synchronized (this.everythingListener) {
			everythingListener.clear();
		}
		synchronized (this.listeners) {
			listeners.clear();
		}
	}

	/**
	 * Removes a listener.
	 * 
	 * @param eventType
	 *            type of event
	 * @param listener
	 *            listener
	 */
	public void removeListener(final EventType eventType, Listener<? extends BaseEvent> listener) {
		synchronized (this.listeners) {
			List<Listener<? extends BaseEvent>> lst = listeners.get(eventType);
			if (lst != null) {
				lst.remove(listener);
				if (lst.isEmpty()) {
					listeners.remove(eventType);
				}
			}
		}
	}

	/**
	 * Removes a listener.
	 * 
	 * @param eventType
	 *            type of event
	 * @param listener
	 *            listener
	 */
	public void removeListener(Listener<? extends BaseEvent> listener) {
		synchronized (this.everythingListener) {
			this.everythingListener.remove(listener);
		}
	}

}
