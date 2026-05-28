package com.uet.common.model.item;

public class Book extends Item {

    private String author;
    private int publishYear;

    public Book() {
    }

    public Book(String author, int publishYear) {
        this.author = author;
        this.publishYear = publishYear;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
    }

    @Override
    public String getCategory() {
        return "BOOK";
    }

}