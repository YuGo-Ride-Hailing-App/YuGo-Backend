package org.yugo.backend.YuGo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.yugo.backend.YuGo.model.Rejection;
import org.yugo.backend.YuGo.repository.RejectionRepository;

import java.util.List;
import java.util.Optional;

public class RejectionServiceImpl implements RejectionService {
    private final RejectionRepository rejectionRepository;

    @Autowired
    public RejectionServiceImpl(RejectionRepository rejectionRepository){
        this.rejectionRepository = rejectionRepository;
    }

    @Override
    public Rejection add(Rejection rejection){
        return rejectionRepository.save(rejection);
    }

    @Override
    public List<Rejection> getAll() {
        return rejectionRepository.findAll();
    }

    @Override
    public Optional<Rejection> get(Integer id) {
        return rejectionRepository.findById(id);
    }
}