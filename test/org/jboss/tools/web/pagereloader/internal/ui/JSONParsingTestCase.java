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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author xcoulon
 *
 */
public class JSONParsingTestCase {
	
	@Test
	public void shouldParseJsonResponse() throws JsonParseException, JsonMappingException, IOException {
		// pre-condition
		ObjectMapper mapper = new ObjectMapper(); // create once, reuse
		File source = new File("test/" + JSONParsingTestCase.class.getPackage().getName().replaceAll("\\.", "/") + "/tabs.json");
		assertTrue("File not found: " + source.getAbsolutePath(), source.exists());
		// operation
		List<BrowserTab> tabs = mapper.readValue(source, new TypeReference<List<BrowserTab>>() {});
		// verification
		assertEquals("Wrong number of tabs", 3, tabs.size());
		for(BrowserTab tab : tabs) {
			assertNotNull("No title", tab.getTitle());
			assertNotNull("No URL", tab.getUrl());
			assertNotNull("No WS URL", tab.getWebSocketDebuggerUrl());
		}
	}
	
}
