package com.example.spotspeak.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.repository.TestEntityFactory;

import jakarta.transaction.Transactional;

public class TagServiceIntegrationTest
        extends BaseServiceIntegrationTest {

    @Autowired
    TagService tagService;

    private final int TAG_COUNT = 10;
    List<Tag> testTags;

    @BeforeEach
    public void setUp() {
        testTags = TestEntityFactory.createPersistedTags(entityManager, TAG_COUNT);
    }

    @Test
    @Transactional
    public void findAll_returnsAllTags() {
        List<Tag> found = tagService.findAll();

        assertThat(found).isNotEmpty().hasSize(TAG_COUNT).containsAll(testTags);
    }

    @Test
    @Transactional
    public void findAllByIds_returnsTags() {
        int ID_COUNT = 4;
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < ID_COUNT && i < TAG_COUNT; i++) {
            ids.add(testTags.get(i).getId());
        }

        List<Tag> found = tagService.findAllByIds(ids);

        assertThat(found).isNotEmpty().hasSize(ID_COUNT);
    }
}
