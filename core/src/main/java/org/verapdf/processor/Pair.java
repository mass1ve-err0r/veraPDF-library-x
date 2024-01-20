package org.verapdf.processor;


public class Pair<T, U> {

	private T first;
	private U second;


	public Pair() {
		this.first = null;
		this.second = null;
	}

	public Pair(T first, U second) {
		this.first = first;
		this.second = second;
	}

	public T first() {
		return first;
	}

	public void setFirst(T first) {
		this.first = first;
	}

	public U second() {
		return second;
	}

	public void setSecond(U second) {
		this.second = second;
	}

}
