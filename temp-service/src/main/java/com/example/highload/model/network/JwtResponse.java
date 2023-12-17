package com.example.highload.model.network;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class JwtResponse implements Serializable {
    private String token;
    private Integer userId;
}
