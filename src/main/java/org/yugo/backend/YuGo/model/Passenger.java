package org.yugo.backend.YuGo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yugo.backend.YuGo.dto.UserDetailedIn;

import java.util.Set;

@Entity
@NoArgsConstructor
@Getter @Setter
@DiscriminatorValue("PASSENGER")
public class Passenger extends User{
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "passenger_ride")
    private Set<Ride> rides;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "passenger_id")
    private Set<FavoritePath> favouritePaths;

    public Passenger(UserDetailedIn userDetailedIn) {
        super(userDetailedIn);
    }

}
