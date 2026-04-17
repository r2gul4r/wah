package com.example.demo.dto;

import lombok.Data;

// Lombok으로 getter/setter 자동 생성
@Data
public class Movie {
    private int rank;
    private String title;
    private String openDate;
    private String rating;
}
