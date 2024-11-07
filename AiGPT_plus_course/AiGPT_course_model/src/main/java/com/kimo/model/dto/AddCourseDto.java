package com.kimo.model.dto;

import lombok.Data;

import java.io.Serializable;
/**
 * @author Mr.kimo
 */
@Data
public class AddCourseDto implements Serializable {


      private String name;

      private String users;


      private String tags;

      private String label;

      private String grade;

      private String teachmode;


      private String description;

      private String pic;


      private String charge;


      private Long memberPrice;


      private String qq;


      private String wechat;

      private String phone;

      private Integer validDays;
}
