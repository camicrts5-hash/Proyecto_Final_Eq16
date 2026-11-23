package com.enlasate.backend.repository;

import com.enlasate.backend.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    boolean existsByNombrePlan(String nombrePlan);
}