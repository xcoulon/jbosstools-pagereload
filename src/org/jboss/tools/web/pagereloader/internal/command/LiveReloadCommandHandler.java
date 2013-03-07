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
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.wst.server.core.IServer;
import org.jboss.tools.web.pagereloader.internal.listener.WebResourceChangeListener;

/**
 * @author xcoulon
 * 
 */
public class LiveReloadCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Command command = event.getCommand();
		boolean alreadyEnabled = HandlerUtil.toggleCommandState(command);
		final IServer server = ServerUtils.getSelectedServer();
		if (alreadyEnabled) {
			disableLiveReload(server);
		} else {
			enableLiveReload(server);
		}
		// must return null
		return null;
	}

	/**
	 * @param server
	 */
	private void enableLiveReload(final IServer server) {
		if (server != null) {
			WebResourceChangeListener.enableLiveReload(server);
		}
	}

	/**
	 * @param server
	 */
	private void disableLiveReload(final IServer server) {
		if (server != null) {
			WebResourceChangeListener.disableLiveReload(server);
		}
	}

}
