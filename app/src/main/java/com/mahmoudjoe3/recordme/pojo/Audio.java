package com.mahmoudjoe3.recordme.pojo;
import org.jetbrains.annotations.NotNull;

public class Audio {
    private String name;
    private String path;
    private String createdDate;
    private String time;
    private String space;

    public Audio() { }

    @Override
    public @NotNull String toString() {
        return "Audio{" +
                "name='" + name + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", time='" + time + '\'' +
                ", space='" + space + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }
}