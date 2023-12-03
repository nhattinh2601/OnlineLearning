package src.Dto;

import lombok.Data;

@Data
public class RegisterCourseDTO {
    private int courseId;
    private String title;
    private String name;
    private String image;
    private Boolean active;
    private Boolean isActive;
    private Boolean isDeleted;
    private Boolean Deleted;
    private int userId;
}
