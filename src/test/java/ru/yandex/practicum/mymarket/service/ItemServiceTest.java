package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ItemService.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ItemServiceTest {

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private ItemService itemService;

    private Item item1, item2;

    @BeforeEach
    void setUp() {

        item1 = new Item();
        item1.setId(1L);
        item1.setTitle("Apple");
        item1.setDescription("Red apple");
        item1.setImgPath("/img/apple.jpg");
        item1.setPrice(100L);

        item2 = new Item();
        item2.setId(2L);
        item2.setTitle("Banana");
        item2.setDescription("Yellow banana");
        item2.setImgPath("/img/banana.jpg");
        item2.setPrice(50L);

        ItemDto dto1 = new ItemDto();
        dto1.setId(1L);
        dto1.setTitle("Apple");
        dto1.setDescription("Red apple");
        dto1.setImgPath("/img/apple.jpg");
        dto1.setPrice(100L);

        ItemDto dto2 = new ItemDto();
        dto2.setId(2L);
        dto2.setTitle("Banana");
        dto2.setDescription("Yellow banana");
        dto2.setImgPath("/img/banana.jpg");
        dto2.setPrice(50L);
    }

    @Test
    void getAllItems_withoutSearchAndSort_shouldReturnAllItems() {
        when(itemRepository.findAll(PageRequest.of(0, 10, Sort.unsorted()))).thenReturn(new PageImpl<>(List.of(item1, item2)));
        when(cartService.getItemCount(1L)).thenReturn(0);
        when(cartService.getItemCount(2L)).thenReturn(0);

        List<ItemDto> result = itemService.getAllItems(null, "NO", 1, 10).getContent();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void getAllItems_withSearch_shouldFilterByTitleOrDescription() {
        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "apple", "apple", PageRequest.of(0, 10, Sort.unsorted())))
                .thenReturn(new PageImpl<>(List.of(item1)));
        when(cartService.getItemCount(1L)).thenReturn(1);

        List<ItemDto> result = itemService.getAllItems("apple", "NO", 1, 10).toList();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).contains("Apple");
        assertThat(result.getFirst().getCount()).isEqualTo(1);
    }

    @Test
    void getAllItems_withAlphaSort_shouldSortByTitle() {
        when(itemRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title")))).thenReturn(new PageImpl<>(List.of(item1, item2)));
        when(cartService.getItemCount(anyLong())).thenReturn(0);

        List<ItemDto> result = itemService.getAllItems(null, "ALPHA", 1, 10).getContent();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Apple");  // Сортировка по алфавиту
        assertThat(result.get(1).getTitle()).isEqualTo("Banana");
    }

    @Test
    void getAllItems_withPriceSort_shouldSortByPrice() {
        when(itemRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price")))).thenReturn(new PageImpl<>(List.of(item2, item1)));
        when(cartService.getItemCount(anyLong())).thenReturn(0);

        List<ItemDto> result = itemService.getAllItems(null, "PRICE", 1, 10).getContent();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isEqualTo(50L);  // Banana дешевле
        assertThat(result.get(1).getPrice()).isEqualTo(100L); // Apple дороже
    }

    @Test
    void getAllItems_withPagination_shouldReturnPage() {
        when(itemRepository.findAll(PageRequest.of(0, 1, Sort.unsorted()))).thenReturn(new PageImpl<>(List.of(item1, item2)));
        when(cartService.getItemCount(anyLong())).thenReturn(0);

        List<ItemDto> result = itemService.getAllItems(null, "NO", 1, 1).getContent();
        System.out.println("result = " + result);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void getAllItems_emptyResult_shouldReturnEmptyList() {
        when(itemRepository.findAll(PageRequest.of(0, 10, Sort.unsorted()))).thenReturn(new PageImpl<>(List.of()));

        List<ItemDto> result = itemService.getAllItems(null, "NO", 1, 10).getContent();

        assertThat(result).isEmpty();
    }


    @Test
    void getItemById_shouldReturnDto() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(cartService.getItemCount(1L)).thenReturn(3);

        ItemDto result = itemService.getItemById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Apple");
        assertThat(result.getCount()).isEqualTo(3);  // Из корзины
    }

    @Test
    void getItemById_notFound_shouldThrowException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Item not found");
    }

    @Test
    void toDto_shouldIncludeCartCount() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(cartService.getItemCount(1L)).thenReturn(5);

        ItemDto dto = itemService.getItemById(1L);

        assertThat(dto.getCount()).isEqualTo(5);
    }
}
