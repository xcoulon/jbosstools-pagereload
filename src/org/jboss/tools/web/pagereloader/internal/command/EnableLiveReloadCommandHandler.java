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

package org.jboss.tools.web.pagereloader.internal.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.web.pagereloader.internal.listener.WebResourceChangeListener;
import org.jboss.tools.web.pagereloader.internal.remote.websocketx.WebSocketServer;

/**
 * @author xcoulon
 * 
 */
public class EnableLiveReloadCommandHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActivePart();
		IStructuredSelection selection = (IStructuredSelection) activePart.getSite().getSelectionProvider()
				.getSelection();
		// IFolder folder = (IFolder) selection.getFirstElement();
		final IServer server = (IServer) (selection.getFirstElement());
		WebResourceChangeListener.enableLiveReload(server);
		WebSocketServer webSocketServer = WebSocketServer.getInstance(true);
		webSocketServer.start();

		// must return null
		return null;
	}

}
