package tigase.jaxmpp.core.client;

public class UIDGenerator {

	private static final String ELEMENTS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private int[] key = new int[9];

	private int last;

	private final int[] P;

	private long state = 100000l;

	public UIDGenerator() {
		for (int i = 0; i < key.length; i++) {
			int a = ((int) Math.round(Math.random() * 719)) % ELEMENTS.length();
			key[i] = a;
		}
		P = new int[ELEMENTS.length()];
		for (int i = 0; i < ELEMENTS.length(); i++) {
			P[i] = i;
		}
		for (int i = 0; i < ELEMENTS.length() * 3; i++) {
			int a = ((int) Math.round(Math.random() * 821) + last) % ELEMENTS.length();
			last = P[P[((P[(int) ((last + Math.random() * 6173) % P.length)] + 1) % P.length)]];

			swap(i, a);
		}
	}

	public String nextUID() {
		long x = state++;

		last = P[P[(int) ((x + P[(int) ((last + Math.random() * 6173) % P.length)] + 1) % P.length)]];
		String r = "" + ELEMENTS.charAt(last);
		int c = 0;
		while (x > 0) {
			int a = (int) (x % ELEMENTS.length());
			x = x / ELEMENTS.length();

			int m = (key[(c++) % key.length] + a + last);

			r = ELEMENTS.charAt(m % ELEMENTS.length()) + r;
			last = P[P[(c + P[(last) % P.length] + 1 + m) % P.length]];

		}
		return r;
	}

	private void swap(int i, int a) {
		int tmp = P[i % P.length];
		P[i % P.length] = P[a % P.length];
		P[a % P.length] = tmp;
	}

}
