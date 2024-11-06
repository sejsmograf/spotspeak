package com.example.spotspeak.entity;

import java.time.LocalDateTime;
import java.util.*;

import com.example.spotspeak.entity.achievements.UserAchievement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
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

    @Column(nullable = false)
    private String email;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Resource profilePicture;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trace> traces;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable(name = "discovered_traces", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "trace_id"))
    @Builder.Default
    private Set<Trace> discoveredTraces = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<UserAchievement> userAchievements = new ArrayList<>();

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<FriendRequest> sentRequests = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<FriendRequest> receivedRequests = new ArrayList<>();

    @OneToMany(mappedBy = "userInitiating", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Friendship> initiatedFriendships = new ArrayList<>();

    @OneToMany(mappedBy = "userReceiving", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Friendship> receivedFriendships = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        User user = (User) obj;
        return id.equals(user.id);
    }

    public void removeDiscoveredTrace(Trace trace) {
        discoveredTraces.remove(trace);
    }
}
