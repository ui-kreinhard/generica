package de.karlNet.login;

public class NotLoggedInException extends RuntimeException {

	private static final long serialVersionUID = 5542880617278562253L;

	public NotLoggedInException() {
		super("not logged in");
	}
}
