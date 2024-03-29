package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@DataJpaTest
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@Sql("classpath:repository/data.sql")
class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;

    @Test
    void findByText_notEmptyOptional_ItemExist() {
        List<Item> results = itemRepository.findByText("book");

        assertFalse(results.isEmpty());
        assertThat(results.get(0).getName()).isEqualTo("Book");
        assertThat(results.get(0).getOwner().getId()).isEqualTo(1L);
    }

    @Test
    void findAllByOwnerId_notEmptyList_ItemsExist() {
        Page<Item> page = itemRepository.findAllByOwnerId(1L, PageRequest.of(0, 20));
        List<Item> results = page.get().collect(Collectors.toList());

        assertFalse(results.isEmpty());
        assertThat(results.get(0).getName()).isEqualTo("Book");
        assertThat(results.get(0).getOwner().getId()).isEqualTo(1L);
    }

    @Test
    void findAllByRequestIdIn_notEmptyList_ItemsExist() {
        List<Item> results = itemRepository.findAllByRequestIdIn(Set.of(1L));

        assertFalse(results.isEmpty());
        assertThat(results.get(0).getName()).isEqualTo("TV box");
        assertThat(results.get(0).getOwner().getId()).isEqualTo(2L);
    }

    @Test
    void findByRequestId_notEmptyList_ItemExist() {
        Item result = itemRepository.findByRequestId(1L);

        assertNotNull(result);
        assertThat(result.getName()).isEqualTo("TV box");
        assertThat(result.getOwner().getId()).isEqualTo(2L);
    }

}