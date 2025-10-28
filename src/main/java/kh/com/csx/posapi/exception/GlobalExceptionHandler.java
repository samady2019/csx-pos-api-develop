package kh.com.csx.posapi.exception;

import kh.com.csx.posapi.model.BaseResponse;
import kh.com.csx.posapi.model.ErrorResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BaseResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        boolean isUnsafe = errorMessages == null
                || errorMessages.length() > 300
                || errorMessages.matches(".*\\b(exception|null pointer|illegal|stack trace|sql|constraint|invoke|java|at index|com\\.|at org\\.|at java\\.).*")
                || errorMessages.contains("\n")
                || errorMessages.contains("\tat ");
        return new ErrorResponse(isUnsafe ? "Invalid request format or parameters." : errorMessages);
    }

    @ExceptionHandler({
        org.springframework.beans.TypeMismatchException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.validation.BindException.class,
        org.springframework.beans.InvalidPropertyException.class,
        org.springframework.core.convert.ConversionFailedException.class,
        org.springframework.web.bind.ServletRequestBindingException.class,
        org.springframework.http.converter.HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public BaseResponse handleBindingException(Exception e) {
        return new ErrorResponse("Invalid request format or parameters.");
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<BaseResponse> handleApiException(ApiException e) {
        String message = e.getMessage();
        boolean isUnsafe = message == null
                || message.length() > 300
                || message.matches(".*\\b(exception|null pointer|illegal|stack trace|sql|constraint|invoke|java|at index|com\\.|at org\\.|at java\\.).*")
                || message.contains("\n")
                || message.contains("\tat ");
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setMessage(isUnsafe ? "An error occurred. Please try again later." : message);
        return new ResponseEntity<>(baseResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public BaseResponse handleAccessDeniedException(AccessDeniedException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public BaseResponse handleForbiddenException(BadCredentialsException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public BaseResponse handleNoHandlerException(NoHandlerFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public BaseResponse handleException(Exception e) {
        return new ErrorResponse("An error occurred. Please try again later.");
    }
}
