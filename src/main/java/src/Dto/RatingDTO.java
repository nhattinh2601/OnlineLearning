package src.Dto;

import lombok.Data;

@Data
public class RatingDTO {
    private int Id;
    private int courseId;
    private int userId;
    private int rating;
}
