package com.mercedes.vehicle.service;
import com.mercedes.vehicle.dto.*;import com.mercedes.vehicle.entity.*;import com.mercedes.vehicle.event.KafkaEventPublisher;import com.mercedes.vehicle.repository.*;import com.mercedes.vehicle.security.CurrentUser;
import org.springframework.http.HttpStatus;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@Service
public class VehicleService {
 private final VehicleRepository vehicles;private final VehicleOwnershipRepository ownerships;private final KafkaEventPublisher events;
 public VehicleService(VehicleRepository v,VehicleOwnershipRepository o,KafkaEventPublisher e){vehicles=v;ownerships=o;events=e;}
 @Transactional public VehicleResponse create(CreateVehicleRequest r,CurrentUser user){Vehicle v=new Vehicle();v.setVehicleCode(generateVehicleCode());v.setVin(generateVin());v.setModel(r.model().trim());v.setModelId(r.modelId());v.setModelYear(r.modelYear());v.setCurrentOwnerId(user.userId());v.setCurrentOwnerEmail(user.email());v.setStatus(VehicleStatus.ACTIVE);Vehicle saved=vehicles.save(v);recordOwnership(saved,user.userId(),user.email(),null);VehicleResponse response=VehicleResponse.from(saved);events.publishVehicleCreated(response);events.publishOwnershipAssigned(response);return response;}
 @Transactional(readOnly=true) public List<VehicleResponse> myVehicles(CurrentUser user){return vehicles.findByCurrentOwnerIdOrderByCreatedAtDesc(user.userId()).stream().map(VehicleResponse::from).toList();}
 @Transactional(readOnly=true) public VehicleResponse getByCode(String code,CurrentUser user){Vehicle v=vehicles.findByVehicleCode(code).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Vehicle not found"));if(!v.getCurrentOwnerId().equals(user.userId())&&!isAdmin(user))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You do not own this vehicle");return VehicleResponse.from(v);}
 @Transactional(readOnly=true) public List<OwnershipResponse> history(String code,CurrentUser user){Vehicle v=vehicles.findByVehicleCode(code).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Vehicle not found"));if(!v.getCurrentOwnerId().equals(user.userId())&&!isAdmin(user))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You do not own this vehicle");return ownerships.findByVehicleIdOrderByOwnedFromDesc(v.getId()).stream().map(OwnershipResponse::from).toList();}
 private boolean isAdmin(CurrentUser u){return u.role()!=null&&u.role().toUpperCase(Locale.ROOT).contains("ADMIN");}
 private void recordOwnership(Vehicle v,String ownerId,String email,String purchaseId){VehicleOwnership o=new VehicleOwnership();o.setVehicleId(v.getId());o.setVehicleCode(v.getVehicleCode());o.setOwnerId(ownerId);o.setOwnerEmail(email);o.setPurchaseId(purchaseId);o.setCurrentOwner(true);ownerships.save(o);}
 private String generateVehicleCode(){return "MB-VEH-"+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase(Locale.ROOT);}
 private String generateVin(){return "W1K"+UUID.randomUUID().toString().replace("-","").substring(0,14).toUpperCase(Locale.ROOT);}
}
