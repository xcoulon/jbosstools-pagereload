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

/**
 * @author xcoulon
 *
 */
public class BrowserTab {
	
	private String devtoolsFrontendUrl;

	private String faviconUrl;

	private String thumbnailUrl;

	private String title;
	
	private String url;
	
	private String webSocketDebuggerUrl;

	/**
	 * @return the devtoolsFrontendUrl
	 */
	public String getDevtoolsFrontendUrl() {
		return devtoolsFrontendUrl;
	}

	/**
	 * @param devtoolsFrontendUrl the devtoolsFrontendUrl to set
	 */
	public void setDevtoolsFrontendUrl(String devtoolsFrontendUrl) {
		this.devtoolsFrontendUrl = devtoolsFrontendUrl;
	}

	/**
	 * @return the faviconUrl
	 */
	public String getFaviconUrl() {
		return faviconUrl;
	}

	/**
	 * @param faviconUrl the faviconUrl to set
	 */
	public void setFaviconUrl(String faviconUrl) {
		this.faviconUrl = faviconUrl;
	}

	/**
	 * @return the thumbnailUrl
	 */
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	/**
	 * @param thumbnailUrl the thumbnailUrl to set
	 */
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the webSocketDebuggerUrl
	 */
	public String getWebSocketDebuggerUrl() {
		return webSocketDebuggerUrl;
	}

	/**
	 * @param webSocketDebuggerUrl the webSocketDebuggerUrl to set
	 */
	public void setWebSocketDebuggerUrl(String webSocketDebuggerUrl) {
		this.webSocketDebuggerUrl = webSocketDebuggerUrl;
	}
	

}
