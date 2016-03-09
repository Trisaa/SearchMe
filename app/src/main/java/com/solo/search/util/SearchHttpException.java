package com.solo.search.util;

public class SearchHttpException extends Exception {

	private int status;

	private static final long serialVersionUID = 1L;

	public SearchHttpException() {
	}

	public SearchHttpException(String message) {
		super(message);
	}

	public SearchHttpException(String message, int httpStatus) {
		super(message);
		this.status = httpStatus;
	}

	public SearchHttpException(int httpStatus) {
		this.status = httpStatus;
	}

	public int getHttpStatus() {
		return status;
	}
}
