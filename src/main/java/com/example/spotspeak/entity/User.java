package com.example.spotspeak.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.example.spotspeak.entity.achievements.UserAchievement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
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

	@OneToMany(mappedBy = "author", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	private List<Trace> traces;

	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	private List<UserAchievement> userAchievements;

	@OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
	private List<Comment> comments;

	@OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
	private List<FriendRequest> sentRequests;

	@OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY)
	private List<FriendRequest> receivedRequests;

	@Column(nullable = false)
	private LocalDateTime registeredAt;
}
