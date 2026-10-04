package com.taskflow.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(min = 3, max = 60)
    @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "Username may only contain letters, numbers, dots and underscores")
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    // At least one letter and one digit; keep it simple but not trivial.
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "Password must be at least 8 characters and include a letter and a number")
    private String password;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Enter a valid phone number")
    private String phoneNumber;

    // Deliberately NOT a Role field. Public registration always creates a
    // TEAM_MEMBER account - see section "Important Registration Role Security".
    // A "requestedRole" is accepted only for UX (e.g. to show a pending-approval
    // message) and is never trusted for the actual assigned role.
    private String requestedRole;
}
