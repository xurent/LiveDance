package com.xurent.livedance.model;

public class Gift {

    private String code;
    private Integer number;
    private String name;
    private int price;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Gift{" +
                "code='" + code + '\'' +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
