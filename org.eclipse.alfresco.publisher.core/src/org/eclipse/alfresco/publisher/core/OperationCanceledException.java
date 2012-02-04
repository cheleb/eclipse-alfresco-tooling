package org.eclipse.alfresco.publisher.core;


public class OperationCanceledException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OperationCanceledException(String message, Throwable e) {
      super(message,e);
	}


}
