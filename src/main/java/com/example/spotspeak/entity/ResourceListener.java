package com.example.spotspeak.entity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.spotspeak.service.StorageService;

import jakarta.persistence.PreRemove;

@Component
public class ResourceListener {

	@Autowired
	private StorageService storageService;

	@PreRemove
	public void preRemove(Resource resource) {
		storageService.deleteFile(resource.getResourceKey());
	}
}
