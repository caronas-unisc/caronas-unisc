package br.unisc.caronasuniscegm.model;

public class Message {

    private int id;
    private String body;
    private String author;
    private String date;

    public Message(int id, String body, String author, String date) {
        this.id = id;
        this.body = body;
        this.author = author;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
