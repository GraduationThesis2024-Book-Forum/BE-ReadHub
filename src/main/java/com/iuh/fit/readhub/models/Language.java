package com.iuh.fit.readhub.models;

import jakarta.persistence.*;

import java.util.Set;


public enum Language {
//    2 ngôn ngữ Anh và Việt
    English, Vietnamese;
    private String language;
    public String getLanguage() {
        return language;
    }
}