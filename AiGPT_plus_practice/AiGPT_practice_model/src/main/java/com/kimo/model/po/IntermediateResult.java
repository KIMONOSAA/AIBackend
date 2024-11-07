package com.kimo.model.po;

import com.kimo.model.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IntermediateResult {
    UserDto userDto;
    PracticeRecord record;
    double result;


}