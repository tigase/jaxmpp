/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2012 "Bartosz Małkowski" <bartosz.malkowski@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.jaxmpp.core.client;

/**
 * Unique IDs generator. Used for attribute 'id' in stanzas.
 * 
 * @author bmalkow
 * 
 */
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

	private static final class UIDGenerator3 extends UIDGenerator {

		private int[] k1 = new int[32];

		private int[] k2 = new int[32];

		private long l = 4;

		private int[] v = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

		public UIDGenerator3() {
			for (int i = 0; i < k1.length; i++) {
				k1[i] = k2[i] = i;
			}
			for (int i = 0; i < k1.length; i++) {
				k1[i] = ((int) (Math.random() * 6173)) % k1.length;
			}
			for (int i = k2.length - 1; i >= 0; i--) {
				int a = ((int) (Math.random() * 6173)) % k2.length;
				int tmp = k2[i];
				k2[i] = k2[a];
				k2[a] = tmp;
			}
		}

		private void inc(final int p) {
			if (p >= l)
				++l;
			v[p] = v[p] + 1;
			if (v[p] >= ELEMENTS.length()) {
				v[p] = 0;
				inc(p + 1);
			}
		}

		@Override
		public String nextUID() {
			inc(0);

			int b = (int) (Math.random() * 6173) % ELEMENTS.length();
			String t = "" + ELEMENTS.charAt(b);
			b = k2[b % k2.length] ^ ((b >>> 1) ^ (b << 9));
			b = k1[b % k1.length] ^ ((b >>> 1) ^ (b << 8));
			b = k2[b % k2.length] ^ ((b >>> 1) ^ (b << 7));
			for (int i = 0; i < l; i++) {
				int a = v[i];
				a = (a + k1[Math.abs(b) % k1.length]) % ELEMENTS.length();
				t += ELEMENTS.charAt(a);
				b = k2[a % k2.length] ^ ((b >>> 1) ^ (b << 9));
			}
			return t;
		}
	}

	private static final String ELEMENTS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private final static UIDGenerator generator = new UIDGenerator3();

	/**
	 * Generate next id;
	 * 
	 * @return unique id
	 */
	public static String next() {
		return generator.nextUID();
	}

	protected abstract String nextUID();

}