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
package tigase.jaxmpp.core.client.xmpp.modules.vcard;

import java.io.Serializable;
import java.util.List;

import tigase.jaxmpp.core.client.xml.DefaultElement;
import tigase.jaxmpp.core.client.xml.Element;
import tigase.jaxmpp.core.client.xml.XMLException;

public class VCard implements Serializable {

	private static final long serialVersionUID = 1L;

	private static void add(Element vcard, String name, String value) throws XMLException {
		if (value != null)
			vcard.addChild(new DefaultElement(name, value, null));
	}

	private static void add(Element vcard, String name, String[] childNames, String[] values) throws XMLException {
		Element x = new DefaultElement(name);
		vcard.addChild(x);

		for (int i = 0; i < childNames.length; i++) {
			x.addChild(new DefaultElement(childNames[i], values[i], null));
		}

	}

	private static String getChildValue(Element it, String string) throws XMLException {
		List<Element> l = it.getChildren(string);
		if (l == null || l.size() == 0)
			return null;
		return l.get(0).getValue();
	}

	private static boolean match(final Element it, final String elemName, final String... children) throws XMLException {
		if (!elemName.equals(it.getName()))
			return false;

		for (String string : children) {
			List<Element> l = it.getChildren(string);
			if (l == null || l.size() == 0)
				return false;
		}

		return true;
	}

	private String bday;
	private String description;
	private String fullName;
	private String homeAddressCtry;
	private String homeAddressLocality;

	private String homeAddressPCode;

	private String homeAddressRegion;

	private String homeAddressStreet;
	private String homeEmail;
	private String homeTelFax;
	private String homeTelMsg;

	private String homeTelVoice;
	private String jabberID;
	private String nameFamily;
	private String nameGiven;
	private String nameMiddle;
	private String nickName;
	private String orgName;
	private String orgUnit;
	private String photoType;

	private String photoVal;
	private String role;
	private String title;
	private String url;
	private String workAddressCtry;
	private String workAddressLocality;
	private String workAddressPCode;
	private String workAddressRegion;
	private String workAddressStreet;

	private String workEmail;
	private String workTelFax;

	private String workTelMsg;

	private String workTelVoice;

	public String getBday() {
		return bday;
	}

	public String getDescription() {
		return description;
	}

	public String getFullName() {
		return fullName;
	}

	public String getHomeAddressCtry() {
		return homeAddressCtry;
	}

	public String getHomeAddressLocality() {
		return homeAddressLocality;
	}

	public String getHomeAddressPCode() {
		return homeAddressPCode;
	}

	public String getHomeAddressRegion() {
		return homeAddressRegion;
	}

	public String getHomeAddressStreet() {
		return homeAddressStreet;
	}

	public String getHomeEmail() {
		return homeEmail;
	}

	public String getHomeTelFax() {
		return homeTelFax;
	}

	public String getHomeTelMsg() {
		return homeTelMsg;
	}

	public String getHomeTelVoice() {
		return homeTelVoice;
	}

	public String getJabberID() {
		return jabberID;
	}

	public String getNameFamily() {
		return nameFamily;
	}

	public String getNameGiven() {
		return nameGiven;
	}

	public String getNameMiddle() {
		return nameMiddle;
	}

	public String getNickName() {
		return nickName;
	}

	public String getOrgName() {
		return orgName;
	}

	public String getOrgUnit() {
		return orgUnit;
	}

	public String getPhotoType() {
		return photoType;
	}

	public String getPhotoVal() {
		return photoVal;
	}

