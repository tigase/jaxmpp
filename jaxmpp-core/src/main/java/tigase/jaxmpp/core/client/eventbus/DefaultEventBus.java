package tigase.jaxmpp.core.client.eventbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultEventBus extends EventBus {

	protected final Map<Object, Map<EventType<?>, List<EventHandler>>> handlers = new HashMap<Object, Map<EventType<?>, List<EventHandler>>>();

	protected final Logger log = Logger.getLogger(this.getClass().getName());

	protected boolean throwingExceptionOn = true;

	@Override
	public <H extends EventHandler> void addHandler(EventType<H> type, H handler) {
		doAdd(type, null, handler);
	}

	@Override
	public <H extends EventHandler> void addHandler(EventType<H> type, Object source, H handler) {
		doAdd(type, source, handler);
	}

	@Override
	public <H extends EventHandler> void addListener(EventListener listener) {
		doAdd(null, null, listener);
	}

	@Override
	public <H extends EventHandler> void addListener(EventType<H> type, EventListener listener) {
		doAdd(type, null, listener);
	}

	@Override
	public <H extends EventHandler> void addListener(EventType<H> type, Object source, EventListener listener) {
		doAdd(type, source, listener);
	}

	protected void doAdd(EventType<?> type, Object source, EventHandler handler) {
		synchronized (this.handlers) {
			Map<EventType<?>, List<EventHandler>> hdlrs = handlers.get(source);
			if (hdlrs == null) {
				hdlrs = new HashMap<EventType<?>, List<EventHandler>>();
				handlers.put(source, hdlrs);
			}

			List<EventHandler> lst = hdlrs.get(type);
			if (lst == null) {
				lst = new ArrayList<EventHandler>();
				hdlrs.put(type, lst);
			}
			lst.add(handler);
		}

	}

	protected void doFire(Event<EventHandler> event, Object source) {
		if (event == null) {
			throw new NullPointerException("Cannot fire null event");
		}

		setEventSource(event, source);

		final ArrayList<EventHandler> handlers = new ArrayList<EventHandler>();
		handlers.addAll(getHandlersList(event.getType(), source));
		handlers.addAll(getHandlersList(null, source));
		if (source != null) {
			handlers.addAll(getHandlersList(event.getType(), null));
			handlers.addAll(getHandlersList(null, null));
		}

		doFire(event, source, handlers);
	}

	protected void doFire(Event<EventHandler> event, Object source, ArrayList<EventHandler> handlers) {
		final Set<Throwable> causes = new HashSet<Throwable>();

		for (EventHandler eventHandler : handlers) {
			try {
				if (eventHandler instanceof EventListener) {
					((EventListener) eventHandler).onEvent(event);
				} else {
					event.dispatch(eventHandler);
				}
			} catch (Throwable e) {
				if (log.isLoggable(Level.WARNING))
					log.log(Level.WARNING, "", e);
				causes.add(e);
			}
		}

		if (!causes.isEmpty()) {
			if (throwingExceptionOn)
				throw new EventBusException(causes);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void fire(Event<?> event) {
		doFire((Event<EventHandler>) event, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void fire(Event<?> event, Object source) {
		doFire((Event<EventHandler>) event, source);
	}

	protected Collection<EventHandler> getHandlersList(EventType<?> type, Object source) {
		final Map<EventType<?>, List<EventHandler>> hdlrs = handlers.get(source);
		if (hdlrs == null) {
			return Collections.emptyList();
		} else {
			final List<EventHandler> lst = hdlrs.get(type);
			if (lst != null) {
				return lst;
			} else
				return Collections.emptyList();
		}
	}

	public boolean isThrowingExceptionOn() {
		return throwingExceptionOn;
	}

	@Override
	public void remove(EventHandler handler) {
		synchronized (this.handlers) {
			Iterator<Entry<Object, Map<EventType<?>, List<EventHandler>>>> l = this.handlers.entrySet().iterator();
			while (l.hasNext()) {
				Map<EventType<?>, List<EventHandler>> eventHandlers = l.next().getValue();
				Iterator<Entry<EventType<?>, List<EventHandler>>> iterator = eventHandlers.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<EventType<?>, List<EventHandler>> entry = iterator.next();
					if (entry != null) {
						entry.getValue().remove(handler);
						if (entry.getValue().isEmpty())
							iterator.remove();
					}
				}
				if (eventHandlers.isEmpty())
					l.remove();
			}
		}
	}

	@Override
	public void remove(EventType<?> type, EventHandler handler) {
		remove(type, null, handler);
	}

	@Override
	public void remove(EventType<?> type, Object source, EventHandler handler) {
		synchronized (this.handlers) {

			final Map<EventType<?>, List<EventHandler>> hdlrs = handlers.get(source);
			if (hdlrs != null) {
				List<EventHandler> lst = hdlrs.get(type);
				if (lst != null) {
					lst.remove(handler);
					if (lst.isEmpty()) {
						hdlrs.remove(type);
					}
					if (hdlrs.isEmpty()) {
						handlers.remove(source);
					}
				}
			}
		}
	}

	public void setThrowingExceptionOn(boolean throwingExceptionOn) {
		this.throwingExceptionOn = throwingExceptionOn;
	}

}
