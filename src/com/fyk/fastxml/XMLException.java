package com.fyk.fastxml;

public class XMLException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public XMLException(){
        super();
    }

    public XMLException(String message){
        super(message);
    }

    public XMLException(String message, Throwable cause){
        super(message, cause);
    }
}
