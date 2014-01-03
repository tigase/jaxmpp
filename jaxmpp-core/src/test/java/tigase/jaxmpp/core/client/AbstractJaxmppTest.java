package tigase.jaxmpp.core.client;

import junit.framework.TestCase;

import org.junit.Before;

import tigase.jaxmpp.core.client.eventbus.DefaultEventBus;
import tigase.jaxmpp.core.client.eventbus.EventBus;
import tigase.jaxmpp.core.client.xml.Element;

public abstract class AbstractJaxmppTest extends TestCase {

	protected Context context;
	private DefaultEventBus eventBus;
	private MockSessionObject sessionObject;

	private MockWriter writer;

	public AbstractJaxmppTest() {
		this.context = new Context() {

			@Override
			public EventBus getEventBus() {
				return eventBus;
			}

			@Override
			public SessionObject getSessionObject() {
				return sessionObject;
			}

			@Override
			public PacketWriter getWriter() {
				return writer;
			}
		};
	}

	public Element poll() {
		return ((MockWriter) context.getWriter()).poll();
	}

	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		this.eventBus = new DefaultEventBus();
		this.sessionObject = new MockSessionObject(eventBus);
		this.writer = new MockWriter(sessionObject);
	}

}
