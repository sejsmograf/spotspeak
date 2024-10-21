package com.example.spotspeak.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resources")
@Builder
public class Resource {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false)
	private String s3Key;

	@Column(nullable = true)
	private String fileType;

	@CreatedDate
	private LocalDateTime createdAt;

	@PreRemove
	public void preRemove() {
		// removing files from s3 server before deleting from database
	}

}
