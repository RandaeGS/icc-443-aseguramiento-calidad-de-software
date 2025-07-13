package com.randaegarcia.domain.dto;

import jakarta.ws.rs.DefaultValue;

public record ProductoListRequestDto(@DefaultValue("0") int page,@DefaultValue("10") int size, String name){

}
