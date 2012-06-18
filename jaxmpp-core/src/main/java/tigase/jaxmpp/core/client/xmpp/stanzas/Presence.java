package tigase.jaxmpp.core.client.xmpp.stanzas;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class Presence extends Stanza {

	public static enum Show {
		away(3),
		chat(5),
		dnd(1),
		online(4),
		xa(2);

		private final int weight;

		private Show(int weight) {
			this.weight = weight;
		}

		public int getWeight() {
			return weight;
		}

	}

	public static Presence create() throws XMLException {
		return new Presence(new DefaultElement("presence"));
	}

	private String cacheNickname;

	private boolean cacheNicknameSet = false;

	private Integer cachePriority;

	private Show cacheShow;

	private String cacheStatus;

	private boolean cacheStatusSet = false;

	public Presence(Element element) throws XMLException {
		super(element);
		if (!"presence".equals(element.getName()))
			throw new RuntimeException("Wrong element name: " + element.getName());
	}

	public String getNickname() throws XMLException {
		if (cacheNicknameSet)
			return cacheNickname;

		cacheNickname = getChildElementValue("nick", "http://jabber.org/protocol/nick");
		cacheNicknameSet = true;

		return cacheNickname;
	}

	public Integer getPriority() throws XMLException {
		if (cachePriority != null)
			return cachePriority;

		String x = getChildElementValue("priority");
		final Integer p;
		if (x == null)
			p = 0;
		else
			p = Integer.valueOf(x);

		cachePriority = p;

		return p;
	}

	public Show getShow() throws XMLException {
		if (cacheShow != null)
			return cacheShow;

		String x = getChildElementValue("show");
		final Show show;
		if (x == null)
			show = Show.online;
		else
			show = Show.valueOf(x);
		cacheShow = show;
		return show;
	}

	public String getStatus() throws XMLException {
		if (cacheStatusSet)
			return cacheStatus;
		cacheStatus = getChildElementValue("status");
		cacheStatusSet = true;

		return cacheStatus;
	}

	public void setNickname(String value) throws XMLException {
		setChildElementValue("nick", "http://jabber.org/protocol/nick", value == null ? null : value.toString());
	}

	public void setPriority(Integer value) throws XMLException {
		setChildElementValue("priority", value == null ? null : value.toString());
	}

	public void setShow(Show value) throws XMLException {
		if (value == null || value == Show.online)
			setChildElementValue("show", null);
		else
			setChildElementValue("show", value.name());
	}

	public void setStatus(String value) throws XMLException {
		setChildElementValue("status", value);
	}

}
