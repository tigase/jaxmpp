package tigase.jaxmpp.core.client;

import junit.framework.TestCase;

import org.junit.Before;

import tigase.jaxmpp.core.client.eventbus.DefaultEventBus;
import tigase.jaxmpp.core.client.eventbus.EventBus;
import tigase.jaxmpp.core.client.xml.Element;

public abstract class AbstractJaxmppTest extends TestCase {

	private DefaultEventBus eventBus;
	private MockSessionObject sessionObject;
	private MockWriter writer;

	@Before
	protected void setUp() throws Exception {
		super.setUp();
		this.eventBus = new DefaultEventBus();
		this.sessionObject = new MockSessionObject(eventBus);
		this.writer = new MockWriter(sessionObject);
	}

	protected Context context;

	public Element poll() {
		return ((MockWriter) context.getWriter()).poll();
	}

	public AbstractJaxmppTest() {
		this.context = new Context() {

			@Override
			public SessionObject getSessionObject() {
				return sessionObject;
			}

			@Override
			public EventBus getEventBus() {
				return eventBus;
			}

			@Override
			public PacketWriter getWriter() {
				return writer;
			}
		};
	}

}
