package com.couriertracker.ui.controllers;

import com.couriertracker.core.CourierService;
import com.couriertracker.models.DeliveryAgent;
import com.couriertracker.models.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LostPackageController {

    @Autowired
    private CourierService courierService;

    @GetMapping("/lost")
    public String lost(Model model) {
        model.addAttribute("lostPackages", courierService.getLostPackages());
        model.addAttribute("lostTickets", courierService.getLostTickets());
        return "lost";
    }

    @PostMapping("/lost/mark")
    public String mark(
            @RequestParam("packageId") String packageId,
            @RequestParam("agentName") String agentName,
            RedirectAttributes redirectAttributes) {
        Package pkg = courierService.getPackageById(packageId);
        if (pkg == null) {
            redirectAttributes.addFlashAttribute("message", "Package not found");
            return "redirect:/lost";
        }
        DeliveryAgent agent = courierService.getAgentByName(agentName);
        if (agent == null) {
            redirectAttributes.addFlashAttribute("message", "Agent not found");
            return "redirect:/lost";
        }
        courierService.handleLostPackage(pkg, agent);
        return "redirect:/lost";
    }

    @PostMapping("/lost/resolve")
    public String resolve(@RequestParam("packageId") String packageId, RedirectAttributes redirectAttributes) {
        Package pkg = courierService.getPackageById(packageId);
        if (pkg == null) {
            redirectAttributes.addFlashAttribute("message", "Package not found");
            return "redirect:/lost";
        }
        courierService.resolveLostPackage(pkg);
        return "redirect:/lost";
    }

    @PostMapping("/lost/terminate")
    public String terminate(@RequestParam("packageId") String packageId, RedirectAttributes redirectAttributes) {
        Package pkg = courierService.getPackageById(packageId);
        if (pkg == null) {
            redirectAttributes.addFlashAttribute("message", "Package not found");
            return "redirect:/lost";
        }
        courierService.terminatePackage(pkg);
        return "redirect:/lost";
    }
}