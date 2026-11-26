package com.mercedes.vehicle.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
@Service
public class JwtService {
 private final SecretKey key;
 public JwtService(@Value("${app.security.jwt-secret}") String secret){
  try { byte[] decoded=Base64.getDecoder().decode(secret); if(decoded.length<32) throw new IllegalArgumentException("JWT secret must decode to at least 32 bytes"); key=Keys.hmacShaKeyFor(decoded); }
  catch(IllegalArgumentException ex){throw new IllegalStateException("Set JWT_SECRET to the same valid Base64-encoded secret used by auth-service",ex);}
 }
 public Claims parse(String token){return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();}
 public boolean isValid(String token){try{return parse(token).getExpiration()!=null && parse(token).getExpiration().after(new Date());}catch(JwtException|IllegalArgumentException ex){return false;}}
}
