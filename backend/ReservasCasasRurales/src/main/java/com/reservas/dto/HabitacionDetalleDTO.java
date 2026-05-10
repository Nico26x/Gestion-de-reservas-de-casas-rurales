package com.reservas.dto;

import com.reservas.model.TipoCama;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HabitacionDetalleDTO {
    
    private Long id;
    private String codigoHabitacion;
    private Integer numeroCamas;
    private TipoCama tipoCama;
    private Boolean tieneBano;
}
