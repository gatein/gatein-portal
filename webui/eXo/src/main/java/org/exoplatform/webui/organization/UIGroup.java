package org.exoplatform.webui.organization;

import java.io.Serializable;

import org.exoplatform.services.organization.Group;
import org.gatein.common.text.EntityEncoder;

public class UIGroup implements Serializable {

	private Group group;
	
	public UIGroup(Group group)
	{
		this.group = group;
	}
	
	public String getEncodedLabel()
	{
		EntityEncoder encoder = EntityEncoder.FULL;
		return encoder.encode(getLabel());
	}
	
	public String getLabel()
	{
		return group.getLabel();
	}

	public String getId()
	{
		return group.getId();
	}
}
