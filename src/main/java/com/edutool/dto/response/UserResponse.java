package com.edutool.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserResponse {

    private Long userId;
    
    private String username;
    
    private String role;
    
    private String fullName;
    
    private String email;
    
    private String status;
}
