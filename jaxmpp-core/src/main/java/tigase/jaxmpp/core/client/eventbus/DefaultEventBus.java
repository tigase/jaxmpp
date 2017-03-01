/*
 * DefaultEventBus.java
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
package tigase.jaxmpp.core.client.eventbus;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic implementation of {@link EventBus}.
 */
public class DefaultEventBus
		extends EventBus {

	private final static Object NULL_SOURCE = new Object();
	private final static Class<? extends Event<?>> NULL_TYPE = N.class;
	protected final Map<Object, Map<Class<? extends Event<?>>, List<EventHandler>>> handlers;
	protected final Logger log = Logger.getLogger(this.getClass().getName());
	protected boolean throwingExceptionOn = true;

	public DefaultEventBus() {
		this.handlers = createMainHandlersMap();
	}

	@Override
	public <H extends EventHandler> void addHandler(Class<? extends Event<H>> type, H handler) {
		doAdd(type, null, handler);
	}

	@Override
	public <H extends EventHandler> void addListener(Class<? extends Event<H>> type, EventListener listener) {
		doAdd(type, null, listener);
	}

	@Override
	public <H extends EventHandler> void addListener(EventListener listener) {
		doAdd(null, null, listener);
	}

	protected List<EventHandler> createHandlersArray() {
		return new ArrayList<EventHandler>();
	}

	protected Map<Object, Map<Class<? extends Event<?>>, List<EventHandler>>> createMainHandlersMap() {
		return new HashMap<Object, Map<Class<? extends Event<?>>, List<EventHandler>>>();
	}

	protected Map<Class<? extends Event<?>>, List<EventHandler>> createTypeHandlersMap() {
		return new HashMap<Class<? extends Event<?>>, List<EventHandler>>();
	}

	protected void doAdd(Class<? extends Event<?>> type, Object source, EventHandler handler) {
		synchronized (this.handlers) {
			Map<Class<? extends Event<?>>, List<EventHandler>> hdlrs = getHandlersBySource(source);
			if (hdlrs == null) {
				hdlrs = createTypeHandlersMap();
				handlers.put(source == null ? NULL_SOURCE : source, hdlrs);
			}

			List<EventHandler> lst = hdlrs.get(type == null ? NULL_TYPE : type);
			if (lst == null) {
				lst = createHandlersArray();
				hdlrs.put(type == null ? NULL_TYPE : type, lst);
			}
			lst.add(handler);
		}

	}

	@SuppressWarnings("unchecked")
	protected void doFire(Event<EventHandler> event) {
		if (event == null) {
			throw new NullPointerException("Cannot fire null event");
		}

		final ArrayList<EventHandler> handlers = new ArrayList<EventHandler>();
		synchronized (this.handlers) {
			handlers.addAll(getHandlersList((Class<? extends Event<?>>) event.getClass()));
			handlers.addAll(getHandlersList(null));
		}
		doFire(event, handlers);
	}

	protected void doFire(Event<EventHandler> event, ArrayList<EventHandler> handlers) {
		final Set<Throwable> causes = new HashSet<Throwable>();

		for (EventHandler eventHandler : handlers) {
			try {
				log.finest("Calling handler class " + eventHandler.getClass() + " with event " + event.getClass());
				if (eventHandler instanceof EventListener) {
					((EventListener) eventHandler).onEvent(event);
				} else {
					event.dispatch(eventHandler);
				}
			} catch (Throwable e) {
				if (log.isLoggable(Level.WARNING)) {
					log.log(Level.WARNING, "", e);
				}
				causes.add(e);
			}
		}

		if (event instanceof JaxmppEventWithCallback) {
			JaxmppEventWithCallback.RunAfter run = ((JaxmppEventWithCallback<EventHandler>) event).getRunAfter();
			if (run != null) {
				run.after(event);
			}
		}

		if (!causes.isEmpty()) {
			if (throwingExceptionOn) {
				throw new EventBusException(causes);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void fire(Event<?> event) {
		doFire((Event<EventHandler>) event);
	}

	private Map<Class<? extends Event<?>>, List<EventHandler>> getHandlersBySource(Object source) {
		return handlers.get(source == null ? NULL_SOURCE : source);
	}

	protected Collection<EventHandler> getHandlersList(Class<? extends Event<?>> type) {
		final Map<Class<? extends Event<?>>, List<EventHandler>> hdlrs = getHandlersBySource(null);
		if (hdlrs == null) {
			return Collections.emptyList();
		} else {
			final List<EventHandler> lst = hdlrs.get(type == null ? NULL_TYPE : type);
			if (lst != null) {
				return lst;
			} else {
				return Collections.emptyList();
			}
		}
	}

	public boolean isThrowingExceptionOn() {
		return throwingExceptionOn;
	}

	public void setThrowingExceptionOn(boolean throwingExceptionOn) {
		this.throwingExceptionOn = throwingExceptionOn;
	}

	@Override
	public void remove(Class<? extends Event<?>> type, EventHandler handler) {
		synchronized (this.handlers) {
			final Map<Class<? extends Event<?>>, List<EventHandler>> hdlrs = getHandlersBySource(null);
			if (hdlrs != null) {
				List<EventHandler> lst = hdlrs.get(type == null ? NULL_TYPE : type);
				if (lst != null) {
					lst.remove(handler);
					if (lst.isEmpty()) {
						hdlrs.remove(type == null ? NULL_TYPE : type);
					}
					if (hdlrs.isEmpty()) {
						handlers.remove(NULL_SOURCE);
					}
				}
			}
		}
	}

	@Override
	public void remove(EventHandler handler) {
		synchronized (this.handlers) {
			Iterator<Entry<Object, Map<Class<? extends Event<?>>, List<EventHandler>>>> l = this.handlers.entrySet()
					.iterator();
			while (l.hasNext()) {
				Map<Class<? extends Event<?>>, List<EventHandler>> eventHandlers = l.next().getValue();
				Iterator<Entry<Class<? extends Event<?>>, List<EventHandler>>> iterator = eventHandlers.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Entry<Class<? extends Event<?>>, List<EventHandler>> entry = iterator.next();
					if (entry != null) {
						entry.getValue().remove(handler);
						if (entry.getValue().isEmpty()) {
							iterator.remove();
						}
					}
				}
				if (eventHandlers.isEmpty()) {
					l.remove();
				}
			}
		}
	}

	private final static class N
			extends Event<EventHandler> {

		@Override
		public void dispatch(EventHandler handler) throws Exception {
		}
	}

}
