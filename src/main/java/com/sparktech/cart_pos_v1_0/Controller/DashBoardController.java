package com.sparktech.cart_pos_v1_0.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j

public class DashBoardController {
    @GetMapping("/")
    public String dashboard() {
        return "dashboard";
    }
}
