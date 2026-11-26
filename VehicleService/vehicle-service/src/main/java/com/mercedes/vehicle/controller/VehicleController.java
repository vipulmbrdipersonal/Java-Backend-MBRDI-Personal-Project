package com.mercedes.vehicle.controller;
import com.mercedes.vehicle.dto.*;import com.mercedes.vehicle.security.CurrentUser;import com.mercedes.vehicle.service.VehicleService;
import jakarta.servlet.http.HttpServletRequest;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.util.List;
@RestController @RequestMapping("/api/vehicles")
public class VehicleController {
 private final VehicleService service;public VehicleController(VehicleService s){service=s;}
 private CurrentUser user(HttpServletRequest r){return (CurrentUser)r.getAttribute("currentUser");}
 @PostMapping public ResponseEntity<VehicleResponse> create(@Valid @RequestBody CreateVehicleRequest request,HttpServletRequest http){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request,user(http)));}
 @GetMapping("/my") public List<VehicleResponse> myVehicles(HttpServletRequest http){return service.myVehicles(user(http));}
 @GetMapping("/{vehicleId}") public VehicleResponse get(@PathVariable String vehicleId,HttpServletRequest http){return service.getByCode(vehicleId,user(http));}
 @GetMapping("/{vehicleId}/ownership-history") public List<OwnershipResponse> history(@PathVariable String vehicleId,HttpServletRequest http){return service.history(vehicleId,user(http));}
}
