package org.yugo.backend.YuGo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class    TokenRefreshIn {
    private String refreshToken;

    public void TokenRefreshIn(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
