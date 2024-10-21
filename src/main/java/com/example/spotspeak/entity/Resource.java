package com.example.spotspeak.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "resources")
public class Resource {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false)
	private String s3Key;

	@Column(nullable = false)
	private String fileName;

	@Column(nullable = true)
	private String fileType;

	@PreRemove
	public void preRemove() {
		//removing files from s3 server before deleting from database
	}
}
