package com.carenet.api.infrastructure.exam.dto.payload;

import com.carenet.api.domain.useraccount.UserAccount;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserAccountPayload {

    @Getter
    @NoArgsConstructor
    public static class Get {
        private Long id;
        private String username;

        @QueryProjection
        public Get(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        public UserAccount toDomain() {
            return UserAccount.of(this.id, this.username);
        }
    }

}
