package com.solo.search.model;

public class MenuItem {

	private int id;
	private int title;

	public MenuItem(int id, int title) {
		this.id = id;
		this.title = title;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}

}