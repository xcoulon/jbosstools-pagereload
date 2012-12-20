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

package org.jboss.tools.web.pagereloader.internal.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.jboss.tools.web.pagereloader.internal.remote.http.HttpClient;
import org.jboss.tools.web.pagereloader.internal.remote.websocketx.WebSocketClient;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author xcoulon
 *
 */
public class ChromeRemoteDebuggingProtocolTestCase {

	@Test
	public void shouldCommunicateWithLocalChromeBrowser() throws URISyntaxException, JsonParseException, JsonMappingException, IOException {
		// operation
		final HttpClient httpClient = new HttpClient(new URI("http://localhost:9222/json"));
		final String responseBody = httpClient.get();
		// verification
		assertNotNull("No response", responseBody);
		// parse the JSON response
		ObjectMapper mapper = new ObjectMapper(); // create once, reuse
		// operation
		List<BrowserTab> tabs = mapper.readValue(responseBody, new TypeReference<List<BrowserTab>>() {});
		assertTrue(tabs.size() > 0);
	}
	
	@Test
	public void shouldConnectToWebsocketEndpoint() throws Exception {
		String websocketDebuggingUrl = "ws://localhost:9222/devtools/page/15_1";
		WebSocketClient client = new WebSocketClient(new URI(websocketDebuggingUrl));
		HashMap<String, Object> request = new HashMap<String, Object>();
		request.put("id", Math.round(1000));
		request.put("method", "Page.reload");
		HashMap<String, Object> params = new HashMap<String, Object>();
		request.put("params", params);
		//params.put("ignoreCache", true);
		ObjectMapper mapper = new ObjectMapper(); // create once, reuse
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, request);
		writer.flush();
		final String message = writer.getBuffer().toString();
		client.open();
		client.sendMessage(message);
		client.close();
	}
	
}
