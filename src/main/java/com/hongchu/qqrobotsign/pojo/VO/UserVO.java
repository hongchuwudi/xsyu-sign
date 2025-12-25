package com.hongchu.qqrobotsign.pojo.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    Long id;
    String name;
    String username;
    String email;
    private Boolean autoSign;
}
