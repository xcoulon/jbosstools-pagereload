/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.tools.web.pagereloader.internal.remote.websocketx;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * A HTTP server which serves Web Socket requests at:
 * 
 * http://localhost:8080/websocket
 * 
 * Open your browser at http://localhost:8080/, then the demo page will be
 * loaded and a Web Socket connection will be made automatically.
 * 
 * This server illustrates support for the different web socket specification
 * versions and will work with:
 * 
 * <ul>
 * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Firefox 11+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * </ul>
 * 
 * @see https://github.com/guard/guard-livereload
 * 
 */
public class WebSocketServer {

	private final int port = 35729;

	// Configure the server.
	private final ServerBootstrap bootstrap;

	private static WebSocketServer instance = null;

	private final WebSocketServerHandler websocketHandler;
	
	public static WebSocketServer getInstance() {
		if(instance == null) {
			instance = new WebSocketServer();
		}
		return instance;
	}
	
	public static WebSocketServer getInstance(final boolean createNewServer) {
		if(createNewServer && instance != null) {
			instance.reset();
		}
		return getInstance();
	}

	/**
	 * Private constructor for the singleton
	 */
	private WebSocketServer() {
		// configure the server
		this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		// Set up the event pipeline factory.
		final WebSocketServerPipelineFactory pipelineFactory = new WebSocketServerPipelineFactory();
		bootstrap.setPipelineFactory(pipelineFactory);
		websocketHandler = pipelineFactory.getWebsocketHandler();
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	public void start() {
		// Bind and start to accept incoming connections.
		bootstrap.bind(new InetSocketAddress(port));
		System.out.println("Web socket server started at port " + port + '.');
	}

	public void reset() {
		websocketHandler.closeChannels();
		WebSocketServer.instance = null;
		System.out.println("Web socket server stopped");
	}
	
	public void notifyResourceChange(final String path) {
		websocketHandler.notifyResourceChange(path);
	}

}
