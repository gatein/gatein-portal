package org.exoplatform.webui.organization;

import org.exoplatform.services.organization.Group;
import org.gatein.common.text.EntityEncoder;

public class UIGroup {

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
