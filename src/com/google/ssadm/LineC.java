package com.google.ssadm;

public class LineC extends Line{
    private Integer time;

    public LineC(String type , String service , String question , String responseType , String date , Integer time) {
        super(type , service , question , responseType , date);
        this.time = time;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "LineC{" +
                "type='" + getType() + '\'' +
                ", service='" + getService() + '\'' +
                ", question='" + getQuestion() + '\'' +
                ", responseType='" + getResponseType() + '\'' +
                ", date='" + getDate() + '\'' +
                "time=" + time +
                '}';
    }
}
