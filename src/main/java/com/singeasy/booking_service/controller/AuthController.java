package com.singeasy.booking_service.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.singeasy.booking_service.dto.req.LoginDto;
import com.singeasy.booking_service.dto.res.ResLoginDto;
import com.singeasy.booking_service.dto.res.UserResDto;
import com.singeasy.booking_service.entity.User;
import com.singeasy.booking_service.service.UserService;
import com.singeasy.booking_service.util.SecurityUtil;
import com.singeasy.booking_service.util.error.IdInvalidException;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${dungmount.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService=userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("auth/login")
    public ResponseEntity<ResLoginDto>login(@Valid @RequestBody LoginDto loginDTO){
        //Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        //xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // set thông tin người dùng đăng nhập vào context (có thể sử dụng sau này)
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //create token
        ResLoginDto res=new ResLoginDto();
        User currentUserDB=this.userService.getUserByEmail(loginDTO.getUsername());
        if(currentUserDB!=null){
        ResLoginDto.UserLogin u = new ResLoginDto.UserLogin(currentUserDB.getId(),currentUserDB.getEmail(),currentUserDB.getName(),currentUserDB.getRole());
        res.setUser(u);
    }
    String accessToken=this.securityUtil.createAcessToken(authentication.getName(),res);

    res.setAccessToken(accessToken);
    //create refresh token
    String refreshToken=this.securityUtil.createRefreshToken(loginDTO.getUsername(), res);
    // update user
    this.userService.updateUserToken(refreshToken,loginDTO.getUsername());

    //set cookies
    ResponseCookie responseCookie=
    ResponseCookie.from("refresh_token",refreshToken)
    .httpOnly(true)
    .secure(true)
    .path("/")
    .maxAge(refreshTokenExpiration)
    .build();
        return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .body(res);
    }

    @GetMapping("/auth/account")
    public ResponseEntity<ResLoginDto.UserGetAccount> getAccount(){
        String email=SecurityUtil.getCurrentUserLogin().isPresent()?SecurityUtil.getCurrentUserLogin().get():"";
        User currentUserDB=this.userService.getUserByEmail(email);
        ResLoginDto.UserLogin res=new ResLoginDto.UserLogin();
        ResLoginDto.UserGetAccount userGetAccount=new ResLoginDto.UserGetAccount();
        if(currentUserDB!=null){
            res.setEmail(currentUserDB.getEmail());
            res.setId(currentUserDB.getId());
            res.setName(currentUserDB.getName());
            res.setRole(currentUserDB.getRole());
            userGetAccount.setUser(res);
        
        }

        return ResponseEntity.ok().body(userGetAccount);
    }
    @GetMapping("/auth/refresh")
    public ResponseEntity<ResLoginDto>getRefreshToken(
        @CookieValue(name="refresh_token",defaultValue = "abc")String refresh_token
    ) throws IdInvalidException{
        if(refresh_token.equals("abc")){
            throw new IdInvalidException("Bạn không có refresh token ở cookies");
        }
        //check valid
        Jwt decodedToken=this.securityUtil.checkValidRefreshToken(refresh_token);
        String email=decodedToken.getSubject();
        //check user by token + email
        User currentUser=this.userService.getUserByRefreshTokenAndEmail(refresh_token, email);
        if(currentUser==null){
            throw new IdInvalidException("Refresh token không hợp lệ");
        }
        //issue new token,set refresh token as cookies
        //create token
    ResLoginDto res=new ResLoginDto();
    User currentUserDB=this.userService.getUserByEmail(email);
    if(currentUserDB!=null){
    ResLoginDto.UserLogin u = new ResLoginDto.UserLogin(currentUserDB.getId(),currentUserDB.getEmail(),currentUserDB.getName(),currentUserDB.getRole());
    res.setUser(u);

    }
    String accessToken=this.securityUtil.createAcessToken(email,res);

    res.setAccessToken(accessToken);
    //create refresh token
    String refreshToken=this.securityUtil.createRefreshToken(email, res);
    // update user
    this.userService.updateUserToken(refreshToken,email);

    //set cookies
    ResponseCookie responseCookie=
    ResponseCookie.from("refresh_token",refreshToken)
    .httpOnly(true)
    .secure(true)
    .path("/")
    .maxAge(refreshTokenExpiration)
    .build();
        return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
        .body(res);
    }
    @PostMapping("/auth/logout")
    public ResponseEntity<Void>logOut() throws IdInvalidException  {
        String email=SecurityUtil.getCurrentUserLogin().isPresent()?SecurityUtil.getCurrentUserLogin().get():"";
        if(email.equals("")){
            throw new IdInvalidException("Acess token không hơp lệ");
        }
        //update refresh token = null
        this.userService.updateUserToken(null, email);
        //remove refresh token
        ResponseCookie deleteCookie= ResponseCookie.from("refresh_token",null)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(0)
        .build();
        return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .body(null);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<UserResDto> register(@Valid @RequestBody User user) throws IdInvalidException {
       boolean isExist=this.userService.isEmailExist(user.getEmail());
         if(isExist){
          throw new IdInvalidException("Email đã tồn tại");
         }
         String hashPassword=this.passwordEncoder.encode(user.getPassword());
         user.setPassword(hashPassword);
         User createdUser=this.userService.createUser(user);

         return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertResCreateUserDTO(createdUser));
    }
}


