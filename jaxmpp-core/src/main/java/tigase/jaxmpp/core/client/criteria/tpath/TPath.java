package tigase.jaxmpp.core.client.criteria.tpath;

import java.util.ArrayList;

import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;

public class TPath {

	public TPathExpression compile(final String path) {
		String[] tokens = path.split("/");
		Node rootNode = null;
		Node lastNode = null;
		for (String string : tokens) {
			if (rootNode == null && string.isEmpty())
				continue;
			Object n = createNode(string);
			if (n instanceof Node) {
				if (rootNode == null)
					rootNode = (Node) n;

				if (lastNode != null) {
					lastNode.setSubnode((Node) n);
				}

				lastNode = (Node) n;
			} else if (n instanceof Function) {
				lastNode.setFunction((Function) n);
			}
		}

		return new TPathExpression(rootNode);
	}

	private Node createConditionNode(String item) {
		String[] tkns = item.split("[\\[\\]]");
		String name = null;
		ArrayList<String> pNames = new ArrayList<String>();
		ArrayList<String> pVals = new ArrayList<String>();
		for (int i = 0; i < tkns.length; i++) {
			String l = tkns[i];
			if (i == 0) {
				name = l;
			} else if (l.startsWith("@")) {
				String[] x = l.split("=");
				// XXX
				String pName = x[0].substring(1);
				String pVal = x[1].replace('\'', ' ').trim();

				pNames.add(pName);
				pVals.add(pVal);
			}

		}

		final String fName = name.equals("*") ? null : name;

		Criteria criteria;
		if (pNames.isEmpty()) {
			criteria = ElementCriteria.name(fName);
		} else {
			criteria = ElementCriteria.name(fName, pNames.toArray(new String[] {}), pVals.toArray(new String[] {}));
		}

		Node n = new Node(criteria, null);

		return n;
	}

	private Function createFunction(final String f) {
		String[] tk = f.split("[\\(\\)]");

		final String fname = tk[0].trim();
		final String fp = tk.length > 1 ? tk[1].replace('\'', ' ').trim() : null;

		if ("value".equals(fname)) {
			return new Function.Value();
		} else if ("attr".equals(fname)) {
			return new Function.Attr(fp);
		} else
			throw new RuntimeException("Unkown function '" + fname + "'");
	}

	private Object createNode(final String string) {
		if (string.endsWith(")")) {
			return createFunction(string);
		} else
			return createConditionNode(string);
	}
}
