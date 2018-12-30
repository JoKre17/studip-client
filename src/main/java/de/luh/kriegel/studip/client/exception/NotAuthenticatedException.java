package de.luh.kriegel.studip.client.exception;

public class NotAuthenticatedException extends Exception {

	private static final long serialVersionUID = 6511293854048000040L;

	public NotAuthenticatedException() {
		super("Not Authenticated!");
	}

}
