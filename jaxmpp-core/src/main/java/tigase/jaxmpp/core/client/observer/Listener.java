package tigase.jaxmpp.core.client.observer;

import java.util.EventListener;

import tigase.jaxmpp.core.client.exceptions.JaxmppException;

/**
 * Interface for objects that are notified of events.
 * 
 * <pre>
 * moduleManager.getModule(ResourceBinderModule.class).addListener(ResourceBinderModule.ResourceBindSuccess,
 * 		new Listener&lt;ResourceBinderModule.ResourceBindEvent&gt;() {
 * 			public void handleEvent(ResourceBindEvent be) {
 * 				System.out.println(&quot;Binded as &quot; + be.getJid());
 * 			}
 * 		});
 * </pre>
 * 
 * @author bmalkow
 */
public interface Listener<E extends BaseEvent> extends EventListener {

	/**
	 * Execuded when an event happends.
	 * 
	 * @param be
	 *            event
	 */
	public void handleEvent(E be) throws JaxmppException;

}
