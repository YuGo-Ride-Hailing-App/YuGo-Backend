package org.yugo.backend.YuGo.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

public class UserDTO {
    @Getter @Setter
    private String name;

    @Getter @Setter
    private String lastName;

    @Getter @Setter
    private String profilePicture;

    @Getter @Setter
    private String phone;

    @Getter @Setter
    private String email;

    @Getter @Setter
    private String address;

    @Getter @Setter
    private String password;

    public UserDTO(String name, String lastName, String profilePicture, String phone, String email, String address, String password) {
        this.name = name;
        this.lastName = lastName;
        this.profilePicture = profilePicture;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.password = password;
    }
}
