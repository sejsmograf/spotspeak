package com.example.spotspeak.entity;

import java.time.LocalDateTime;
import java.util.List;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Point location;

    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<TraceEvent> traceEvents;

    @Column(nullable = false)
    private Boolean isActive = true;
}
