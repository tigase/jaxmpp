package tigase.jaxmpp.core.client.eventbus;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import tigase.jaxmpp.core.client.eventbus.DefaultEventBusTest.Test01Handler.Test01Event;
import tigase.jaxmpp.core.client.eventbus.DefaultEventBusTest.Test02Handler.Test02Event;

public class DefaultEventBusTest extends TestCase {

	public interface Test01Handler extends EventHandler {

		public static class Test01Event extends Event<Test01Handler> {

			public static final EventType<Test01Handler> TYPE = new EventType<Test01Handler>();

			private final String data;

			public Test01Event(String data) {
				super(TYPE);
				this.data = data;
			}

			@Override
			protected void dispatch(Test01Handler handler) {
				handler.onTest01Event(data);
			}

			public String getData() {
				return data;
			}

		}

		void onTest01Event(String data);
	}

	public interface Test02Handler extends EventHandler {

		public static class Test02Event extends Event<Test02Handler> {

			public static final EventType<Test02Handler> TYPE = new EventType<Test02Handler>();

			private final String data;

			public Test02Event(String data) {
				super(TYPE);
				this.data = data;
			}

			@Override
			protected void dispatch(Test02Handler handler) {
				handler.onTest02Event(data);
			}

			public String getData() {
				return data;
			}

		}

		void onTest02Event(String data);
	}

	private EventBus eventBus;

	@Override
	@Before
	public void setUp() throws Exception {
		eventBus = new DefaultEventBus();
	}

	@Test
	public void test0() {
		Object fakeSource1 = new Object();
		Object fakeSource2 = new Object();
		final String[] value = new String[1];
		Test01Handler h1 = new Test01Handler() {

			@Override
			public void onTest01Event(String data) {
				Assert.fail("It shouldn't be called!");
			}
		};
		eventBus.addHandler(Test01Event.TYPE, fakeSource1, h1);
		eventBus.addHandler(Test01Event.TYPE, fakeSource2, new Test01Handler() {

			@Override
			public void onTest01Event(String data) {
				value[0] = "h" + data;
			}
		});

		Test01Event event = new Test01Event("test0.1");
		eventBus.fire(event, this);

		event = new Test01Event("test0.2");
		eventBus.fire(event, fakeSource2);

		Assert.assertEquals("htest0.2", value[0]);

		eventBus.remove(h1);

		event = new Test01Event("test0.3");
		eventBus.fire(event, fakeSource1);
	}

	@Test
	public void test1() {
		final String[] value = new String[5];
		eventBus.addHandler(Test01Event.TYPE, new Test01Handler() {

			@Override
			public void onTest01Event(String data) {
				value[0] = "h" + data;
			}
		});

		eventBus.addHandler(Test02Event.TYPE, new Test02Handler() {

			@Override
			public void onTest02Event(String data) {
				value[2] = "x" + data;
			}
		});

		eventBus.addListener(new EventListener() {

			@Override
			public void onEvent(Event<? extends EventHandler> event) {
				if (event instanceof Test01Event) {
					value[1] = "l" + ((Test01Event) event).getData();
				} else if (event instanceof Test02Event) {
					value[4] = "l" + ((Test02Event) event).getData();
				}
			}
		});

		eventBus.addListener(Test02Event.TYPE, new EventListener() {

			@Override
			public void onEvent(Event<? extends EventHandler> event) {
				value[3] = "fail";
			}
		});

		Test01Event event = new Test01Event("test01");
		eventBus.fire(event, this);

		Assert.assertEquals("htest01", value[0]);
		Assert.assertEquals("ltest01", value[1]);
		Assert.assertNull(value[2]);
		Assert.assertNull(value[3]);
		Assert.assertNull(value[4]);
	}

	@Test
	public void testRemove() {
		final String[] value = new String[2];
		Test01Handler h1 = new Test01Handler() {

			@Override
			public void onTest01Event(String data) {
				value[0] = data;
			}
		};
		EventListener l1 = new EventListener() {

			@Override
			public void onEvent(Event<? extends EventHandler> event) {
				value[1] = ((Test01Event) event).getData();
			}
		};
		eventBus.addHandler(Test01Event.TYPE, h1);
		eventBus.addListener(Test01Event.TYPE, l1);

		Test01Event event = new Test01Event("t1");
		eventBus.fire(event, this);

		Assert.assertEquals("t1", value[0]);
		Assert.assertEquals("t1", value[1]);

		eventBus.remove(h1);

		event = new Test01Event("t2");
		eventBus.fire(event, this);

		Assert.assertEquals("t1", value[0]);
		Assert.assertEquals("t2", value[1]);

		eventBus.remove(Test01Event.TYPE, l1);

		event = new Test01Event("t3");
		eventBus.fire(event, this);

		Assert.assertEquals("t1", value[0]);
		Assert.assertEquals("t2", value[1]);
	}
}
