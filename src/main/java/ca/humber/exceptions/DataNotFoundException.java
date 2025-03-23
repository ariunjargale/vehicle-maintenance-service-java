package ca.humber.exceptions;

@SuppressWarnings("serial")
public class DataNotFoundException extends Exception {
	public DataNotFoundException(String m) {
		super(m);
	}
}