package com.blog.sun.test;

public class Author1 {
    private String id;
    private String password;
    private String name;
    private String email;

    public Author1(String i,String p,String n,String e){
        this.id=i;
        this.password=p;
        this.name=n;
        this.email=e;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String i){
        this.id=i;
    }
}