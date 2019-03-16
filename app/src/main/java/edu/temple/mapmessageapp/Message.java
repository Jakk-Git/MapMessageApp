package edu.temple.mapmessageapp;

public class Message {

    String text;
    boolean fromme;

    public Message(String text, boolean fromme)
    {
        this.text = text;
        this.fromme = fromme;
    }

    public String getText() {
        return text;
    }

    public boolean isFromme() {
        return fromme;
    }
}
