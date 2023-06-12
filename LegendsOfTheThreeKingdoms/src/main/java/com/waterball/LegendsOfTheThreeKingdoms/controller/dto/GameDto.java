package com.waterball.LegendsOfTheThreeKingdoms.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameDto implements Serializable {
    private String gameId;
    private List<PlayerDto> players;

}
