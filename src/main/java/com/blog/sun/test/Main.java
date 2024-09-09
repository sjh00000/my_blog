package com.blog.sun.test;

import javafx.scene.control.OverrunStyle;

public class Main{
    public static void main(String[] args) {
        Author1 author=new Author1("1","2","3","4");
        String a = author.getId();
        author.setId("12");

        System.out.println(author.getId()+" ");

        System.out.println(author.getId()+" ");
    }

}
