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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import org.jboss.tools.web.pagereloader.internal.remote.http.HttpClient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadBrowserTabSelectionWizardModel {

	/**
	 * The Browser URL property key, used by databinding. Must match some
	 * JavaBean in this model class.
	 */
	public static final String PROPERTY_BROWSER_URL = "browserUrl";

	/** The Browser URL. */
	private String browserUrl = "http://localhost:9222";

	/**
	 * The selected Browser Tab property key, used by databinding. Must match some
	 * JavaBean in this model class.
	 */
	public static final String PROPERTY_SELECTED_BROWSER_TAB = "selectedBrowserTab";

	private BrowserTab selectedBrowserTab = null;
	
	/**
	 * The Browser URL property key, used by databinding. Must match some
	 * JavaBean in this model class.
	 */
	public static final String PROPERTY_BROWSER_TABS = "browserTabs";

	/** The Browser URL. */
	private List<BrowserTab> browserTabs;

	/** Property change support. */
	private PropertyChangeSupport propertyChangeSupport;

	/**
	 * Default constructor
	 */
	public LiveReloadBrowserTabSelectionWizardModel() {
		this.propertyChangeSupport = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		if (!contains(listener)) {
			propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (!contains(listener)) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

    protected boolean contains(PropertyChangeListener listener) {
    	boolean contains = false;
    	for (PropertyChangeListener registeredListener : propertyChangeSupport.getPropertyChangeListeners()) {
    		if (registeredListener == listener) {
    			contains = true;
    			break;
    		}
    	}
    	return contains;
    }
    
	protected PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}
	

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

    public String getBrowserUrl() {
		return browserUrl;
	}

	public void setBrowserUrl(String browserUrl) {
		this.browserUrl = browserUrl;
	}

	public List<BrowserTab> getBrowserTabs() {
		return browserTabs;
	}

	public void setBrowserTabs(List<BrowserTab> browserTabs) {
		this.browserTabs = browserTabs;
		firePropertyChange(PROPERTY_BROWSER_TABS, this.browserTabs, this.browserTabs = browserTabs);
	}
	
	public void loadTabs() throws JsonParseException, JsonMappingException, IOException, URISyntaxException {

		// operation
		final HttpClient httpClient = new HttpClient(new URI(browserUrl + "/json"));
		final String responseBody = httpClient.get();
		// parse the JSON response
		ObjectMapper mapper = new ObjectMapper(); // create once, reuse
		// convert into java beans
		List<BrowserTab> tabs = mapper.readValue(responseBody, new TypeReference<List<BrowserTab>>() {
		});
		//remove all tabs with "chrome-extension://" scheme
		for(Iterator<BrowserTab> iterator = tabs.iterator(); iterator.hasNext();) {
			final BrowserTab tab = iterator.next();
			if(tab.getUrl().startsWith("chrome-extension://")) {
				iterator.remove();
			}
		}
		setBrowserTabs(tabs);
	}

	public boolean hasSelection() {
		return selectedBrowserTab != null;
	}

	public BrowserTab getSelectedBrowserTab() {
		return selectedBrowserTab;
	}

	public void setSelectedBrowserTab(BrowserTab selectedBrowserTab) {
		firePropertyChange(PROPERTY_SELECTED_BROWSER_TAB, this.selectedBrowserTab, this.selectedBrowserTab = selectedBrowserTab);
	}
}
