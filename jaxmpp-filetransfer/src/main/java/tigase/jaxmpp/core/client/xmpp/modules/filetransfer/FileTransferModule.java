/*
 * FileTransferModule.java
 *
 * Tigase XMPP Client Library
 * Copyright (C) 2004-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
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
package tigase.jaxmpp.core.client.xmpp.modules.filetransfer;

import tigase.jaxmpp.core.client.AsyncCallback;
import tigase.jaxmpp.core.client.Context;
import tigase.jaxmpp.core.client.SessionObject;
import tigase.jaxmpp.core.client.XmppModule;
import tigase.jaxmpp.core.client.criteria.Criteria;
import tigase.jaxmpp.core.client.criteria.ElementCriteria;
import tigase.jaxmpp.core.client.eventbus.EventHandler;
import tigase.jaxmpp.core.client.eventbus.JaxmppEvent;
import tigase.jaxmpp.core.client.exceptions.JaxmppException;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.ElementFactory;
import tigase.jaxmpp.core.client.xmpp.modules.filetransfer.FileTransferModule.FileTransferRequestHandler.FileTransferRequestEvent;
import tigase.jaxmpp.core.client.xmpp.stanzas.IQ;
import tigase.jaxmpp.core.client.xmpp.stanzas.Stanza;
import tigase.jaxmpp.core.client.xmpp.stanzas.StanzaType;

import java.util.ArrayList;
import java.util.List;

public class FileTransferModule
		implements XmppModule {

	public static final String XMLNS_SI = "http://jabber." + "org/protocol/si";
	public static final String XMLNS_SI_FILE = "http://jabber.org/protocol/si/profile/file-transfer";
	private static final Criteria CRIT = ElementCriteria.name("iq")
			.add(ElementCriteria.name("si", new String[]{"xmlns", "profile"}, new String[]{XMLNS_SI, XMLNS_SI_FILE}));
	private static final String[] FEATURES = new String[]{XMLNS_SI, XMLNS_SI_FILE};
	private Context context;

	public FileTransferModule(Context context) {
		this.context = context;
	}

	public void acceptStreamInitiation(FileTransfer ft, String id, String streamMethod) throws JaxmppException {
		Element iq = ElementFactory.create("iq");
		iq.setAttribute("type", "result");
		iq.setAttribute("to", ft.getPeer().toString());
		iq.setAttribute("id", id);

		Element si = ElementFactory.create("si", null, XMLNS_SI);
		iq.addChild(si);

		Element feature = ElementFactory.create("feature", null, "http://jabber.org/protocol/feature-neg");
		si.addChild(feature);

		Element x = ElementFactory.create("x", null, "jabber:x:data");
		x.setAttribute("type", "submit");
		feature.addChild(x);

		Element field = ElementFactory.create("field");
		field.setAttribute("var", "stream-method");
		x.addChild(field);

		Element value = ElementFactory.create("value", streamMethod, null);
		field.addChild(value);

		context.getWriter().write(iq);
	}

	public void addFileTransferRequestHandler(FileTransferRequestHandler handler) {
		context.getEventBus().addHandler(FileTransferRequestHandler.FileTransferRequestEvent.class, handler);
	}

	@Override
	public Criteria getCriteria() {
		return CRIT;
	}

	@Override
	public String[] getFeatures() {
		return FEATURES;
	}

	@Override
	public void process(Element element) throws JaxmppException {
		final IQ iq = element instanceof Stanza ? (IQ) element : (IQ) Stanza.create(element);
		process(iq);
	}

	public void process(IQ iq) throws JaxmppException {
		Element query = iq.getChildrenNS("si", XMLNS_SI);
		if (query != null) {
			processStreamInitiationRequest(iq);
		}
	}

	private void processStreamInitiationRequest(IQ iq) throws JaxmppException {
		if (iq.getType() != StanzaType.set) {
			return;
		}

		Element si = iq.getChildrenNS("si", XMLNS_SI);
		Element file = si.getChildrenNS("file", XMLNS_SI_FILE);
		if (file == null) {
			return;
		}

		Element feature = si.getChildrenNS("feature", "http://jabber.org/protocol/feature-neg");
		if (feature == null) {
			returnErrorBadRequest(iq);
			return;
		}
		Element x = feature.getChildrenNS("x", "jabber:x:data");
		if (x == null) {
			returnErrorBadRequest(iq);
			return;
		}
		Element field = x.getFirstChild();
		if (field == null) {
			returnErrorBadRequest(iq);
			return;
		}
		List<String> streamMethods = new ArrayList<String>();
		List<Element> options = field.getChildren("option");
		if (options != null) {
			for (Element option : options) {
				Element value = option.getFirstChild();
				if (value != null) {
					if (value.getValue() != null) {
						streamMethods.add(value.getValue());
					}
				}
			}
		}

		Long filesize = null;
		if (file.getAttribute("size") != null) {
			filesize = Long.parseLong(file.getAttribute("size"));
		}

		FileTransfer ft = new FileTransfer(context.getSessionObject(), iq.getFrom(), si.getAttribute("id"));
		ft.setFileInfo(file.getAttribute("name"), filesize, null, si.getAttribute("mimetype"));

		FileTransferRequestEvent event = new FileTransferRequestEvent(context.getSessionObject(), ft,
																	  iq.getAttribute("id"), streamMethods);
		context.getEventBus().fire(event);
	}

	public void rejectStreamInitiation(FileTransfer ft, String id) throws JaxmppException {
		returnError(ft.getPeer().toString(), id, "cancel", new String[]{"forbidden"},
					new String[]{"urn:ietf:params:xml:ns:xmpp-stanzas"});
	}

	public void removeFileTransferRequestHandler(FileTransferRequestHandler handler) {
		context.getEventBus().remove(FileTransferRequestHandler.FileTransferRequestEvent.class, handler);
	}

	private void returnError(String to, String id, String type, String[] names, String[] xmlnss)
			throws JaxmppException {
		Element result = ElementFactory.create("iq");
		result.setAttribute("id", id);
		result.setAttribute("to", to);
		result.setAttribute("type", "error");

		Element error = ElementFactory.create("error");
		error.setAttribute("type", type);
		for (int i = 0; i < names.length; i++) {
			Element err = ElementFactory.create(names[i], null, xmlnss[i]);
			error.addChild(err);
		}
		result.addChild(error);
		context.getWriter().write(result);
	}

	private void returnErrorBadRequest(IQ iq) throws JaxmppException {
		returnError(iq.getAttribute("from"), iq.getAttribute("id"), "cancel", new String[]{"bad-request"},
					new String[]{"urn:ietf:params:xml:ns:xmpp-stanzas"});
	}

	public void sendNoValidStreams(FileTransferRequestEvent be) throws JaxmppException {
		returnError(be.getFileTransfer().getPeer().toString(), be.getId(), "cancel",
					new String[]{"bad-request", "no-valid-streams"},
					new String[]{"urn:ietf:params:xml:ns:xmpp-stanzas", XMLNS_SI});
	}

	public void sendStreamInitiationOffer(FileTransfer ft, String[] streamMethods, AsyncCallback callback)
			throws JaxmppException {
		IQ iq = IQ.create();
		iq.setTo(ft.getPeer());
		iq.setType(StanzaType.set);

		Element si = ElementFactory.create("si", null, XMLNS_SI);
		si.setAttribute("profile", XMLNS_SI_FILE);
		String sid = ft.getSid();
		si.setAttribute("id", sid);
		// if (callback != null) {
		// callback.setSid(sid);
		// }

		if (ft.getFileMimeType() != null) {
			si.setAttribute("mime-type", ft.getFileMimeType());
		}

		iq.addChild(si);

		Element file = ElementFactory.create("file", null, XMLNS_SI_FILE);
		file.setAttribute("name", ft.getFilename());
		file.setAttribute("size", String.valueOf(ft.getFileSize()));
		si.addChild(file);

		Element feature = ElementFactory.create("feature", null, "http://jabber.org/protocol/feature-neg");
		si.addChild(feature);
		Element x = ElementFactory.create("x", null, "jabber:x:data");
		x.setAttribute("type", "form");
		feature.addChild(x);
		Element field = ElementFactory.create("field");
		field.setAttribute("var", "stream-method");
		field.setAttribute("type", "list-single");
		x.addChild(field);
		for (String streamMethod : streamMethods) {
			Element option = ElementFactory.create("option");
			field.addChild(option);
			Element value = ElementFactory.create("value", streamMethod, null);
			option.addChild(value);
		}

		context.getWriter().write(iq, (long) (10 * 60 * 1000), callback);
	}

	public interface FileTransferRequestHandler
			extends EventHandler {

		void onFileTransferRequest(SessionObject sessionObject, FileTransfer fileTransfer, String id,
								   List<String> streamMethods);

		class FileTransferRequestEvent
				extends JaxmppEvent<FileTransferRequestHandler> {

			private FileTransfer fileTransfer;

			private String id;

			private List<String> streamMethods;

			public FileTransferRequestEvent(SessionObject sessionObject, FileTransfer ft, String id,
											List<String> streamMethods) {
				super(sessionObject);
				this.fileTransfer = ft;
				this.id = id;
				this.streamMethods = streamMethods;
			}

			@Override
			public void dispatch(FileTransferRequestHandler handler) {
				handler.onFileTransferRequest(sessionObject, fileTransfer, id, streamMethods);
			}

			public FileTransfer getFileTransfer() {
				return fileTransfer;
			}

			public void setFileTransfer(FileTransfer fileTransfer) {
				this.fileTransfer = fileTransfer;
			}

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public List<String> getStreamMethods() {
				return streamMethods;
			}

			public void setStreamMethods(List<String> streamMethods) {
				this.streamMethods = streamMethods;
			}

		}
	}

}
