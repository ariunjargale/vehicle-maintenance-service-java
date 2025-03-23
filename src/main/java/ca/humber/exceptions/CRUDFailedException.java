package ca.humber.exceptions;

/**
 * 
 */

@SuppressWarnings("serial")
public class CRUDFailedException extends Exception {
	public CRUDFailedException(String m) {
		super(m);
	}
}