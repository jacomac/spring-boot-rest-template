package sprest.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Getter
@Setter
public class ApiErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private Date timestamp;

    private int code;

    private String status;

    private String message;

    private Object[] errors;

    public ApiErrorResponse() {
        timestamp = new Date();
    }

    public ApiErrorResponse(
        HttpStatus httpStatus,
        String message,
        Object[] errors
    ) {
        this();
        this.code = httpStatus.value();
        this.status = httpStatus.name();
        this.message = message;
        this.errors = errors;
    }

}