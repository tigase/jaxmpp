package tigase.jaxmpp.j2se;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class BoshConnector {

	static class ConnectorData {

		int defaultTimeout = 3;

		String fromUser;

		long rid;

		String sid;

		Stage stage = Stage.disconnected;

		String toHost;

		URL url;

	}

	protected static enum Stage {
		connected,
		connecting,
		disconnected
	}

	public static void main(String[] args) throws Exception {
		// cd.serverJID = BareJID.bareJIDInstance("malkowscy.net");
		// cd.userJID = BareJID.bareJIDInstance("bmalkow@malkowscy.net");

		BoshConnector con = new BoshConnector();
		con.data.url = new URL("http://messenger.tigase.org/bosh");
		con.data.toHost = "tigase.org";
		con.data.fromUser = "bmalkow@tigase.org";

		con.start();

		System.out.println(".");

		Thread.sleep(1000 * 15);

		System.out.println(".");
		con.stop();
	}

	private final ConnectorData data = new ConnectorData();

	private final Queue<Element> toSendQueue = new LinkedList<Element>();

	protected void onError(int responseCode, Element response, Throwable caught) {
		System.out.println("onError(): responseCode=" + responseCode + "; " + " " + caught);
		this.data.stage = Stage.disconnected;
	}

	protected void onResponse(final int responseCode, final Element response) {
		System.out.println("onResponse()");
		try {
			if (this.data.stage == Stage.connecting) {
				this.data.sid = response.getAttribute("sid");
				data.stage = Stage.connected;
			}
			if (this.data.stage == Stage.connected) {
				Element toSend = toSendQueue.poll();
				final Element body = prepareBody(toSend);
				processSendData(body);
			}
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	protected void onTerminate(int responseCode, Element response) {
		System.out.println("onTerminate()");
		this.data.stage = Stage.disconnected;

	}

	private Element prepareBody(Element payload) throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("rid", String.valueOf(++data.rid));
		e.setAttribute("sid", this.data.sid);
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");

		if (payload != null)
			e.addChild(payload);

		return e;
	}

	private Element prepareStartBody() throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("content", "text/xml; charset=utf-8");
		e.setAttribute("from", data.fromUser);
		e.setAttribute("hold", "1");
		e.setAttribute("rid", String.valueOf(++data.rid));
		e.setAttribute("to", data.toHost);
		e.setAttribute("secure", "true");
		e.setAttribute("wait", String.valueOf(data.defaultTimeout));
		e.setAttribute("xml:lang", "en");
		e.setAttribute("xmpp:version", "1.0");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");
		e.setAttribute("xmlns:xmpp", "urn:xmpp:xbosh");

		return e;
	}

	private Element prepareTerminateBody(Element payload) throws XMLException {
		Element e = new DefaultElement("body");
		e.setAttribute("rid", String.valueOf(++data.rid));
		e.setAttribute("sid", this.data.sid);
		e.setAttribute("type", "terminate");
		e.setAttribute("xmlns", "http://jabber.org/protocol/httpbind");

		if (payload != null)
			e.addChild(payload);

		return e;
	}

	protected void processSendData(final Element element) throws XMLException {
		BoshWorker worker = new BoshWorker(data, element) {

			@Override
			protected void onError(int responseCode, Element response, Throwable caught) {
				BoshConnector.this.onError(responseCode, response, caught);
			}

			@Override
			protected void onSuccess(int responseCode, Element response) {
				BoshConnector.this.onResponse(responseCode, response);
			}

			@Override
			protected void onTerminate(int responseCode, Element response) {
				BoshConnector.this.onTerminate(responseCode, response);
			}
		};

		(new Thread(worker)).start();
	}

	public void start() throws IOException, XMLException {
		data.sid = null;
		data.stage = Stage.connecting;
		this.data.rid = (long) (Math.random() * 10000000);
		processSendData(prepareStartBody());

	}

	public void stop() throws IOException, XMLException {
		if (data.stage != Stage.disconnected)
			processSendData(prepareTerminateBody(null));
	}
}
