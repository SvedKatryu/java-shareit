package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.controller.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toComment(CommentDto commentDto);

    @Mapping(target = "authorName", expression = "java(comment.getAuthor().getName())")
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "authorName", expression = "java(comment.getAuthor().getName())")
    List<CommentDto> toCommentDto(List<Comment> comments);
}
