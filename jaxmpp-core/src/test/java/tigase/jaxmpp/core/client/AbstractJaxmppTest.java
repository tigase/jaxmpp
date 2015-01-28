package tigase.jaxmpp.core.client;

import junit.framework.TestCase;

import org.junit.Before;

import tigase.jaxmpp.core.client.eventbus.DefaultEventBus;
import tigase.jaxmpp.core.client.eventbus.EventBus;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xmpp.modules.ModuleProvider;
import tigase.jaxmpp.core.client.xmpp.stream.XmppStreamsManager;

public abstract class AbstractJaxmppTest extends TestCase {

	protected Context context;
	private DefaultEventBus eventBus;
	private MockSessionObject sessionObject;
	private XmppStreamsManager streamsManager;

	private MockWriter writer;

	public AbstractJaxmppTest() {
		this.context = new Context() {

			@Override
			public EventBus getEventBus() {
				return eventBus;
			}

			@Override
			public ModuleProvider getModuleProvider() {
				return null;
			}

			@Override
			public SessionObject getSessionObject() {
				return sessionObject;
			}

			@Override
			public XmppStreamsManager getStreamsManager() {
				return streamsManager;
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
		this.streamsManager = new XmppStreamsManager();
		this.streamsManager.setContext(context);
	}

}
