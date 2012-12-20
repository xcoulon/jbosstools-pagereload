/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.jboss.tools.web.pagereloader.internal.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.osgi.util.NLS;

/**
 * @author Andre Dietisheim
 */
public class ImageRepository {

	private ImageRegistry imageRegistry;
	private URL baseUrl;
	private Plugin plugin;
	private String imageFolder;

	public ImageRepository(String imageFolder, Plugin plugin, ImageRegistry imageRegistry) {
		this.imageFolder = imageFolder;
		this.plugin = plugin;
		this.imageRegistry = imageRegistry;
		scanImageFolder();
	}

	private void scanImageFolder() {
		URL baseUrl = getBaseUrl();
		if (baseUrl == null) {
			Logger.error("Unable to initialize ImageRepository: no folder available.");
			return;
		}
		final Enumeration<URL> entries = plugin.getBundle().findEntries(imageFolder, "*.*", true);
		while(entries.hasMoreElements()) {
			final URL entry = entries.nextElement();
			entry.getFile();
			create(imageRegistry, entry);
		}

	}

	protected URL getBaseUrl() {
		try {
			if (baseUrl == null) {
				this.baseUrl = new URL(plugin.getBundle().getEntry("/"), imageFolder);
			}
			return baseUrl;
		} catch (MalformedURLException e) {
			Logger.error("Failed to get baseUrl", e);
			return null;
		}
	}

	private ImageDescriptor create(ImageRegistry registry, URL imageUrl) {
		if (imageUrl == null) {
			return null;
		}
		final String name = new Path(imageUrl.getFile()).removeFileExtension().lastSegment();
		final ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(imageUrl);
		registry.put(name, imageDescriptor);
		return imageDescriptor;
	}

	private URL createFileURL(String name, URL baseUrl) {
		try {
			return new URL(baseUrl, name);
		} catch (MalformedURLException e) {
			plugin.getLog().log(
					new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(), NLS.bind(
							"Could not create URL for image {0}", name), e));
			return null;
		}
	}

	public ImageDescriptor getImageDescriptor(String name) {
		return imageRegistry.getDescriptor(name);
	}
}
