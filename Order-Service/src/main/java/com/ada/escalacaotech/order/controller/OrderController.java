package com.ada.escalacaotech.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class OrderController {

    @GetMapping
    public List<String> getPedidos() {
        return List.of("Pedido #001", "Pedido #002", "Pedido #003");
    }

    @GetMapping("/admin")
    public String admin() {
        return "Área administrativa";
    }
}
