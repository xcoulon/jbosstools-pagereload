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

import org.eclipse.jface.wizard.Wizard;

/**
 * @author xcoulon
 *
 */
public class LiveReloadBrowserTabSelectionWizard extends Wizard {

	private final LiveReloadBrowserTabSelectionWizardModel wizardModel = new LiveReloadBrowserTabSelectionWizardModel();
	
	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		addPage(new LiveReloadBrowserTabSelectionWizardPage(wizardModel, this));
	}
	
	public BrowserTab getSelectedBrowserTab() {
		return wizardModel.getSelectedBrowserTab();
	}

}
