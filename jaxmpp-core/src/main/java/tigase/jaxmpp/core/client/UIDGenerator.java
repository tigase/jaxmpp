/*
 * Tigase XMPP Client Library
 * Copyright (C) 2006-2014 Tigase, Inc.
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

	private static final class UIDGenerator35 extends UIDGenerator {

		private int[] k1 = new int[32];

		private long l = 5;

		private int[] v = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0 };

		public UIDGenerator35() {
			for (int i = 0; i < k1.length; i++) {
				k1[i] = ((int) (Math.random() * 6173)) % k1.length;
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
		protected String nextUID() {
			inc(0);

			int iv = (int) (Math.random() * 6173) % ELEMENTS.length();
			String t = "" + ELEMENTS.charAt(iv);
			int b = (1 + k1[(iv) % k1.length]) % ELEMENTS.length();
			for (int i = 0; i < l; i++) {
				int a = v[i];

				a = (a + b) % ELEMENTS.length();
				t += ELEMENTS.charAt(a);

				b = (b + a + k1[(iv + i) % k1.length]) % ELEMENTS.length();

			}
			return t;
		}
	}

	private static final String ELEMENTS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private final static UIDGenerator generator = new UIDGenerator35();

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			System.out.println(next());
		}
	}

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