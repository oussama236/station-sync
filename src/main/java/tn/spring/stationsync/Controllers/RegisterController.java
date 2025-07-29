package tn.spring.stationsync.Controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.spring.stationsync.Dtos.RegisterRequest;
import tn.spring.stationsync.Services.IUserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class RegisterController {

    private final IUserService userService;

    public RegisterController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
            userService.register(request);
            response.put("message", "Utilisateur enregistré avec succès !");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("error", "Erreur : " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}
