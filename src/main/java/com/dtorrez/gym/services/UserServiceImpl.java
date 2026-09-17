package com.dtorrez.gym.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.micrium.bd.access.jpa.models.Usuario;
import com.micrium.bd.access.jpa.repositories.IUsuarioRepository;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUsuarioRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public Usuario save(Usuario user) {
        return repository.save(user);
    }

}
