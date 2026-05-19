package com.reservas.dto;

import com.reservas.model.TipoCama;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HabitacionRequestDTO {

    private Integer numeroCamas;
    private TipoCama tipoCama;
    private Boolean tieneBano;
}
