package com.alexstrasza.authentication.controller;


import Messages.UserModel;
import com.alexstrasza.authentication.dao.UsersDao;
import com.alexstrasza.authentication.components.RabbitMessenger;
import com.alexstrasza.authentication.entity.AuthoritiesEntity;
import com.alexstrasza.authentication.entity.TokenResponse;
import com.alexstrasza.authentication.entity.UsersEntity;
import com.alexstrasza.authentication.service.Authority;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.security.Principal;
import java.util.HashSet;

@CrossOrigin
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    RabbitMessenger messenger;

    private UsersDao applicationUserRepository;
    private PasswordEncoder bCryptPasswordEncoder;

    public UserController(UsersDao applicationUserRepository, PasswordEncoder bCryptPasswordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/sign-up")
    public String signUp(@RequestBody UsersEntity user) {
        if(applicationUserRepository.findByUsername(user.getUsername()) != null)
        {
            System.out.println("User already exists with that username.");
            return "User already exists with that username.";
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        HashSet<AuthoritiesEntity> set = new HashSet<>();
        set.add(new AuthoritiesEntity(user,Authority.ROLE_USER));
        user.setAuthorities(set);
        user = applicationUserRepository.save(user);
        if(user != null){
            messenger.NotifyUserCreation(new UserModel(user.getUsername(), "email"));
        }
        System.out.println("User created");
        return "User created";
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody UsersEntity usersEntity)
    {
        System.out.println("Attempting login");
        if(applicationUserRepository.findByUsername(usersEntity.getUsername()) == null)
        {
            System.out.println("Attempted to login with nonexistant user");
            return null;
        }
        RestTemplate template = new RestTemplate();
        HttpHeaders header = createHeaders("clientId","client-secret");
        MultiValueMap<String,String> map = new LinkedMultiValueMap<String,String>();
        map.add("username",usersEntity.getUsername());
        map.add("password",usersEntity.getPassword());
        map.add("grant_type","password");
        System.out.println("Doing the oauth/token thing");
        HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<MultiValueMap<String, String>>(map,header);
        ResponseEntity<TokenResponse> response = template.postForEntity("http://localhost:8082/oauth/token", request, TokenResponse.class);

        return response.getBody();
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestParam String refreshToken){
        RestTemplate template = new RestTemplate();
        HttpHeaders header = createHeaders("clientId","client-secret");
        MultiValueMap<String,String> map = new LinkedMultiValueMap<String,String>();
        map.add("refresh_token",refreshToken);
        map.add("grant_type","refresh_token");
        HttpEntity<MultiValueMap<String,String>> request = new HttpEntity<MultiValueMap<String, String>>(map,header);
        ResponseEntity<TokenResponse> response = template.postForEntity("http://localhost:8082/oauth/token",request,TokenResponse.class);
        return response.getBody();
    }

    @GetMapping("/getExtraUserInfo")
    public String getExtraUserInfo(Principal principal)
    {
        return principal.getName();
    }

    HttpHeaders createHeaders(String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }

    @GetMapping("/getGuestToken")
    public String generateToken(Principal principal){
        return ""; //applicationUserRepository.findByMainUserId(applicationUserRepository.findByUsername(principal.getName()).getId()).getToken();
    }

   // @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/OK")
    public String OK(Principal principal){
        return principal.getName();
    }



    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/OKSecured")
    public String OKSec(Principal principal){
        return principal.getName();
    }

    @GetMapping("/findMe")
    public String findMe(Principal principal){
        return "";// ((Role)applicationUserRepository.findByUsername(principal.getName()).getRoles().toArray()[0]).getRoleName();
    }


}
