package tigase.jaxmpp.j2se.connectors.socket;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.Queue;

import tigase.jaxmpp.core.client.Connector;
import tigase.xml.DomBuilderHandler;
import tigase.xml.Element;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

public class SocketConnector implements Connector {

	static class Worker extends Thread {

		private final DomBuilderHandler handler;

		public Worker(DomBuilderHandler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			super.run();
			while (true) {
				System.out.println(",");
				Queue<Element> elems = handler.getParsedElements();
				System.out.println(" "+elems.size());
				Element elem;
				while ((elem = elems.poll()) != null) {
					System.out.println(" RECEIVED: " + elem.toString());
					// zrob cos z elem
				}
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		public void wakeUp() {
			synchronized (this) {
				System.out.println(".");
				notify();
			}
		}
	}

	public static void main(String[] args) throws Exception {

		final DomBuilderHandler domHandler = new DomBuilderHandler();

		Worker worker = new Worker(domHandler);

		final SimpleParser parser = SingletonFactory.getParserInstance();

		worker.start();
		Socket s = new Socket("tigase.tigase.org", 5222);
		OutputStream out = s.getOutputStream();
		Reader in = new InputStreamReader(s.getInputStream());
		out.write("<stream:stream to='tigase.org' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>".getBytes());

		char[] buffer = new char[10240];

		int r;
		while ((r = in.read(buffer)) != -1) {
			System.out.println("R: " + new String(buffer));

			parser.parse(domHandler, buffer, 0, r);
			worker.wakeUp();
		}

	}
}
