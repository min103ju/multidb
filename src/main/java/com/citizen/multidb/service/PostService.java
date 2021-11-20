package com.citizen.multidb.service;

import com.citizen.multidb.domain.post.Post;
import com.citizen.multidb.domain.post.PostRepository;
import com.citizen.multidb.web.dto.PostRequestDto;
import com.citizen.multidb.web.dto.PostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public PostResponseDto findPostById(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id = " + id));

        return new PostResponseDto(post);
    }

    @Transactional
    public PostResponseDto update(PostRequestDto requestDto) {
        Post post = postRepository.findById(requestDto.getId())
            .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id = " + requestDto.getId()));

        return post.update(requestDto.getTitle(), requestDto.getContent());
    }

}
