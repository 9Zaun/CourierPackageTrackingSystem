package com.couriertracker.ui.controllers;

import com.couriertracker.core.CourierService;
import com.couriertracker.models.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TrackController {

    @Autowired
    private CourierService courierService;

    @GetMapping("/track")
    public String trackGet(Model model) {
        return "track";
    }

    @PostMapping("/track")
    public String trackPost(@RequestParam("packageId") String packageId, Model model) {
        Package pkg = courierService.trackPackage(packageId);
        model.addAttribute("pkg", pkg);
        return "track";
    }
}