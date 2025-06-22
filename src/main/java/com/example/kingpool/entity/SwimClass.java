package com.example.kingpool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "swim_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SwimClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Integer classId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;  // ✅ Tên lớp học

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private User coach;

    @NotNull
    @Min(value = 1, message = "Số học viên tối đa phải lớn hơn 0")
    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    @NotNull
    @Min(value = 0, message = "Số học viên hiện tại không được âm")
    @Column(name = "current_students", nullable = false)
    private Integer currentStudents = 0;

    @NotBlank
    @Size(max = 20)
    @Column(name = "level", nullable = false, length = 20)
    private String level = "Beginner";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotBlank
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "Open";

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Danh sách buổi học (lịch học) của lớp này
     */
    @OneToMany(mappedBy = "swimClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassSchedule> schedules = new ArrayList<>();
}
