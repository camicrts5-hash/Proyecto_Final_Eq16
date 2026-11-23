package com.enlasate.backend.service;

import java.util.List;

import com.enlasate.backend.dto.PlanRequest;
import com.enlasate.backend.dto.PlanResponse;
import com.enlasate.backend.entity.Plan;
import com.enlasate.backend.repository.PlanRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    // Consultar todos los planes
    @Transactional(readOnly = true)
    public List<PlanResponse> listar() {
        return planRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    // Consultar un plan por ID
    @Transactional(readOnly = true)
    public PlanResponse buscarPorId(Integer id) {

        Plan plan = planRepository.findById(id)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El plan no existe"
                    )
                );

        return convertirRespuesta(plan);
    }

    // Registrar un plan
    public PlanResponse registrar(PlanRequest request) {

        if (planRepository.existsByNombrePlan(request.nombrePlan())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe un plan con ese nombre"
            );
        }

        Plan plan = new Plan();

        plan.setNombrePlan(request.nombrePlan());
        plan.setVelocidadMb(request.velocidadMb());
        plan.setPrecioMensual(request.precioMensual());

        return convertirRespuesta(planRepository.save(plan));
    }

    // Actualizar un plan
    public PlanResponse actualizar(Integer id, PlanRequest request) {

        Plan plan = planRepository.findById(id)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El plan no existe"
                    )
                );

        if (planRepository.existsByNombrePlanAndIdPlanNot(
                request.nombrePlan(), id)) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe otro plan con ese nombre"
            );
        }

        plan.setNombrePlan(request.nombrePlan());
        plan.setVelocidadMb(request.velocidadMb());
        plan.setPrecioMensual(request.precioMensual());

        return convertirRespuesta(planRepository.save(plan));
    }

    // Eliminar un plan
    public void eliminar(Integer id) {

        Plan plan = planRepository.findById(id)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El plan no existe"
                    )
                );

        planRepository.delete(plan);
    }

    // Convertir entidad a DTO
    private PlanResponse convertirRespuesta(Plan plan) {

        return new PlanResponse(
            plan.getIdPlan(),
            plan.getNombrePlan(),
            plan.getVelocidadMb(),
            plan.getPrecioMensual()
        );
    }
}