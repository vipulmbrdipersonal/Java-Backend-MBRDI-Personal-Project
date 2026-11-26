
package com.mercedes.telemetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}


/*
For Jwt issue in any service
# Generate one shared JWT secret
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$secret = [Convert]::ToBase64String($bytes)
$rng.Dispose()

# Set for the current terminal session
$env:JWT_SECRET = $secret

# Save permanently for future sessions
[Environment]::SetEnvironmentVariable("JWT_SECRET", $secret, "User")

Write-Host "JWT_SECRET configured successfully!" -ForegroundColor Green
 */