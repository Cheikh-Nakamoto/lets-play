package com._talent.lets_play.exception;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
public class ErrorResponse {
    private final String code;
    private final String message;
    private final int status;
    private final LocalDateTime timestamp;
    private final List<FieldError> fieldErrors;
    private final String path;

    private ErrorResponse(Builder builder, String path) {
        this.code = builder.code;
        this.message = builder.message;
        this.status = builder.status;
        this.timestamp = builder.timestamp;
        this.fieldErrors = builder.fieldErrors;
        this.path = path;
    }


    // Classe interne représentant une erreur de validation de champ

    @Data
    public static class FieldError {
        private final String field;
        private final String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }

    // Builder pattern
    public static class Builder {
        private String code;
        private String message;
        private int status;
        private LocalDateTime timestamp = LocalDateTime.now();
        private List<FieldError> fieldErrors = new ArrayList<>();
        private String path;

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withStatus(int status) {
            this.status = status;
            return this;
        }

        public Builder withTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withFieldError(String field, String message) {
            this.fieldErrors.add(new FieldError(field, message));
            return this;
        }


        public ErrorResponse build() {
            return new ErrorResponse(this, path);
        }
    }
}
