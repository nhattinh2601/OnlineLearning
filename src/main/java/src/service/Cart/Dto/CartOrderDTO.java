package src.service.Cart.Dto;/*
Created on 12/4/2023  2:42 AM 2023

@author: tinh2

ProjectName: OnlineLearning
*/



import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import lombok.Data;

import java.util.Date;
@Data
public class CartOrderDTO {
    private int Id;
    private int courseId;
    private int userId;
    private String fullname;
    private double price;
    private double promotional_price;
    private String email;
    private String phone;
    private double payment_price;
    private int Status;
    private String image;
    private String title;
    private String teacher;
    private Boolean isDeleted;
    private Date createAt;
    private Date updateAt;
}
