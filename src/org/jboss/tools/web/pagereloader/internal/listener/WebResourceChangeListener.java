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

package org.jboss.tools.web.pagereloader.internal.listener;

import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.jboss.tools.web.pagereloader.internal.remote.websocketx.WebSocketClient;
import org.jboss.tools.web.pagereloader.internal.util.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author xcoulon
 * 
 */
public class WebResourceChangeListener implements IResourceChangeListener {

	/** Observed location. */
	private final IPath observedLocation;

	private final WebSocketClient websocketClient;

	private final ObjectMapper mapper = new ObjectMapper(); // create once,
															// reuse

	public static void observeAndNotify(final IPath observedLocation, final String websocketDebuggingUrl) {
		try {
			WebResourceChangeListener listener = new WebResourceChangeListener(observedLocation, websocketDebuggingUrl);
			ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		} catch (Exception e) {
			Logger.error("Failed to register observer for " + observedLocation, e);
		}
	}

	/**
	 * Default constructor
	 * 
	 * @throws Exception
	 */
	private WebResourceChangeListener(final IPath observedLocation, final String websocketDebuggingUrl)
			throws Exception {
		this.observedLocation = observedLocation;
		// register this listener
		websocketClient = new WebSocketClient(new URI(websocketDebuggingUrl));
		websocketClient.open();
	}

	public void close() {
		websocketClient.close();
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * Receives a notification event each time a resource changed. If the
	 * resource is a subresource of the observed location, then the event is
	 * propagated.
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		final IResource resource = findChangedResource(event.getDelta());
		if (observedLocation.isPrefixOf(resource.getFullPath())) {
			try {
				HashMap<String, Object> request = new HashMap<String, Object>();
				request.put("id", Math.round(1000));
				request.put("method", "Page.reload");
				HashMap<String, Object> params = new HashMap<String, Object>();
				request.put("params", params);
				params.put("ignoreCache", true);
				StringWriter writer = new StringWriter();
				mapper.writeValue(writer, request);
				writer.flush();
				final String message = writer.getBuffer().toString();
				websocketClient.sendMessage(message);
			} catch (Exception e) {
				Logger.error("Failed to send Page.Reload command over websocket", e);
			}

		}
	}

	private IResource findChangedResource(IResourceDelta delta) {
		if(delta.getAffectedChildren().length > 0) {
			return findChangedResource(delta.getAffectedChildren()[0]);
		}
		return delta.getResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((observedLocation == null) ? 0 : observedLocation.toPortableString().hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		WebResourceChangeListener other = (WebResourceChangeListener) obj;
		if (observedLocation == null) {
			if (other.observedLocation != null) {
				return false;
			}
		} else if (!observedLocation.toPortableString().equals(other.observedLocation.toPortableString())) {
			return false;
		}
		return true;
	}

}
