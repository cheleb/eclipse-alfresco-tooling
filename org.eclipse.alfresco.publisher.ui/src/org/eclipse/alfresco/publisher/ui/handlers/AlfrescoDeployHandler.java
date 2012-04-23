package org.eclipse.alfresco.publisher.ui.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.alfresco.publisher.core.AlfrescoNature;
import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.helper.ServerHelper;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class AlfrescoDeployHandler extends AbstractHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AlfrescoDeployHandler.class);

	/**
	 * The constructor.
	 */
	public AlfrescoDeployHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchWindow window = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		IEditorInput editorInput = window.getActivePage().getActiveEditor()
				.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			IProject project = fileEditorInput.getFile().getProject();
			try {
				if (project.hasNature(AlfrescoNature.NATURE_ID)) {
					AlfrescoPreferenceHelper preferences = new AlfrescoPreferenceHelper(
							project);
					final String login = preferences.getServerLogin();
					final String password;
					try {
						password = preferences.getPassword();
					} catch (StorageException e) {
						MessageDialog.openError(window.getShell(), "Error",
								e.getMessage());
						return null;
					}
					final String url = preferences.getServerReloadWebscriptURL();

					IRunnableWithProgress iRunnableWithProgress = new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							monitor.beginTask("Reloading", 2);

							ServerHelper.reload(url, login, password, monitor);

						}
					};

					try {
						new ProgressMonitorDialog(window.getShell()).run(true,
								true, iRunnableWithProgress);
					} catch (InvocationTargetException e) {
						LOGGER.error("", e);
					} catch (InterruptedException e) {
						LOGGER.error("", e);
					}

				} else {
					LOGGER.debug("Not an alfresco project");
				}
			} catch (CoreException e) {
				MessageDialog.openError(window.getShell(), "Error",
						e.getMessage());
			}
		}
		return null;
	}
}
