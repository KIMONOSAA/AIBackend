package com.kimo.controller;



import com.kimo.api.dto.UserDto;
import com.kimo.ucenter.model.po.Token;
import com.kimo.ucenter.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @PostMapping("/findtoken")
    public Token findByToken(String jwt){
        return tokenService.findByToken(jwt);
    }

    @PostMapping("/findalluser")
    public List<Token> findAllValidTokenByUser(Long id){
        return tokenService.findAllValidTokenByUser(id);
    }

    @PostMapping("/revoketoken")
    public void getRevokeAllUserToken(@RequestBody UserDto user){
        tokenService.executeRevokeAllUserToken(user);
    }

    @PostMapping("/savetoken")
    public void getSaveUserToken(@RequestBody UserDto user,@RequestParam String jwtToken){
        tokenService.executeSaveUserToken(user,jwtToken);
    }
}
