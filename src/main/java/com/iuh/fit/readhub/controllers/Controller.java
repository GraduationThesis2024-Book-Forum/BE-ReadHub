package com.iuh.fit.readhub.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class Controller {
    //Test heartbeat
    @GetMapping("/heartbeat")
    public String heartbeat() {
        return "OK";
    }
}
