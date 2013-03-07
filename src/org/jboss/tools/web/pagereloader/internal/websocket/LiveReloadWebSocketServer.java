/******************************************************************************* 
 * Copyright (c) 2008 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Xavier Coulon - Initial API and implementation 
 ******************************************************************************/

package org.jboss.tools.web.pagereloader.internal.websocket;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.jboss.tools.web.pagereloader.internal.util.Logger;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadWebSocketServer {

	private final int port = 35729;


	private Server server = null;

	private LiveReloadWebSocketHandler liveReloadHandler;

	public void start() {
		final Runnable  runnable = new Runnable() {
			
			@Override
			public void run() {
				try {
					server = new Server();

					SelectChannelConnector wsConnector = new SelectChannelConnector();
					wsConnector.setPort(port);
					wsConnector.setMaxIdleTime(0);
					server.setConnectors(new Connector[] { wsConnector });
					liveReloadHandler = new LiveReloadWebSocketHandler();
					server.setHandler(liveReloadHandler);
					Logger.debug("Starting LiveReload Websocket Server...");
					server.start();
					server.join();
				} catch (Exception e) {
					Logger.error("Failed to start embedded jetty server to provide support for LiveReload", e);
				}
			}
		};

		Thread thread = new Thread(runnable, "jetty-livereload");
		thread.start();
	}

	public void stop() throws Exception {
		if (server != null) {
			server.stop();
		}
	}

	public void notifyResourceChange(final String path) {
		liveReloadHandler.notifyResourceChange(path);
	}

}
