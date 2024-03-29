package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.JpaBookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.controller.dto.CommentDto;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.JpaCommentRepository;
import ru.practicum.shareit.item.repository.JpaItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final JpaItemRepository itemRepository;
    private final ItemMapper mapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;
    private final JpaUserRepository userRepository;
    private final JpaBookingRepository bookingRepository;
    private final JpaCommentRepository commentRepository;

    @Override
    @Transactional()
    public ItemDtoResponse addNewItem(Long userId, ItemDtoRequest request) {
        Item item = mapper.toItem(request);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        item.setOwner(user);
        Item createdItem = itemRepository.save(item);
        return mapper.toResponse(createdItem);
    }

    @Override
    @Transactional
    public ItemDtoResponse update(Long userId, Long itemId, ItemDtoRequest request) {
        Item requestItem = mapper.toItem(request);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (requestItem.getName() != null) {
            item.setName(requestItem.getName());
        }
        if (requestItem.getDescription() != null) {
            item.setDescription(requestItem.getDescription());
        }
        if (requestItem.getAvailable() != null) {
            item.setAvailable(requestItem.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        return mapper.toResponse(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoResponse getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        ItemDtoResponse responseItem = mapper.toResponse(item);
        responseItem.setComments(commentMapper.toCommentDto(commentRepository.findByItemIdIn(List.of(itemId))));
        if (!userId.equals(item.getOwner().getId())) return responseItem;
        List<Booking> bookings = bookingRepository
                .findByItemIdInAndStatusNot(List.of(itemId), Status.REJECTED);
        responseItem.setLastBooking(findLastBooking(bookings));
        responseItem.setNextBooking(findNextBooking(bookings));

        return responseItem;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDtoResponse> getItemsByUserId(Long userId) {
        List<ItemDtoResponse> items = itemRepository.findAllByOwnerId(userId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        List<Long> itemIds = items.stream()
                .map(ItemDtoResponse::getId)
                .collect(Collectors.toList());
        Map<Long, List<Booking>> bookingsByItemId = bookingRepository
                .findByItemIdInAndStatusNot(itemIds, Status.REJECTED).stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));
        Map<Long, List<Comment>> commentsMapByItemId = commentRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));
        return items.stream()
                .peek(i -> i.setLastBooking(findLastBooking(bookingsByItemId.get(i.getId()))))
                .peek(i -> i.setNextBooking(findNextBooking(bookingsByItemId.get(i.getId()))))
                .peek(i -> i.setComments(commentMapper.toCommentDto(commentsMapByItemId.get(i.getId()))))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDtoResponse> findItemsByText(String text) {
        List<Item> items = itemRepository.findByNameOrDescriptionContainingIgnoreCaseAndAvailableIsTrue(text, text);
        return items.stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long bookerId, Long itemId, CommentDto commentDto) {
        Booking booking = bookingRepository.findFirstByItemIdAndBookerIdOrderByStart(itemId, bookerId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID%d не бронировал вещь с ID%d ",
                        bookerId, itemId)));
        if (booking.getEnd().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Пользователь может оставлять отзыв только после окончания срока аренды!");
        }
        Comment comment = commentMapper.toComment(commentDto);
        comment.setItem(booking.getItem());
        comment.setAuthor(booking.getBooker());
        comment.setCreated(LocalDateTime.now());
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    private BookingDtoForItem findLastBooking(List<Booking> bookingList) {
        if (bookingList == null) return null;
        return bookingList.stream()
                .filter(b -> b.getStart().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(Booking::getEnd))
                .map(bookingMapper::toBookingDtoForItem).orElse(null);
    }

    private BookingDtoForItem findNextBooking(List<Booking> bookingList) {
        if (bookingList == null) return null;
        return bookingList.stream()
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(Booking::getStart))
                .map(bookingMapper::toBookingDtoForItem).orElse(null);
    }

}