package com.mercedes.vehicle.security;
public record CurrentUser(String userId,String email,String role,String tokenId) {}
