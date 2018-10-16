/*
 * ThreadSafeEventBus.java
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
package tigase.jaxmpp.j2se.eventbus;

import tigase.jaxmpp.core.client.eventbus.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ThreadSafeEventBus
		extends DefaultEventBus {

	private static int threadCounter = 1;

	private final Executor executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("EventBus-Thread-" + (++threadCounter));
			t.setDaemon(true);
			return t;
		}
	});

	@Override
	protected List<EventHandler> createHandlersArray() {
		// CopyOnWriteArrayList ??
		return new ArrayList<EventHandler>();
	}

	@Override
	protected Map<Object, Map<Class<? extends Event<?>>, List<EventHandler>>> createMainHandlersMap() {
		return new ConcurrentHashMap<Object, Map<Class<? extends Event<?>>, List<EventHandler>>>();
	}

	@Override
	protected Map<Class<? extends Event<?>>, List<EventHandler>> createTypeHandlersMap() {
		return new ConcurrentHashMap<Class<? extends Event<?>>, List<EventHandler>>();
	}

	@Override
	protected void doFire(final Event<EventHandler> event, final ArrayList<EventHandler> handlers) {
		final AtomicInteger counter = (event instanceof JaxmppEventWithCallback) ? new AtomicInteger(
				handlers.size() + 1) : null;

		for (final EventHandler eventHandler : handlers) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						log.finest(
								"Calling handler class " + eventHandler.getClass() + " with event " + event.getClass());
						if (eventHandler instanceof EventListener) {
							((EventListener) eventHandler).onEvent(event);
						} else {
							event.dispatch(eventHandler);
						}
					} catch (Throwable e) {
						if (log.isLoggable(Level.WARNING)) {
							log.log(Level.WARNING, "", e);
						}
					} finally {
						doFireEventRunAfter(counter, event);
					}
				}
			};
			executor.execute(r);
//			r.run();
		}

		doFireEventRunAfter(counter, event);
	}

	protected void doFireEventRunAfter(AtomicInteger counter, final Event<EventHandler> event) {
		if (counter != null && counter.decrementAndGet() == 0) {
			final JaxmppEventWithCallback.RunAfter run = ((JaxmppEventWithCallback<EventHandler>) event).getRunAfter();
			if (run != null) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						run.after(event);
					}
				});
			}
		}
	}
}