package com.example.demo.rest.dto.DocumentDtos;

import com.example.demo.domain.User;

import java.util.Objects;

public class NewDocumentRequest {
    private String title;
    private String content;








    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "NewDocumentRequest{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewDocumentRequest that = (NewDocumentRequest) o;
        return Objects.equals(title, that.title) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, content);
    }

    public NewDocumentRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
    public  NewDocumentRequest(){

    }



    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
