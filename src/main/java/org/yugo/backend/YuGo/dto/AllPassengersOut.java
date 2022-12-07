package org.yugo.backend.YuGo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.yugo.backend.YuGo.mapper.UserDetailedMapper;
import org.yugo.backend.YuGo.model.Passenger;

import java.util.List;

@NoArgsConstructor
public class AllPassengersOut {
    @Getter @Setter
    private long totalCount;

    @Getter @Setter
    private List<UserDetailedInOut> results;

    public AllPassengersOut(Page<Passenger> passengers){
        this.results = passengers.stream()
                .map(UserDetailedMapper::fromUsertoDTO)
                .toList();

        this.totalCount = passengers.getTotalElements();
    }
}
