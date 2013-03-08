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

package org.jboss.tools.web.pagereloader.internal.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.tools.web.pagereloader.internal.util.Logger;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadWebServer {

	private static final int LIVE_RELOAD_PORT = 35729;

	private final LiveReloadCommandBroadcaster broadcaster;

	private final LiveReloadWebServerRunnable liveReloadWebServerRunnable;
	
	public LiveReloadWebServer() throws UnknownHostException {
		this.broadcaster = new LiveReloadCommandBroadcaster(8081, 8080);
		this.liveReloadWebServerRunnable = new LiveReloadWebServerRunnable(8081,
				LIVE_RELOAD_PORT, broadcaster);
	}

	public void start() {
		Thread serverThread = new Thread(liveReloadWebServerRunnable, "jetty-livereload");
		serverThread.start();
	}

	public void stop() throws Exception {
		liveReloadWebServerRunnable.stop();
	}
	
	public void notifyResourceChange(final String path) {
		this.broadcaster.notifyResourceChange(path);
	}

	class LiveReloadWebServerRunnable implements Runnable {

		private final Server server;
		
		private int proxyConnectorPort = -1;

		private int websocketConnectorPort = -1;
		
		private final LiveReloadCommandBroadcaster liveReloadCommandBroadcaster;

		public LiveReloadWebServerRunnable(final int proxyConnectorPort, final int websocketConnectorPort,
				final LiveReloadCommandBroadcaster liveReloadCommandBroadcaster) throws UnknownHostException {
			this.server = new Server();
			this.proxyConnectorPort = proxyConnectorPort;
			this.websocketConnectorPort = websocketConnectorPort;
			this.liveReloadCommandBroadcaster = liveReloadCommandBroadcaster;
		}

		@Override
		public void run() {
			try {
				// connectors
				final String hostName = InetAddress.getLocalHost().getHostAddress();
				// TODO include SSL Connector
				final SelectChannelConnector wsConnector = new SelectChannelConnector();
				wsConnector.setPort(websocketConnectorPort);
				wsConnector.setMaxIdleTime(0);
				final SelectChannelConnector proxyConnector = new SelectChannelConnector();
				proxyConnector.setPort(proxyConnectorPort);
				proxyConnector.setMaxIdleTime(0);
				server.setConnectors(new Connector[] { wsConnector, proxyConnector });
				
				final HandlerCollection handlers = new HandlerCollection();
				server.setHandler(handlers);
				
				final ServletContextHandler liveReloadContext = new ServletContextHandler(handlers, "/",
						ServletContextHandler.NO_SESSIONS);
				
				// Livereload specific content
				liveReloadContext.addServlet(
						new ServletHolder(new LiveReloadWebSocketServlet(liveReloadCommandBroadcaster)), "/livereload");
				liveReloadContext.addFilter(new FilterHolder(new LiveReloadScriptFileFilter()), "/livereload/livereload.js", null);
				
				
				// Handling all applications behind the proxy
				liveReloadContext.addServlet(new ServletHolder(LiveReloadProxyServlet.class), "/");
				liveReloadContext.addFilter(new FilterHolder(new LiveReloadScriptInjectionFilter(hostName, LIVE_RELOAD_PORT)), "/*", null);
				Logger.debug("Starting LiveReload Websocket Server...");
				server.start();
				server.join();
			} catch (Exception e) {
				Logger.error("Failed to start embedded jetty server to provide support for LiveReload", e);
			}
		}

		public void stop() throws Exception {
			if (server != null) {
				server.stop();
			}
		}
	}

}
