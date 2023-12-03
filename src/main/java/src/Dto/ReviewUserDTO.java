package src.Dto;

import lombok.Data;

@Data
public class ReviewUserDTO {
    private int reviewId;
    private String content;
    private String fullname;
}
