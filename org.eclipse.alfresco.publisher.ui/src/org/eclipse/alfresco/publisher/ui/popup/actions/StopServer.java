package org.eclipse.alfresco.publisher.ui.popup.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
import org.eclipse.alfresco.publisher.core.helper.ServerHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopServer implements IObjectActionDelegate {
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(StopServer.class);
	
	private Shell shell;

	private ISelection selection;

	@Override
	public void run(IAction action) {
		final IProject project = getProject();

		if (project == null) {
			return;
		}

		final AlfrescoPreferenceHelper preferences = new AlfrescoPreferenceHelper(
				project);
		
		final int expectedSteps = getExpectedStep(preferences);
		
		IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
			
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
					try {
						monitor.beginTask("Stopping server", expectedSteps);
						ServerHelper.stopServer(preferences, monitor);
						monitor.done();
					} catch (IOException e) {
						monitor.setCanceled(true);
						throw new InvocationTargetException(e, e.getLocalizedMessage());
					}
				
			}
		};
		
		try {
			new ProgressMonitorDialog(shell).run(true, true,runnableWithProgress);
		} catch (InvocationTargetException e) {
			LOGGER.error(e.getLocalizedMessage(), e.getCause());
			MessageDialog.openError(shell, "Error", e.getLocalizedMessage());
		} catch (InterruptedException e) {
			LOGGER.error(e.getLocalizedMessage(), e.getCause());
			MessageDialog.openError(shell, "Error", e.getLocalizedMessage());
		}

	}
	
	
	private int getExpectedStep(AlfrescoPreferenceHelper preferences) {
		if(preferences.isAlfresco()) {
			return preferences.getStopTimeout();
		}else {
			return 2;
		}
	}


	protected IProject getProject() {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object object = structuredSelection.getFirstElement();

			if (object instanceof IProject) {
				return (IProject) object;
			}
		}
		return null;

	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;

	}
}
