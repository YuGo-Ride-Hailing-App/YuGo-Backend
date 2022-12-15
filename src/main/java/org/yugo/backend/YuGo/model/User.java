package org.yugo.backend.YuGo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yugo.backend.YuGo.dto.UserDetailedIn;


@Entity
@NoArgsConstructor
@Getter @Setter
@Table(name="Users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="user_type",
        discriminatorType = DiscriminatorType.STRING)
public abstract class User {
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "surname", nullable = false)
    private String surname;
    @Column(name = "profile_picture", nullable = false)
    private String profilePicture;
    @Column(name = "telephone_number", nullable = false)
    private String telephoneNumber;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "address", nullable = false)
    private String address;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    public User(UserDetailedIn userDetailedIn) {
        this.name = userDetailedIn.getName();
        this.surname = userDetailedIn.getSurname();
        this.profilePicture = userDetailedIn.getProfilePicture();
        this.telephoneNumber = userDetailedIn.getTelephoneNumber();
        this.email = userDetailedIn.getEmail();
        this.address = userDetailedIn.getAddress();
        this.password = userDetailedIn.getPassword();
        this.isBlocked = false;
        this.isActive = false;
    }
}
