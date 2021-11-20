package com.citizen.multidb.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.citizen.multidb.domain.post.Post;
import com.citizen.multidb.domain.post.PostRepository;
import com.citizen.multidb.web.dto.PostRequestDto;
import com.citizen.multidb.web.dto.PostResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MultidbServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        final Long id = 1L;
        final String title = "title";
        final String content = "content";
        final String author = "author";
        Post post = Post.builder()
            .id(id)
            .title(title)
            .content(content)
            .author(author)
            .build();

        postRepository.save(post);
        postRepository.flush();

    }

    @Test
    void 단건_게시글_조회_테스트_SLAVE() {
        // given
        final Long id = 1L;

        // when
        PostResponseDto findedPost = postService.findPostById(id);

        // then
        assertThat(findedPost.getId()).isEqualTo(id);
    }

    @Test
    void 게시글_업데이트_테스트_MASTER() {
        // given
        final Long id = 1L;
        final String updateTitle = "updated title";
        final String updateContent = "updated content";
        PostRequestDto postRequestDto = new PostRequestDto(id, updateTitle, updateContent);

        // when
        PostResponseDto updatedPost = postService.update(postRequestDto);

        // then
        assertThat(updatedPost.getTitle()).isEqualTo(updateTitle);
        assertThat(updatedPost.getContent()).isEqualTo(updateContent);
    }

}
