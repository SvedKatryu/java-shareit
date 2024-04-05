package ru.practicum.shareit.item.mapper;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.practicum.shareit.item.controller.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentMapperTest {

    private CommentMapper commentMapper;

    @BeforeAll
    void setUp() {
        commentMapper = new CommentMapperImpl();
    }

    @Test
    @DisplayName("Маппинг null toComment")
    void toComment_mapNull_ShouldReturnNull() {
        Comment comment = commentMapper.toComment(null);

        assertThat(comment, nullValue());
    }

    @Test
    @DisplayName("Маппинг null toCommentDto")
    void toCommentDto_mapNull_ShouldReturnNull() {
        CommentDto comment = commentMapper.toCommentDto(null);

        assertThat(comment, nullValue());
    }

    @Test
    @DisplayName("Маппинг null toCommentDtoList")
    void toCommentDtoList_mapNull_ShouldReturnNull() {
        List<CommentDto> comments = commentMapper.toCommentDtoList(null);

        assertThat(comments, nullValue());
    }

}
