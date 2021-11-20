package com.citizen.multidb.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PostRequestDto {

    private Long id;
    private String title;
    private String content;

    public PostRequestDto(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }
}
