package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;

import java.util.Collection;
import java.util.List;

@Repository
public interface JpaCommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemIdIn(List<Long> itemId);

    List<Comment> findAllByItemId(Long itemId);

    List<Comment> findAllByItemIdIn(Collection<Long> itemIds);
}
