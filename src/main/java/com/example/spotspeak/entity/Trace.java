package com.example.spotspeak.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "traces")
public class Trace {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@JsonIgnore
	@Column(nullable = false)
	private Point location;

	@Column(nullable = false)
	private String description;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", referencedColumnName = "id", nullable = false)
	private User author;

	@JsonIgnore
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "resource_id", referencedColumnName = "id", nullable = true)
	private Resource resource;

	@JsonIgnore
	@OneToMany(mappedBy = "trace", fetch = FetchType.LAZY)
	private List<Comment> comments;

	@ManyToMany
	@JoinTable(name = "trace_tags", joinColumns = @JoinColumn(name = "trace_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
	@Builder.Default
	private List<Tag> tags = new ArrayList<>();

	@JsonIgnore
	@ManyToMany(cascade = CascadeType.MERGE, mappedBy = "discoveredTraces", fetch = FetchType.LAZY)
	Set<User> discoverers;

	@JsonIgnore
	@OneToMany(mappedBy = "trace", fetch = FetchType.LAZY)
	private List<TraceEvent> traceEvents;

	@Column(nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	@CreationTimestamp
	private LocalDateTime createdAt;

	public double getLongitude() {
		return location.getX();
	}

	public double getLatitude() {
		return location.getY();
	}
}
