package com.example.demo.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


@Data
public class ProductRequest {

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private Double price;

}