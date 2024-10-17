package com.example.spotspeak.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {

	@Id
	private UUID id; // Represents UUID given from AuthServer as "sub" claim in access token

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(nullable = false)
	private String username;

	@Column(nullable = true)
	private String profilePictureUrl;

	@OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
	private List<Trace> traces;

	@Column(nullable = false)
	private LocalDateTime registeredAt;
}
