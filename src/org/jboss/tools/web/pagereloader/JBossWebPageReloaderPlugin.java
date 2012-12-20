package org.jboss.tools.web.pagereloader;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.web.pagereloader.internal.util.ImageRepository;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class JBossWebPageReloaderPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.web.pagereloader"; //$NON-NLS-1$

	private static final String ICONS_FOLDER = "icons";

	private ImageRepository imageRepository = null;

	// The shared instance
	private static JBossWebPageReloaderPlugin plugin;
	
	/**
	 * The constructor
	 */
	public JBossWebPageReloaderPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		imageRepository = new ImageRepository(ICONS_FOLDER, getDefault(), getDefault().getImageRegistry());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static JBossWebPageReloaderPlugin getDefault() {
		return plugin;
	}

	public ImageDescriptor getImageDescriptor(String imageName) {
		return imageRepository.getImageDescriptor(imageName);
	}
}
