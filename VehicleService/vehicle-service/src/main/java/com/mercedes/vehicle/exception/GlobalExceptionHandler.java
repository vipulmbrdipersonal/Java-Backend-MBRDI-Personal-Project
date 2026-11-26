package com.mercedes.vehicle.exception;
import com.mercedes.vehicle.dto.ApiError;import jakarta.servlet.http.HttpServletRequest;import org.springframework.http.*;import org.springframework.web.bind.MethodArgumentNotValidException;import org.springframework.web.bind.annotation.*;import org.springframework.web.server.ResponseStatusException;import java.time.Instant;
@RestControllerAdvice public class GlobalExceptionHandler {
 @ExceptionHandler(ResponseStatusException.class) ResponseEntity<ApiError> status(ResponseStatusException ex,HttpServletRequest req){HttpStatusCode s=ex.getStatusCode();return ResponseEntity.status(s).body(new ApiError(Instant.now(),s.value(),"Request failed",ex.getReason(),req.getRequestURI()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex,HttpServletRequest req){String m=ex.getBindingResult().getFieldErrors().stream().map(e->e.getField()+": "+e.getDefaultMessage()).findFirst().orElse("Invalid request");return ResponseEntity.badRequest().body(new ApiError(Instant.now(),400,"Validation failed",m,req.getRequestURI()));}
 @ExceptionHandler(Exception.class) ResponseEntity<ApiError> generic(Exception ex,HttpServletRequest req){return ResponseEntity.status(500).body(new ApiError(Instant.now(),500,"Internal server error","An unexpected error occurred",req.getRequestURI()));}
}
