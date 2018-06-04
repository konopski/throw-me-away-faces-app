package org.example;

import javax.faces.bean.ManagedBean;

@ManagedBean
public class MyFacesApp {
    private String message = "hello";

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
