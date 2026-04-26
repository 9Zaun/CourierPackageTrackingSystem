package com.couriertracker.ui.controllers;

import com.couriertracker.core.CourierService;
import com.couriertracker.models.Customer;
import com.couriertracker.models.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegisterController {

    @Autowired
    private CourierService courierService;

    @GetMapping("/register")
    public String registerGet(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerPost(
            @RequestParam("senderName") String senderName,
            @RequestParam("senderCity") String senderCity,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("destinationCity") String destinationCity,
            RedirectAttributes redirectAttributes) {
        Customer customer = new Customer("", senderName, "", "", senderCity, "", "", "", null);
        Package pkg = courierService.createPackage(customer, receiverName, destinationCity);
        courierService.registerPackage(pkg, customer);
        redirectAttributes.addFlashAttribute("packageId", pkg.getPackageID());
        redirectAttributes.addFlashAttribute("message", "Package registered successfully");
        return "redirect:/register";
    }
}