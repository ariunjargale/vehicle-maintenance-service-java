package ca.humber.exceptions;

@SuppressWarnings("serial")
public class ConstraintException extends Exception {
	public ConstraintException(String m) {
		super(m);
	}
}