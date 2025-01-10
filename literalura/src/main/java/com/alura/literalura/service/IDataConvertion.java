package com.alura.literalura.service;

public interface IDataConvertion {
    <T> T convertData(String json, Class<T> clase);
}
