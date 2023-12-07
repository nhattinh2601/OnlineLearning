package src.service.CourseRegister.Dto;/*
Created on 11/29/2023  3:46 PM 2023

@author: tinh2

ProjectName: OnlineLearning
*/

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserRegisterCourse {

    private String fullname;

    private int courseId;


    private String course_name;


    private int userId;

    private String email;

    private int register_course_id;
}
