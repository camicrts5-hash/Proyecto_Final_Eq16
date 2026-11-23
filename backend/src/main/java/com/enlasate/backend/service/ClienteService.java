package com.enlasate.backend.service;

import com.enlasate.backend.dto.ClienteRequest;
import com.enlasate.backend.dto.ClienteResponse;
import com.enlasate.backend.entity.Cliente;
import com.enlasate.backend.entity.Plan;
import com.enlasate.backend.entity.Usuario;
import com.enlasate.backend.repository.ClienteRepository;
import com.enlasate.backend.repository.PlanRepository;
import com.enlasate.backend.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PlanRepository planRepository;
    private final UsuarioRepository usuarioRepository;

    public ClienteService(
            ClienteRepository clienteRepository,
            PlanRepository planRepository,
            UsuarioRepository usuarioRepository) {

        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(Pageable pageable) {

        return clienteRepository.findAll(pageable)
                .map(this::convertirRespuesta);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Integer id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        return convertirRespuesta(cliente);
    }

    @Transactional
    public ClienteResponse actualizar(
            Integer id,
            ClienteRequest request) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el cliente con ID: " + id
                        )
                );

        Usuario usuario = cliente.getUsuario();

        if (!usuario.getEmail().equalsIgnoreCase(request.email())
                && usuarioRepository.existsByEmail(request.email())) {

            throw new IllegalArgumentException(
                    "El correo electrónico ya está registrado"
            );
        }

        Plan plan = planRepository.findById(request.idPlan())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "No se encontró el plan seleccionado"
                        )
                );

        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setEmail(request.email());
        usuario.setTelefono(request.telefono());

        cliente.setPlan(plan);
        cliente.setEstado(request.estado());

        clienteRepository.save(cliente);

        return convertirRespuesta(cliente);
    }

    private ClienteResponse convertirRespuesta(Cliente cliente) {

        Usuario usuario = cliente.getUsuario();
        Plan plan = cliente.getPlan();

        return new ClienteResponse(
                cliente.getIdCliente(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getTelefono(),
                plan.getNombrePlan(),
                plan.getPrecioMensual(),
                cliente.getFechaAlta(),
                cliente.getEstado()
        );
    }
}