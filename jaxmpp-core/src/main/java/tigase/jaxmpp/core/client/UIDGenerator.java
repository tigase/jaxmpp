package tigase.jaxmpp.core.client;

public abstract class UIDGenerator {

	private static final class UIDGenerator1 extends UIDGenerator {
		private int[] key = new int[9];

		private int last;

		private final int[] P;

		private long state = 100000l;

		public UIDGenerator1() {
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

		@Override
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

	private static final class UIDGenerator2 extends UIDGenerator {

		private int[] k = { 1, 2, 3, 4, 5, 6, 7, 9, 10, 11 };

		private int[] v = { 0, 1, 2, 3, 4, 5, 6, 7, 9 };

		public UIDGenerator2() {
			for (int i = 0; i < k.length * 5; i++) {
				k[i % k.length] = ((k[i % k.length] + k[(i + 1) % k.length] + i + (int) (Math.random() * 6173)) % ELEMENTS.length());
			}
		}

		@Override
		public String nextUID() {
			String r = "";
			for (int i = 0; i < v.length; i++) {
				int a = v[i % v.length] = (v[(i + 2) % v.length] + v[(i + 1) % v.length] + v[(i + 3) % v.length]
						+ k[i % k.length] + v[(k[(i + 1) % k.length] + 1) % v.length])
						% ELEMENTS.length();
				r += ELEMENTS.charAt(a);
			}

			return r;
		}
	}

	private static final String ELEMENTS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private final static UIDGenerator generator = new UIDGenerator2();

	public static void main(String[] args) {
		for (int i = 0; i < 100000; i++) {
			System.out.println(next());
		}
	}

	public static String next() {
		return generator.nextUID();
	}

	public abstract String nextUID();

}
