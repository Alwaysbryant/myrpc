package com.rpc.test.service;

import java.io.Serializable;

public class Dto implements Serializable {
    private static final long serialVersionUID = 5054844026931758113L;

    private String name;

    private Integer age;

    private String job;

    public Dto(String name, Integer age, String job) {
        this.name = name;
        this.age = age;
        this.job = job;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public Dto() {
    }

    @Override
    public String toString() {
        return "Dto{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", job='" + job + '\'' +
                '}';
    }
}
