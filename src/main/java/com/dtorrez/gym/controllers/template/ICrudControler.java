package com.dtorrez.gym.controllers.template;

import java.net.URISyntaxException;
import java.io.Serializable;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;

import com.dtorrez.gym.common.exceptions.ApiException;
import com.dtorrez.gym.security.utils.JwtTokenUtil;

/**
 *
 * @author alepaco.maton
 * @param <R>
 * @param <T>
 * @param <P>
 */
public interface ICrudControler<R extends Serializable, T extends Serializable, P> {

    @GetMapping
    Page<T> list(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form, Pageable pageRequest)throws Exception;
            //@RequestHeader(value = JWTTokenUtil.ROUTE) String form, @Valid @RequestBody @Size(max = 1000) Pageable pageRequest)throws Exception;

    @GetMapping("/{id}")
    ResponseEntity<T> get(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form, @PathVariable P id) throws ApiException;

    @PostMapping
    ResponseEntity<T> create(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Valid @RequestBody R request, BindingResult result) throws URISyntaxException, ApiException;

    @PutMapping("/{id}")
    ResponseEntity<T> update(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            @Valid @RequestBody R request,
            //@PathVariable @Min(value = 1, message = "Identificador invalido, no puede ser menor a 1") @Max(value = 999999999, message = "Identificador invalido") P id,
            @PathVariable P id,
            BindingResult result) throws ApiException;

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@RequestHeader(value = JwtTokenUtil.KEY_TOKEN) String token,
            @RequestHeader(value = JwtTokenUtil.IP_CLIENT) String ipClient,
            @RequestHeader(value = JwtTokenUtil.ROUTE) String form,
            //@PathVariable @Min(value = 1, message = "Identificador invalido, no puede ser menor a 1") @Max(value = 999999999, message = "Identificador invalido") P id
            @PathVariable P id
            ) throws ApiException;

}