	public String getRole() {
		return role;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getWorkAddressCtry() {
		return workAddressCtry;
	}

	public String getWorkAddressLocality() {
		return workAddressLocality;
	}

	public String getWorkAddressPCode() {
		return workAddressPCode;
	}

	public String getWorkAddressRegion() {
		return workAddressRegion;
	}

	public String getWorkAddressStreet() {
		return workAddressStreet;
	}

	public String getWorkEmail() {
		return workEmail;
	}

	public String getWorkTelFax() {
		return workTelFax;
	}

	public String getWorkTelMsg() {
		return workTelMsg;
	}

	public String getWorkTelVoice() {
		return workTelVoice;
	}

	void loadData(final Element element) throws XMLException {
		if (!element.getName().equals("vCard") || !element.getXMLNS().equals("vcard-temp"))
			throw new RuntimeException("Element isn't correct <vCard xmlns='vcard-temp'> vcard element");
		for (final Element it : element.getChildren()) {
			if (match(it, "EMAIL", "WORK")) {
				this.workEmail = getChildValue(it, "USERID");
			} else if (match(it, "EMAIL", "HOME")) {
				this.homeEmail = getChildValue(it, "USERID");
			} else if (match(it, "ADR", "HOME")) {
				for (Element e : it.getChildren()) {
					if ("STREET".equals(e.getName())) {
						this.homeAddressStreet = e.getValue();
					} else if ("LOCALITY".equals(e.getName())) {
						this.homeAddressLocality = e.getValue();
					} else if ("REGION".equals(e.getName())) {
						this.homeAddressRegion = e.getValue();
					} else if ("PCODE".equals(e.getName())) {
						this.homeAddressPCode = e.getValue();
					} else if ("CTRY".equals(e.getName())) {
						this.homeAddressCtry = e.getValue();
					}
				}
			} else if (match(it, "ADR", "WORK")) {
				for (Element e : it.getChildren()) {
					if ("STREET".equals(e.getName())) {
						this.workAddressStreet = e.getValue();
					} else if ("LOCALITY".equals(e.getName())) {
						this.workAddressLocality = e.getValue();
					} else if ("REGION".equals(e.getName())) {
						this.workAddressRegion = e.getValue();
					} else if ("PCODE".equals(e.getName())) {
						this.workAddressPCode = e.getValue();
					} else if ("CTRY".equals(e.getName())) {
						this.workAddressCtry = e.getValue();
					}
				}
			} else if (match(it, "TEL", "WORK", "VOICE")) {
				this.workTelVoice = getChildValue(it, "NUMBER");
			} else if (match(it, "TEL", "WORK", "FAX")) {
				this.workTelFax = getChildValue(it, "NUMBER");
			} else if (match(it, "TEL", "WORK", "MSG")) {
				this.workTelMsg = getChildValue(it, "NUMBER");
			} else if (match(it, "TEL", "HOME", "VOICE")) {
				this.homeTelVoice = getChildValue(it, "NUMBER");
			} else if (match(it, "TEL", "HOME", "FAX")) {
				this.homeTelFax = getChildValue(it, "NUMBER");
			} else if (match(it, "TEL", "HOME", "MSG")) {
				this.homeTelMsg = getChildValue(it, "NUMBER");
			} else if ("FN".equals(it.getName())) {
				this.fullName = it.getValue();
			} else if ("N".equals(it.getName())) {
				for (Element pit : it.getChildren()) {
					if ("FAMILY".equals(pit.getName())) {
						this.nameFamily = pit.getValue();
					} else if ("GIVEN".equals(pit.getName())) {
						this.nameGiven = pit.getValue();
					} else if ("MIDDLE".equals(pit.getName())) {
						this.nameMiddle = pit.getValue();
					}
				}
			} else if ("NICKNAME".equals(it.getName())) {
				this.nickName = it.getValue();
			} else if ("URL".equals(it.getName())) {
				this.url = it.getValue();
			} else if ("BDAY".equals(it.getName())) {
				this.bday = it.getValue();
			} else if ("ORG".equals(it.getName())) {
				for (Element pit : it.getChildren()) {
					if ("ORGNAME".equals(pit.getName())) {
						this.orgName = pit.getValue();
					} else if ("ORGUNIT".equals(pit.getName())) {
						this.orgUnit = pit.getValue();
					}
				}
			} else if ("TITLE".equals(it.getName())) {
				this.title = it.getValue();
			} else if ("ROLE".equals(it.getName())) {
				this.role = it.getValue();
			} else if ("JABBERID".equals(it.getName())) {
				this.jabberID = it.getValue();
			} else if ("DESC".equals(it.getName())) {
				this.description = it.getValue();
			} else if ("PHOTO".equals(it.getName())) {
				for (Element pit : it.getChildren()) {
					if ("TYPE".equals(pit.getName())) {
						this.photoType = pit.getValue();
					} else if ("BINVAL".equals(pit.getName())) {
						this.photoVal = pit.getValue();
						if (this.photoVal != null)
							this.photoVal = this.photoVal.replace("\n", "").trim();
					}
				}
			}

		}
	}

	public Element makeElement() throws XMLException {
		Element vcard = new DefaultElement("vCard", null, "vcard-temp");
		add(vcard, "FN", this.fullName);
		add(vcard, "N", new String[] { "FAMILY", "GIVEN", "MIDDLE" }, new String[] { this.nameFamily, this.nameGiven,
				this.nameMiddle });
		add(vcard, "NICKNAME", this.nickName);
		add(vcard, "URL", this.url);
		add(vcard, "BDAY", this.bday);
		add(vcard, "ORG", new String[] { "ORGNAME", "ORGUNIT" }, new String[] { this.orgName, this.orgUnit });
		add(vcard, "TITLE", this.title);
		add(vcard, "ROLE", this.role);

		add(vcard, "TEL", new String[] { "WORK", "VOICE", "NUMBER" }, new String[] { null, null, this.workTelVoice });
		add(vcard, "TEL", new String[] { "WORK", "FAX", "NUMBER" }, new String[] { null, null, this.workTelFax });
		add(vcard, "TEL", new String[] { "WORK", "MSG", "NUMBER" }, new String[] { null, null, this.workTelMsg });
		add(vcard, "ADR", new String[] { "WORK", "STREET", "LOCALITY", "REGION", "PCODE", "CTRY" }, new String[] { null,
				this.workAddressStreet, this.workAddressLocality, this.workAddressRegion, this.workAddressPCode,
				this.workAddressCtry });

		add(vcard, "TEL", new String[] { "HOME", "VOICE", "NUMBER" }, new String[] { null, null, this.homeTelVoice });
		add(vcard, "TEL", new String[] { "HOME", "FAX", "NUMBER" }, new String[] { null, null, this.homeTelFax });
		add(vcard, "TEL", new String[] { "HOME", "MSG", "NUMBER" }, new String[] { null, null, this.homeTelMsg });
		add(vcard, "ADR", new String[] { "HOME", "STREET", "LOCALITY", "REGION", "PCODE", "CTRY" }, new String[] { null,
				this.homeAddressStreet, this.homeAddressLocality, this.homeAddressRegion, this.homeAddressPCode,
				this.homeAddressCtry });

		add(vcard, "JABBERID", this.jabberID);
		add(vcard, "DESC", this.description);
		add(vcard, "EMAIL", new String[] { "HOME", "USERID" }, new String[] { null, this.homeEmail });

		add(vcard, "EMAIL", new String[] { "WORK", "USERID" }, new String[] { null, this.workEmail });

		add(vcard, "PHOTO", new String[] { "TYPE", "BINVAL" }, new String[] { this.photoType, this.photoVal });

		return vcard;
	}

	public void setBday(String bday) {
		this.bday = bday;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setHomeAddressCtry(String homeAddressCtry) {
		this.homeAddressCtry = homeAddressCtry;
	}

	public void setHomeAddressLocality(String homeAddressLocality) {
		this.homeAddressLocality = homeAddressLocality;
	}

	public void setHomeAddressPCode(String homeAddressPCode) {
		this.homeAddressPCode = homeAddressPCode;
	}

	public void setHomeAddressRegion(String homeAddressRegion) {
		this.homeAddressRegion = homeAddressRegion;
	}

	public void setHomeAddressStreet(String homeAddressStreet) {
		this.homeAddressStreet = homeAddressStreet;
	}

	public void setHomeEmail(String homeEmail) {
		this.homeEmail = homeEmail;
	}

	public void setHomeTelFax(String homeTelFax) {
		this.homeTelFax = homeTelFax;
	}

	public void setHomeTelMsg(String homeTelMsg) {
		this.homeTelMsg = homeTelMsg;
	}

	public void setHomeTelVoice(String homeTelVoice) {
		this.homeTelVoice = homeTelVoice;
	}

	public void setJabberID(String jabberID) {
		this.jabberID = jabberID;
	}

	public void setNameFamily(String nameFamily) {
		this.nameFamily = nameFamily;
	}

	public void setNameGiven(String nameGiven) {
		this.nameGiven = nameGiven;
	}

	public void setNameMiddle(String nameMiddle) {
		this.nameMiddle = nameMiddle;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}

	public void setPhotoType(String photoType) {
		this.photoType = photoType;
	}

	public void setPhotoVal(String photoVal) {
		this.photoVal = photoVal;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setWorkAddressCtry(String workAddressCtry) {
		this.workAddressCtry = workAddressCtry;
	}

	public void setWorkAddressLocality(String workAddressLocality) {
		this.workAddressLocality = workAddressLocality;
	}

	public void setWorkAddressPCode(String workAddressPCode) {
		this.workAddressPCode = workAddressPCode;
	}

	public void setWorkAddressRegion(String workAddressRegion) {
		this.workAddressRegion = workAddressRegion;
	}

	public void setWorkAddressStreet(String workAddressStreet) {
		this.workAddressStreet = workAddressStreet;
	}

	public void setWorkEmail(String workEmail) {
		this.workEmail = workEmail;
	}

	public void setWorkTelFax(String workTelFax) {
		this.workTelFax = workTelFax;
	}

	public void setWorkTelMsg(String workTelMsg) {
		this.workTelMsg = workTelMsg;
	}

	public void setWorkTelVoice(String workTelVoice) {
		this.workTelVoice = workTelVoice;
	}
}