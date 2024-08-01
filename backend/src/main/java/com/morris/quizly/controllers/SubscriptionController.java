package com.morris.quizly.controllers;

import com.morris.quizly.models.security.UserDetails;
import com.morris.quizly.services.QuizlyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final QuizlyUserDetailsService userService;

    @Autowired
    public SubscriptionController(QuizlyUserDetailsService userService) {
        this.userService = userService;
    }

    @GetMapping("/confirm-signup")
    public ResponseEntity<String> confirmSignup(@RequestParam("token") String token) {
        UserDetails user = userService.findBySignupToken(token);
        if (null == user) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
        }
        userService.updateEnabledStatus(user, true);

        String htmlResponse = "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; text-align: center; padding: 20px; }" +
                "h2 { color: #333; }" +
                ".logo { margin: 20px auto; }" +
                ".container { max-width: 600px; margin: 0 auto; text-align: center; }" +
                ".button { background-color: #000; color: #fff; padding: 10px 20px; text-decoration: none; display: inline-block; margin-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<img class='logo' src='/images/quizly-logo.png' alt='Quizly Logo'>" +
                "<h2>Thanks for confirming signup with Quizly, " + user.getFirstName() + "!</h2>" +
                "</div>" +
                "<script>" +
                "window.onload = function() {" +
                "   var newTab = window.open('', '_blank');" +
                "   newTab.document.write(\"<html><head><style>" +
                "body { font-family: Arial, sans-serif; text-align: center; padding: 20px; }" +
                "h1 { color: #333; }" +
                ".logo { margin: 20px auto; }" +
                ".container { max-width: 600px; margin: 0 auto; text-align: center; }" +
                "</style></head><body><div class='container'>" +
                "<h1>Subscription confirmed successfully!</h1>" +
                "</div></body></html>\");" +
                "};" +
                "</script>" +
                "</body>" +
                "</html>";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return ResponseEntity.ok().headers(headers).body(htmlResponse);
    }
}
