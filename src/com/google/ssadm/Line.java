package com.google.ssadm;

public abstract class Line {
    private String type;
    private String service;
    private String question;
    private String responseType;
    private String date;

    public Line(String type , String service , String question , String responseType , String date) {
        this.type = type;
        this.service = service;
        this.question = question;
        this.responseType = responseType;
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Line{" +
                "type='" + type + '\'' +
                ", service='" + service + '\'' +
                ", question='" + question + '\'' +
                ", responseType='" + responseType + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}

