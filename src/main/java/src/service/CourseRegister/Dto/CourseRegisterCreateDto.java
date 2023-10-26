package src.service.CourseRegister.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CourseRegisterCreateDto {
    @JsonProperty(value = "courseId", required = true)
    private int courseId;
    @JsonProperty(value = "userId", required = true)
    private int userId;
}
