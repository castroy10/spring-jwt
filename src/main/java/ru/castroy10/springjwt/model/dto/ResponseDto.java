package ru.castroy10.springjwt.model.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDto {

    private String message;
    private Object payload;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ResponseDto(final String invalidUsernameOrPassword) {
        this.message = invalidUsernameOrPassword;
    }

    public ResponseDto(final String message, final Object payload) {
        this.message = message;
        this.payload = payload;
    }

}
