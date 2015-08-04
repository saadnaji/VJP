/*******************************************************************************
 * Copyright © 2008 Sandro Badame. All Rights Reserved.
 * 
 * This software and the accompanying materials is available under the 
 * Eclipse Public License 1.0 (EPL), which accompanies this distribution, and is
 * available at http://visualjpf.sourceforge.net/epl-v10.html
 ******************************************************************************/
package com.javapathfinder.vjp;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * VJP serves as the main class for the VJP Project. This class serves as a
 * utility to provide access to the logging and file system for this plugin.
 * 
 * @author Sandro Badame
 */
public class VJP extends AbstractUIPlugin {

  /**
   * This is a unique id for the VJP plugin.
   * @see org.eclipse.ui.plugin.AbstractUIPlugin
   */
  public static final String PLUGIN_ID = "com.javapathfinder.vjp";

  /**
   * This is a shared singleton instance of the VJP plugin class.
   */
  private static VJP plugin;
  
  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static VJP getDefault() {
    return plugin;
  }

  /**
   * Convenience method to log any type of message.
   * The code for this log will be Status.OK
   */
  public static void log(int severity, String message, Throwable exception) {
    getDefault().getLog().log(new Status(severity, PLUGIN_ID, Status.OK,
                                         message, exception == null ? new Throwable() : exception));
  }

  /**
   * Convenience method to log plugin information.
   */
  public static void logInfo(String message) {
    log(Status.INFO, message == null ? "null" : message, null);
  }

  /**
   * Convenience method to log Warnings without an exception This call is
   * exactly equivalent to logWarning(message, null) @param message the message
   * to include with the warning
   */
  public static void logWarning(String message) {
    logWarning(message, null);
  }

  /**
   * Convenience method to log Warnings along with an exception @param message
   * the message to include with the warning @param exception the exception to
   * include with the warning
   */
  public static void logWarning(String message, Exception exception) {
    log(Status.WARNING, message, exception);
  }

  /**
   * Convenience method to log errors
   * @param message the message to display with this error
   * @param exception the exception to associate with ths error.
   */
  public static void logError(String message, Throwable exception) {
    log(Status.ERROR, message, exception);
  }
  
  
  /**
   * Convenience method to log errors
   * @param message the message to display with this error
   */
  public static void logError(String message) {
    logError(message, new Exception());
  }

  /**
   * Gives an absolute path to the root of the VJP plugin directory
   * @return an absolute path to the root of the VJP plugin directory
   */
  public static String getRootPath() {
    try {
      return FileLocator.toFileURL(plugin.getBundle().getEntry("/")).getPath();
    } catch (IOException e) {
      // I don't really know when/how this would happen.
      logError("Error finding path to root directory of VJP.", e);
      return null;
    }
  }

  /**
   * Gives a URL that points to the resource given by the path
   * @return a URL pointing to the resource specified. null if the resource
   *         doesn't exist.
   */
  public static URL getResourceURL(String path) {
    return FileLocator.find(plugin.getBundle(), new Path(path), null);
  }

  /**
   * The default constructor for this plugin. 
   * This is usually called by Eclipse and need not be called.
   */
  public VJP() {
    plugin = this;
  }

  /**
   * Used by Eclipse to start the plugin.
   * VJP does nothing special here.
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
  }

  /**
   * Used by Eclipse to stop the plugin.
   * VJP only sets its own reference to this plugin to null. (ie: getDefault()
   * will return null)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }


}
