package org.yugo.backend.YuGo.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yugo.backend.YuGo.model.Document;
import org.yugo.backend.YuGo.repository.DocumentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentServiceImpl(DocumentRepository documentRepository){
        this.documentRepository = documentRepository;
    }

    @Override
    public Document add(Document document){
        return documentRepository.save(document);
    }

    @Override
    public List<Document> getAll() {
        return documentRepository.findAll();
    }

    @Override
    public Optional<Document> get(Integer id) {
        return documentRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteAllForDriver(Integer driverId) {
        documentRepository.deleteAllByDriver_Id(driverId);
    }
}
