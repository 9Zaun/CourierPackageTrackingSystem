package com.couriertracker.ui.controllers;

import com.couriertracker.core.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private CourierService courierService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("agents", courierService.getAgents());
        model.addAttribute("routes", courierService.getRoutes());
        return "home";
    }
}