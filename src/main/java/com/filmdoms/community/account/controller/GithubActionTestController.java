package com.filmdoms.community.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GithubActionTestController {

    @GetMapping("/test")
    public String version() {
        return "github-action-version-1";
    }
}
