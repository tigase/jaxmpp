package tigase.jaxmpp.core.client.eventbus;

import java.util.Set;

public class EventBusException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected static String createMessage(Set<Throwable> causes) {
		if (causes.isEmpty()) {
			return null;
		}

		StringBuilder b = new StringBuilder();

		int c = causes.size();
		if (c == 1) {
			b.append("Exception caught: ");
		} else {
			b.append(c).append(" exceptions caught: ");
		}

		boolean first = true;
		for (Throwable t : causes) {
			if (first) {
				first = false;
			} else {
				b.append("; ");
			}
			b.append(t.getMessage());
		}

		return b.toString();
	}

	protected static Throwable createThrowable(Set<Throwable> causes) {
		if (causes.isEmpty()) {
			return null;
		}
		return causes.iterator().next();
	}

	private final Set<Throwable> causes;

	public EventBusException(Set<Throwable> causes) {
		super(createMessage(causes), createThrowable(causes));
		this.causes = causes;
	}

	public Set<Throwable> getCauses() {
		return causes;
	}

}
