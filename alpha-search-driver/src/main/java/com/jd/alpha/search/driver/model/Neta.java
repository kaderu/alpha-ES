package com.jd.alpha.search.driver.model;

/**
 * Created by sunxuming on 2016/11/30.
 */
public class Neta implements Cloneable {
    private long id;
    private String content; // sentence, as the minus cell, e.g., "You jump, I jump."
    private int topicId; // index of the topic where content comes from
    private int cellIdInTopic; // index of the content in this topic, e.g., "You jump, I jump." is the first line for the jump sence in film, so its cellId is 1.
    private String provenance; // e.g., Titanic
    private String charactor; // e.g., Rose, leading lady of Titanic
    private double popular; // how popular this content is

    @Override
    public Neta clone() {
        Neta neta = new Neta();
        neta.setPopular(popular);
        neta.setTopicId(topicId);
        neta.setProvenance(provenance);
        return neta;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getCellIdInTopic() {
        return cellIdInTopic;
    }

    public void setCellIdInTopic(int cellIdInTopic) {
        this.cellIdInTopic = cellIdInTopic;
    }

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

    public String getCharactor() {
        return charactor;
    }

    public void setCharactor(String charactor) {
        this.charactor = charactor;
    }

    public double getPopular() {
        return popular;
    }

    public void setPopular(double popular) {
        this.popular = popular;
    }
}
