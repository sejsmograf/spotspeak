package com.example.spotspeak.entity;

import java.time.LocalDateTime;
import java.util.List;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

	@Column(nullable = false)
	private Point location;

	@Column(nullable = false)
	private String description;

	@ManyToOne
	@JoinColumn(name = "author_id", referencedColumnName = "id", nullable = false)
	private User author;

	@CreatedDate
	private LocalDateTime createdAt;

	@OneToOne(cascade = CascadeType.REMOVE)
	@JoinColumn(name = "resource_id", referencedColumnName = "id", nullable = true)
	private Resource resource;

	@OneToMany(mappedBy = "trace", fetch = FetchType.LAZY)
	private List<Comment> comments;

	@OneToMany(mappedBy = "trace", fetch = FetchType.LAZY)
	private List<TraceTag> traceTags;

	@OneToMany(mappedBy = "trace", fetch = FetchType.LAZY)
	private List<TraceEvent> traceEvents;

	@Column(nullable = false)
	private Boolean isActive = true;
}
