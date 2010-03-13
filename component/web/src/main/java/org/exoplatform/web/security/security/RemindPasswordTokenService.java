package org.exoplatform.web.security.security;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.xml.InitParams;

public class RemindPasswordTokenService extends CookieTokenService {

	public RemindPasswordTokenService(InitParams initParams,
			ChromatticManager chromatticManager) {
		
		super(initParams, chromatticManager);
	}
	
	protected String nextTokenId()
	{
	   return "" + random.nextInt();
	}

}
