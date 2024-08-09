package com.morris.quizly.controllers;

import com.morris.quizly.models.security.JwtTokenProvider;
import com.morris.quizly.models.security.UserDetails;
import com.morris.quizly.services.QuizlyUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final QuizlyUserDetailsService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public SubscriptionController(QuizlyUserDetailsService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
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

    @GetMapping("/password-reset")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        UserDetails user = userService.findByPasswordResetToken(token);
        if (null == user) {
            String htmlResponse = "<head>" +
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
                    "<h2>Sorry, you're token is invalid and has expired!</h2>" +
                    "</div>" +
                    "<script>" +
                    "window.onload = function() {" +
                    "   var newTab = window.open('', '_blank');" +
                    "   newTab.document.write(\"<html><head><style>" +
                    "body { font-family: Arial, sans-serif; text-align: center; padding: 20px; }" +
                    "h1 { color: #333; }" +
                    ".logo { margin: 20px auto; width: 50px; height: 50px; display: block }" +
                    ".container { max-width: 600px; margin: 0 auto; text-align: center; }" +
                    "</style></head><body><div class='container'>" +
                    "</div></body></html>\");" +
                    "};" +
                    "</script>" +
                    "</body>" +
                    "</html>";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(htmlResponse);
        }
        // remove token from document to prevent reuse
        userService.updateAndRemovePasswordResetToken(token);
        // generate a one time use session token
        String sessionToken = jwtTokenProvider.createOneTimeUseSessionToken(user.getUsername());
        String redirectUrl = "/reset-password?sessionToken=" + sessionToken;
        response.sendRedirect(redirectUrl);
        return ResponseEntity.ok().build();
    }
}
