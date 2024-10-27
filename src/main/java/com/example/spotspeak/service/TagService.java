package com.example.spotspeak.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.spotspeak.entity.Tag;
import com.example.spotspeak.repository.TagRepository;

@Service
public class TagService {

	private TagRepository tagRepository;

	public TagService(TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}

	public List<Tag> getAllTags() {
		return (List<Tag>) tagRepository.findAll();
	}

	public List<Tag> getTagsByIds(List<Long> tagIds) {
		return (List<Tag>) tagRepository.findAllById(tagIds);
	}
}
