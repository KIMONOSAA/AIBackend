package com.kimo.model.dto;

import com.kimo.model.po.Teachplan;
import com.kimo.model.po.TeachplanMedia;
import lombok.Data;

/**
 * @author Mr.kimo
 */
@Data
public class TeachplanListDto extends TeachplanDto {

    private TeachplanMedia teachplanMedia;


}
