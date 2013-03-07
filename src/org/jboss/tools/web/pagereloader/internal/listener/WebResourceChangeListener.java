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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.util.PublishAdapter;
import org.jboss.tools.web.pagereloader.JBossWebPageReloaderPlugin;
import org.jboss.tools.web.pagereloader.internal.util.Logger;
import org.jboss.tools.web.pagereloader.internal.util.WtpUtils;
import org.jboss.tools.web.pagereloader.internal.websocket.LiveReloadWebSocketServer;

/**
 * @author xcoulon
 * 
 */
public class WebResourceChangeListener implements IResourceChangeListener, IServerListener {

	public static final QualifiedName WEB_RESOURCE_CHANGE_LISTENER = new QualifiedName(
			JBossWebPageReloaderPlugin.PLUGIN_ID, "WebResourceChangeListener");

	private final IServer server;
	private Map<IFolder, IModule> webappFolders = new HashMap<IFolder, IModule>();
	private final ArrayBlockingQueue<String> pendingChanges = new ArrayBlockingQueue<String>(1000);
	private final LiveReloadWebSocketServer liveReloadServer;

	public static void enableLiveReload(final IServer server) {
		try {
			final WebResourceChangeListener listener = new WebResourceChangeListener(server);
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
			workspace.getRoot().setSessionProperty(WEB_RESOURCE_CHANGE_LISTENER, listener);
			for (IModule module : server.getModules()) {
				final IProject project = module.getProject();
				final IFolder webappFolder = WtpUtils.getWebappFolder(project);
				listener.watch(module, webappFolder);
			}
			listener.startEmbeddedWebSocketServer();
			server.addServerListener(listener);

		} catch (Exception e) {
			Logger.error("Failed to register observer for " + server, e);
		}
	}

	public static void disableLiveReload(final IServer server) {
		try {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final IWorkspaceRoot workspaceRoot = workspace.getRoot();
			final WebResourceChangeListener webResourceChangeListener = (WebResourceChangeListener) workspaceRoot
					.getSessionProperty(WEB_RESOURCE_CHANGE_LISTENER);
			if (webResourceChangeListener != null) {
				webResourceChangeListener.stopEmbeddedWebSocketServer();
				workspaceRoot.setSessionProperty(WEB_RESOURCE_CHANGE_LISTENER, null);
				// make sure the command state is set to 'false' (unchecked)
				ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
				Command command = service.getCommand("org.jboss.tools.web.pagereloader.liveReloadCommand");
				State state = command.getState("org.eclipse.ui.commands.toggleState");
				state.setValue(false);
				Logger.debug("LiveReload Websocket Server stopped.");
			}
		} catch (Exception e) {
			Logger.error("Failed to register observer for " + server, e);
		}
	}

	/**
	 * Internal Constructor.
	 * 
	 * @param server
	 * @throws Exception
	 */
	private WebResourceChangeListener(IServer server) throws Exception {
		this.server = server;
		liveReloadServer = new LiveReloadWebSocketServer();
		server.addPublishListener(new PageReloadPublishAdapter());
	}

	/**
	 * Adds the given webappFolder from the given module to the list of
	 * resources that must be looked after changes.
	 * 
	 * @param module
	 * @param webappFolder
	 */
	public void watch(final IModule module, final IFolder webappFolder) {
		webappFolders.put(webappFolder, module);
	}

	private void startEmbeddedWebSocketServer() throws Exception {
		liveReloadServer.start();
	}

	private void stopEmbeddedWebSocketServer() throws Exception {
		liveReloadServer.stop();
	}

	/**
	 * Returns true if there is a listener for the given server, false
	 * otherwise.
	 * 
	 * @param server
	 *            the server on which LiveReload may be started
	 * @return true or false
	 * @throws CoreException
	 */
	public static boolean isStarted(IServer server) {
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		try {
			return (workspaceRoot.getSessionProperty(WEB_RESOURCE_CHANGE_LISTENER) != null);
		} catch (CoreException e) {
			Logger.error("Failed to retrieve LiveReload status on selected server", e);
		}
		return false;
	}

	/**
	 * Receives a notification event each time a resource changed. If the
	 * resource is a subresource of the observed location, then the event is
	 * propagated.
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		final IResource resource = findChangedResource(event.getDelta());
		for (Entry<IFolder, IModule> entry : webappFolders.entrySet()) {
			final IFolder webappFolder = entry.getKey();
			final IModule module = entry.getValue();
			if (webappFolder.getFullPath().isPrefixOf(resource.getFullPath())) {
				try {
					final IPath changedPath = resource.getFullPath().makeRelativeTo(webappFolder.getFullPath());
					final String path = "http://" + server.getHost() + ":" + getServerPort() + "/" + module.getName()
							+ "/" + changedPath.toString();
					//System.out.println("Putting '" + path + "' on wait queue until server publish is done.");
					pendingChanges.offer(path);
				} catch (Exception e) {
					Logger.error("Failed to send Page.Reload command over websocket", e);
				}
				break;
			}
		}
	}
	
	/**
	 * Stops the LiveReload WebSocket Server when the JEE Server is stopped
	 */
	@Override
	public void serverChanged(ServerEvent event) {
		if(event.getState() == IServer.STATE_STOPPED) {
			disableLiveReload(event.getServer());
		}
	}

	/**
	 * @return
	 */
	private String getServerPort() {
		return server.getAttribute("org.jboss.ide.eclipse.as.core.server.webPort", "8080");
	}

	private IResource findChangedResource(IResourceDelta delta) {
		if (delta.getAffectedChildren().length > 0) {
			return findChangedResource(delta.getAffectedChildren()[0]);
		}
		return delta.getResource();
	}

	class PageReloadPublishAdapter extends PublishAdapter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.wst.server.core.util.PublishAdapter#publishFinished(org
		 * .eclipse.wst.server.core.IServer, org.eclipse.core.runtime.IStatus)
		 */
		@Override
		public void publishFinished(IServer server, IStatus status) {
			if (!status.isOK()) {
				return;
			}
			try {
				while (!pendingChanges.isEmpty()) {
					String changedPath = pendingChanges.take();
					liveReloadServer.notifyResourceChange(changedPath);
				}
			} catch (Exception e) {
				Logger.error("Failed to send notifications for pending changes", e);
			}
		}

	}

}
