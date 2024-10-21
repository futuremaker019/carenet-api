package com.carenet.api.interfaces.dto;

import com.carenet.api.domain.exam.model.Exam;

import java.time.LocalDateTime;

public class ExamDto {

    public record Request(String name, int orders) {
        public Exam to() {
            return new Exam(this.name, this.orders);
        }

        public static Request of(String name) {
            return new Request(name, 0);
        }
    }

    public record Response(Long id, String name, int orders, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Response of(Exam exam) {
            return new Response(exam.getId(), exam.getName(), exam.getOrders(), exam.getCreatedAt(), exam.getUpdatedAt());
        }
    }

}
