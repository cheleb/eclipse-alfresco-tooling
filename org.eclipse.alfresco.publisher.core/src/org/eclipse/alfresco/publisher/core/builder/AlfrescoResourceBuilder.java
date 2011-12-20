package org.eclipse.alfresco.publisher.core.builder;

import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class AlfrescoResourceBuilder extends IncrementalProjectBuilder {

	class SampleDeltaVisitor implements IResourceDeltaVisitor {
		private String path;

		public SampleDeltaVisitor(String persistentProperty) {
			this.path = persistentProperty;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
		 * .core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource.getProjectRelativePath().toOSString()
					.startsWith("target/")) {
				switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					// handle added resource
					checkResource(resource, path);
					break;
				case IResourceDelta.REMOVED:
					removeResource(resource, path);
					break;
				case IResourceDelta.CHANGED:
					// handle changed resource
					checkResource(resource, path);
					break;
				}
				// return true to continue visiting children.
				return true;
			}
			return true;
		}
	}

	class SampleResourceVisitor implements IResourceVisitor {
		private String path;

		public SampleResourceVisitor(String persistentProperty) {
			this.path = persistentProperty;
		}

		public boolean visit(IResource resource) {
			checkResource(resource, path);
			// return true to continue visiting children.
			return true;
		}
	}

	public static final String BUILDER_ID = "org.eclipse.alfresco.publisher.core.alfrescoResourceBuilder";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}

		return null;
	}

	void removeResource(IResource resource, String path) {
		System.out.println("remove " + path + " "
				+ resource.getProjectRelativePath());

	}

	void checkResource(IResource resource, String path) {
		
		System.out.println("Copy " + path + " "
				+ resource.getProjectRelativePath());
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		String persistentProperty = ((IResource) getProject())
				.getPersistentProperty(new QualifiedName(
						"org.eclipse.alfresco.publisher", "OWNER"));
		if (persistentProperty != null) {
			System.out.println("AlfrescoResourceBuilder.build()v"
					+ persistentProperty);
		}
		try {
			getProject().accept(new SampleResourceVisitor(persistentProperty));
		} catch (CoreException e) {
		}
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		String persistentProperty = ((IResource) getProject())
				.getPersistentProperty(new QualifiedName(
						"org.eclipse.alfresco.publisher", "OWNER"));
		if (persistentProperty != null) {
			System.out.println("AlfrescoResourceBuilder.build()v"
					+ persistentProperty);
		}
		// the visitor does the work.
		delta.accept(new SampleDeltaVisitor(persistentProperty));
	}
}
