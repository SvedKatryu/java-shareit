package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.controller.dto.CommentDto;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional

class ItemServiceImplIntegrationTest {

    private final ItemServiceImpl itemService;

    private final JpaUserRepository userRepository;

    private final BookingService bookingService;

    private User savedUser1;

    private User savedUser2;

    private ItemDtoRequest itemDto;

    private BookingDtoResponse savedBooking1;
    private BookingDtoResponse savedBooking2;
    private BookingDtoResponse savedBooking3;

    @BeforeAll
    void setUp() {
        User user1 = User.builder()
                .name("user1")
                .email("user1@email.com")
                .build();
        savedUser1 = userRepository.save(user1);
        User user2 = User.builder()
                .name("user2")
                .email("user2@email.com")
                .build();
        savedUser2 = userRepository.save(user2);
        itemDto = ItemDtoRequest.builder()
                .name("itemDto")
                .description("itemDto description")
                .available(true)
                .build();
    }

    @AfterAll
    void deleteUsers() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Добавление вещи")
    void addItem_ShouldReturnItemWithNotNullId() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        assertThat(savedItem, notNullValue());
        assertThat(savedItem.getId(), greaterThan(0L));
        assertThat(savedItem.getName(), is(itemDto.getName()));
        assertThat(savedItem.getDescription(), is(itemDto.getDescription()));
        assertThat(savedItem.getAvailable(), is(itemDto.getAvailable()));
    }

    @Test
    @DisplayName("Добавление вещи, пользователь не найден")
    void addItem_UserNotExists_ShouldThrowNotFoundException() {

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addNewItem(999L, itemDto));

        assertThat(e.getMessage(), is("Пользователь с ID999 не найден"));
    }

    @Test
    @DisplayName("Обновление данных о вещи")
    void updateItem_WhenAllUpdateFieldsNotNull_ShouldUpdateNameDescriptionAndAvailable() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        ItemDtoRequest itemUpdateDto = ItemDtoRequest.builder()
                .name("new name")
                .description("new description")
                .available(false)
                .build();

        ItemDtoResponse updatedItem = itemService.update(savedUser1.getId(), savedItem.getId(), itemUpdateDto);

        assertThat(updatedItem, notNullValue());
        assertThat(updatedItem.getName(), is(itemUpdateDto.getName()));
        assertThat(updatedItem.getDescription(), is(itemUpdateDto.getDescription()));
        assertThat(updatedItem.getAvailable(), is(itemUpdateDto.getAvailable()));
    }

    @Test
    @DisplayName("Обновление данных о вещи без нового названия")
    void updateItem_WhenAllUpdateDescriptionAndAvailable_ShouldUpdateDescriptionAndAvailable() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        ItemDtoRequest itemUpdateDto = ItemDtoRequest.builder()
                .description("new description")
                .available(false)
                .build();

        ItemDtoResponse updatedItem = itemService.update(savedUser1.getId(), savedItem.getId(), itemUpdateDto);

        assertThat(updatedItem, notNullValue());
        assertThat(updatedItem.getName(), is(savedItem.getName()));
        assertThat(updatedItem.getDescription(), is(itemUpdateDto.getDescription()));
        assertThat(updatedItem.getAvailable(), is(itemUpdateDto.getAvailable()));
    }

    @Test
    @DisplayName("Обновление данных о вещи без статуса доступности")
    void updateItem_WhenAllUpdateNameAndDescription_ShouldUpdateNameDescription() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        ItemDtoRequest itemUpdateDto = ItemDtoRequest.builder()
                .name("new name")
                .description("new description")
                .build();

        ItemDtoResponse updatedItem = itemService.update(savedUser1.getId(), savedItem.getId(), itemUpdateDto);

        assertThat(updatedItem, notNullValue());
        assertThat(updatedItem.getName(), is(itemUpdateDto.getName()));
        assertThat(updatedItem.getDescription(), is(itemUpdateDto.getDescription()));
        assertThat(updatedItem.getAvailable(), is(savedItem.getAvailable()));
    }

    @Test
    @DisplayName("Обновление данных о вещи без нового описания")
    void updateItem_WhenAllUpdateNameAndAvailable_ShouldUpdateNameAndAvailable() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        ItemDtoRequest itemUpdateDto = ItemDtoRequest.builder()
                .name("new name")
                .available(false)
                .build();

        ItemDtoResponse updatedItem = itemService.update(savedUser1.getId(), savedItem.getId(), itemUpdateDto);

        assertThat(updatedItem, notNullValue());
        assertThat(updatedItem.getName(), is(itemUpdateDto.getName()));
        assertThat(updatedItem.getDescription(), is(savedItem.getDescription()));
        assertThat(updatedItem.getAvailable(), is(itemUpdateDto.getAvailable()));
    }

    @Test
    @DisplayName("Обновление данных о вещи не владельцем")
    void updateItem_WhenUserIsNotOwner_ShouldThrowNotFoundException() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        ItemDtoRequest itemUpdateDto = ItemDtoRequest.builder()
                .name("new name")
                .available(false)
                .build();

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.update(savedUser2.getId(), savedItem.getId(), itemUpdateDto));
        assertThat(e.getMessage(), is("Пользователь не является владельцем вещи"));
    }

    @Test
    @DisplayName("Обновление данных о вещи, пользователь не найден")
    void updateItem_WhenUserINotFound_ShouldThrowNotFoundException() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        ItemDtoRequest itemUpdateDto = ItemDtoRequest.builder()
                .name("new name")
                .available(false)
                .build();

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.update(999L, savedItem.getId(), itemUpdateDto));
        assertThat(e.getMessage(), is("Пользователь с ID999 не найден"));
    }

    @Test
    @DisplayName("Обновление данных о вещи, вещь не найдена")
    void updateItem_WhenItemINotFound_ShouldThrowNotFoundException() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        ItemDtoRequest itemUpdateDto = ItemDtoRequest.builder()
                .name("new name")
                .available(false)
                .build();

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.update(savedUser1.getId(), 999L, itemUpdateDto));
        assertThat(e.getMessage(), is("Вещь с ID999 не найдена"));
    }

    @Test
    @DisplayName("Поиск вещи по id, запрос от владельца")
    void findItemById_WhenRequestByOwner_ShouldReturnItemWithBookings() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);
        long itemId = savedItem.getId();
        setUpBookings(itemId);


        ItemDtoResponse item = itemService.getItemById(savedUser1.getId(), itemId);

        assertThat(item, notNullValue());
        assertThat(item.getComments(), emptyIterable());
        assertThat(item.getLastBooking().getStart(), is(savedBooking1.getStart()));
        assertThat(item.getLastBooking().getEnd(), is(savedBooking1.getEnd()));
        assertThat(item.getLastBooking().getBookerId(), is(savedUser2.getId()));
        assertThat(item.getNextBooking().getStart(), is(savedBooking3.getStart()));
        assertThat(item.getNextBooking().getEnd(), is(savedBooking3.getEnd()));
        assertThat(item.getNextBooking().getBookerId(), is(savedUser2.getId()));
    }

    @Test
    @DisplayName("Поиск вещи по id, запрос не от владельца")
    void findItemById_WhenRequestByOtherUser_ShouldReturnItemWithoutBookings() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);
        long itemId = savedItem.getId();
        setUpBookings(itemId);


        ItemDtoResponse item = itemService.getItemById(savedUser2.getId(), itemId);

        assertThat(item, notNullValue());
        assertThat(item.getComments(), emptyIterable());
        assertThat(item.getLastBooking(), nullValue());
        assertThat(item.getNextBooking(), nullValue());
    }

    @Test
    @DisplayName("Поиск вещей пользователя")
    void findAllItemsByUserId_ShouldReturnOwnersItemListWithBookings() {
        long from = 0;
        int size = 4;
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);
        long itemId = savedItem.getId();
        setUpBookings(itemId);

        List<ItemDtoResponse> items = itemService.getItemsByUserId(savedUser1.getId(), from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getLastBooking().getStart(), is(savedBooking1.getStart()));
        assertThat(items.get(0).getLastBooking().getEnd(), is(savedBooking1.getEnd()));
        assertThat(items.get(0).getLastBooking().getBookerId(), is(savedUser2.getId()));
        assertThat(items.get(0).getNextBooking().getStart(), is(savedBooking3.getStart()));
        assertThat(items.get(0).getNextBooking().getEnd(), is(savedBooking3.getEnd()));
        assertThat(items.get(0).getNextBooking().getBookerId(), is(savedUser2.getId()));
    }

    @Test
    @DisplayName("Поиск вещей пользователя, со 2го элемента")
    void findAllItemsByUserId_WhenFromIs1_ShouldReturnEmptyList() {
        long from = 1;
        int size = 4;
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);
        long itemId = savedItem.getId();
        setUpBookings(itemId);

        List<ItemDtoResponse> items = itemService.getItemsByUserId(savedUser1.getId(), from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(0));
    }

    @Test
    @DisplayName("Поиск вещей пользователя, у пользователя нет вещей")
    void findAllItemsByUserId_WhenUserNotHaveItems_ShouldReturnEmptyList() {
        long from = 0;
        int size = 4;

        List<ItemDtoResponse> items = itemService.getItemsByUserId(savedUser1.getId(), from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(0));
    }

    @Test
    @DisplayName("Поиск вещей")
    void searchItems_ShouldReturnItemsContainingTextInTitleOrDescription() {
        long from = 0;
        int size = 10;
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        List<ItemDtoResponse> items = itemService.findItemsByText("Dto", from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getName(), is(itemDto.getName()));
        assertThat(items.get(0).getDescription(), is(itemDto.getDescription()));
    }

    @Test
    @DisplayName("Поиск вещей, со 2го элемента")
    void searchItems_From1_ShouldReturnEmptyList() {
        long from = 1;
        int size = 10;
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        List<ItemDtoResponse> items = itemService.findItemsByText("Dto", from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(0));
    }

    @Test
    @DisplayName("Поиск вещей, верхний регистр")
    void searchItems_TextUpperCase_ShouldReturnItemsContainingTextInTitleOrDescription() {
        long from = 0;
        int size = 10;
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        List<ItemDtoResponse> items = itemService.findItemsByText("DTO", from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getName(), is(itemDto.getName()));
        assertThat(items.get(0).getDescription(), is(itemDto.getDescription()));
    }

    @Test
    @DisplayName("Поиск вещей, только по описанию")
    void searchItems_SearchOnlyDescription_ShouldReturnItemsContainingTextInTitleOrDescription() {
        long from = 0;
        int size = 10;
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);

        List<ItemDtoResponse> items = itemService.findItemsByText("DEScripTioN", from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getName(), is(itemDto.getName()));
        assertThat(items.get(0).getDescription(), is(itemDto.getDescription()));
    }

    @Test
    @DisplayName("Поиск вещей, вещь недоступна")
    void searchItems_WhenItemUnavailable_ShouldReturnEmptyList() {
        long from = 1;
        int size = 4;
        ItemDtoRequest unavailableItemDto = ItemDtoRequest.builder()
                .name("itemDto")
                .description("itemDto description")
                .available(false)
                .build();
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), unavailableItemDto);

        List<ItemDtoResponse> items = itemService.findItemsByText("DEScripTioN", from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(0));
    }

    @Test
    @DisplayName("Добавление отзыва о вещи")
    void addCommentToItem_ShouldReturnCommentWithNotNullId() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);
        setUpBookings(savedItem.getId());
        CommentDto addCommentDto = CommentDto.builder().text("comment").build();

        CommentDto commentDto = itemService.addComment(savedUser2.getId(), savedItem.getId(), addCommentDto);

        assertThat(commentDto, notNullValue());
        assertThat(commentDto.getAuthorName(), is(savedUser2.getName()));
        assertThat(commentDto.getText(), is(addCommentDto.getText()));
        assertThat(commentDto.getCreated(), lessThanOrEqualTo(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Добавление отзыва о вещи, от пользователя не бравшего вещь в аренду")
    void addCommentToItem_WhenUserThatNotBookedItem_ShouldThrowItemUnavailableException() {
        ItemDto savedItem = itemService.addNewItem(savedUser1.getId(), itemDto);
        setUpBookings(savedItem.getId());
        CommentDto addCommentDto = CommentDto.builder().text("comment").build();

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addComment(savedUser1.getId(), savedItem.getId(), addCommentDto));
        assertThat(e.getMessage(), is("Пользователь с ID" + savedUser1.getId() + " не бронировал вещь с ID" +
                savedItem.getId()));
    }


    private void setUpBookings(long itemId) {
        BookingDtoRequest addBookingDto1 = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        savedBooking1 = bookingService.add(savedUser2.getId(), addBookingDto1);
//        bookingService.acknowledgeBooking(savedUser1.getId(), savedBooking1.getId(), true);
        BookingDtoRequest addBookingDto2 = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusDays(8))
                .end(LocalDateTime.now().minusDays(7))
                .build();
        savedBooking2 = bookingService.add(savedUser2.getId(), addBookingDto2);
//        bookingService.acknowledgeBooking(savedUser1.getId(), savedBooking2.getId(), true);
        BookingDtoRequest addBookingDto3 = BookingDtoRequest.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(10))
                .end(LocalDateTime.now().plusDays(12))
                .build();
        savedBooking3 = bookingService.add(savedUser2.getId(), addBookingDto3);
//        bookingService.acknowledgeBooking(savedUser1.getId(), savedBooking3.getId(), true);
    }
}