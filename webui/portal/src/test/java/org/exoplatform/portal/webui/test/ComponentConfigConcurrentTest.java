/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.exoplatform.portal.webui.test;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.Event;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for concurrent read of event from UI component configuration.
 * 
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @version $Revision$
 */
public class ComponentConfigConcurrentTest extends AbstractGateInTest 
{
	private static final int WORKERS_COUNT = 50;
	
	private MockApplication mockApplication;
	
	public void testConcurrentReadOfComponentEventConfig() throws Exception 
	{
		// Init configuration and mock WebUI application
		Map<String, String> initParams = new HashMap<String, String>();
		initParams.put("webui.configuration", "webui.configuration");

		String basedir = System.getProperty("basedir");
		String webuiConfig = basedir + "/src/test/resources/webui-configuration.xml";
		Map<String, URL> resources = new HashMap<String, URL>();
		resources.put("webui.configuration", new File(webuiConfig).toURI().toURL());
		initParams.put("webui.configuration", new File(webuiConfig).toURI().toURL().toString());

		mockApplication = new MockApplication(initParams, resources, null);
		mockApplication.onInit();
		
		// init workers list
		List<Worker> workers = new ArrayList<Worker>(WORKERS_COUNT);
		
		// test obtain event configuration concurrently with more worker threads
		for (int i=0 ; i<WORKERS_COUNT ; i++)
		{
			Worker worker = new Worker("Worker-" + i);			
			workers.add(worker);
			worker.start();
		}	
		
		// Wait for all workers to finish
		for (Worker worker : workers)
		{
			worker.join();
		}
		
		// Go throguh all workers and throw error if some worker has null eventConfig
		for (Worker worker : workers)
		{
			assertNotNull("event configuration is null in worker " + worker.getName(), worker.eventConfig);
		}		

		// destroy mock application
		mockApplication.onDestroy();
	}
	
	private class Worker extends Thread
	{
		private Event eventConfig = null;
		
		public Worker(String name)
		{
			super(name);
		}
		
		public void run()
		{			
			try
			{
			   	UIPortal uiPortal = mockApplication.createUIComponent(UIPortal.class, null, null, null);
			   	eventConfig = uiPortal.getComponentConfig().getUIComponentEventConfig("Ping");
				
				// log message now if eventConfig is null, so that we know about all failed workers. Test will be failed later.
				if (eventConfig == null)
				{
					log.error("eventConfig is null for worker " + getName());
				}
			}
			catch (Exception e)
			{
				log.error("Exception occured during concurrent test in worker " + getName(), e);				
			} 
			
		}		
		
	}

}
