package com.jd.alpha.search.driver.creater;

public class Creator {
	private IndexCreator indexCreator;

	private String name;

	public Creator(IndexCreator indexCreator, String name) {
		this.indexCreator = indexCreator;
		this.name=name;
	}

	public boolean create() {
		return indexCreator.create();
	}

	public IndexCreator getIndexCreator() {
		return indexCreator;
	}

	public void setIndexCreator(IndexCreator indexCreator) {
		this.indexCreator = indexCreator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	


}
