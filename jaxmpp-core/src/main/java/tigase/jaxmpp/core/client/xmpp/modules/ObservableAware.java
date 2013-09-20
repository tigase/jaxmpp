package tigase.jaxmpp.core.client.xmpp.modules;

import tigase.jaxmpp.core.client.observer.Observable;

/**
 * Interface should be implemented by module if {@linkplain Observable} should
 * be injected to module.
 */
@Deprecated
public interface ObservableAware {

	/**
	 * Set the new instance of {@linkplain Observable}.
	 * 
	 * @param observable
	 *            new instance of {@linkplain Observable}
	 */
	void setObservable(Observable observable);

}
