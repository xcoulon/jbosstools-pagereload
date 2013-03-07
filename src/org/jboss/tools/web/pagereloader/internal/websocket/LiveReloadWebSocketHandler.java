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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.jboss.tools.web.pagereloader.internal.util.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author xcoulon
 *
 */
public class LiveReloadWebSocketHandler extends WebSocketHandler {

	private final Set<LiveReloadSocket> members = new HashSet<LiveReloadSocket>();
	
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new LiveReloadSocket();
	}
	
	public void notifyResourceChange(final String path) {
		try {
			String cmd = buildRefreshCommand(path);
			for(LiveReloadSocket member : members) {
				if(member.isOpen()) {
					member.sendMessage(cmd);
				}
			}
		} catch (Exception e) {
			Logger.error("Failed to notify browser(s)", e);
		}
	}
	
	private static String buildRefreshCommand(String path) throws JsonGenerationException, JsonMappingException, IOException {
		List<Object> command = new ArrayList<Object>();
		Map<String, Object> refreshArgs = new HashMap<String, Object>();
		command.add("refresh");
		refreshArgs.put("path", path);
		refreshArgs.put("apply_js_live", true);
		refreshArgs.put("apply_css_live", true);
		command.add(refreshArgs);
		StringWriter commandWriter = new StringWriter();
		objectMapper.writeValue(commandWriter, command);
		String cmd = commandWriter.toString();
		return cmd;
	}

	class LiveReloadSocket implements WebSocket.OnTextMessage {
		private Connection connection;

		@Override
		public void onOpen(Connection connection) {
			members.add(this);
			this.connection = connection;
			try {
				connection.sendMessage("!!ver:1.6");
				Logger.debug("A new LiveReload client established a connection. Now serving {} client{}.", members.size(), (members.size() > 1 ? "s" : ""));
			} catch (IOException e) {
				Logger.error("LiveReload client connection failed", e);
			}
		}

		public boolean isOpen() {
			return connection.isOpen();
		}

		@Override
		public void onClose(int closeCode, String message) {
			members.remove(this);
			Logger.debug("A LiveReload client closed its connection. Now serving {} client{}.", members.size(), (members.size() > 1 ? "s" : ""));
		}

		public void sendMessage(String data) throws IOException {
			connection.sendMessage(data);
		}

		@Override
		public void onMessage(String data) {
			//System.out.println("Received: " + data);
		}


	}

}
