package src.Dto;

import lombok.Data;

import java.util.Date;

@Data
public class CommentUserDTO {
    private int commentId;
    private String content;
    private String fullname;
    private String avatar;
    private int userId;
    private int videoId;
    private Boolean isDeleted;
    private Date create;
    private Date update;
}
