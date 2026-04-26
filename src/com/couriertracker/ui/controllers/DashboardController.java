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
import com.couriertracker.models.Route;

@Controller
public class DashboardController {

    @Autowired
    private CourierService courierService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("agents", courierService.getAgents());
        model.addAttribute("routes", courierService.getRoutes());
        model.addAttribute("activePackages", courierService.getActivePackages());
        return "dashboard";
    }

    @PostMapping("/dashboard/select-route")
    public String selectRoute(@RequestParam("agentName") String agentName, RedirectAttributes redirectAttributes) {
        DeliveryAgent agent = courierService.getAgentByName(agentName);
        if (agent == null) {
            redirectAttributes.addFlashAttribute("message", "Agent not found");
            return "redirect:/dashboard";
        }
        System.out.println("Agent: " + agent.getName());
        System.out.println("Carried: " + agent.getCarriedPackages().size());
        for (Route r : agent.getOwnedRoutes()) {
            System.out.println("Route " + r.getRouteID() + " warehouse count: " + r.getWarehousePackageCount());
        }
        for (Package pkg : agent.getCarriedPackages()) {
            System.out.println("Package: " + pkg.getPackageID() + 
                " dest: " + pkg.getDestination() + 
                " isChained: " + pkg.isChained() + 
                " status: " + pkg.getStatus());
        }
        for (com.couriertracker.models.Route r : courierService.getRoutes()) {
            System.out.println("GLOBAL Route " + r.getRouteID() + " warehouse: " + r.getWarehousePackageCount());
        }
        boolean selected = courierService.agentSelectsRoute(agent);
        if (selected) {
            redirectAttributes.addFlashAttribute("message", "Route selected");
        } else {
            redirectAttributes.addFlashAttribute("message", "No packages on any route");
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/depart")
    public String depart(@RequestParam("agentName") String agentName, RedirectAttributes redirectAttributes) {
        DeliveryAgent agent = courierService.getAgentByName(agentName);
        if (agent == null) {
            redirectAttributes.addFlashAttribute("message", "Agent not found");
            return "redirect:/dashboard";
        }
        courierService.startTravelling(agent);
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/arrive")
    public String arrive(@RequestParam("agentName") String agentName, RedirectAttributes redirectAttributes) {
        DeliveryAgent agent = courierService.getAgentByName(agentName);
        if (agent == null) {
            redirectAttributes.addFlashAttribute("message", "Agent not found");
            return "redirect:/dashboard";
        }
        if (agent.getActiveRoute() == null) {
            redirectAttributes.addFlashAttribute("message", "Agent has no active route");
            return "redirect:/dashboard";
        }
        boolean finished = courierService.agentArrived(agent);
        if (finished) {
            redirectAttributes.addFlashAttribute("message", "Route complete");
        } else {
            redirectAttributes.addFlashAttribute("message", "Arrived at next stop");
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/confirm-pickups")
    public String confirmPickups(@RequestParam("agentName") String agentName, RedirectAttributes redirectAttributes) {
        DeliveryAgent agent = courierService.getAgentByName(agentName);
        if (agent == null) {
            redirectAttributes.addFlashAttribute("message", "Agent not found");
            return "redirect:/dashboard";
        }
        courierService.confirmPickups(agent);
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/flag-lost")
    public String flagLost(
            @RequestParam("agentName") String agentName,
            @RequestParam("packageId") String packageId,
            RedirectAttributes redirectAttributes) {
        DeliveryAgent agent = courierService.getAgentByName(agentName);
        if (agent == null) {
            redirectAttributes.addFlashAttribute("message", "Agent not found");
            return "redirect:/dashboard";
        }
        Package pkg = null;
        for (Package p : courierService.getActivePackages()) {
            if (p.getPackageID().equals(packageId)) {
                pkg = p;
                break;
            }
        }
        if (pkg == null) {
            redirectAttributes.addFlashAttribute("message", "Package not found");
            return "redirect:/dashboard";
        }
        courierService.handleLostPackage(pkg, agent);
        redirectAttributes.addFlashAttribute("message", "Package flagged as lost");
        return "redirect:/dashboard";
    }
}