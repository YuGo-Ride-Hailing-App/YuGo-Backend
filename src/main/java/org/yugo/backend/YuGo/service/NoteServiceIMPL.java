package org.yugo.backend.YuGo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.yugo.backend.YuGo.model.Note;
import org.yugo.backend.YuGo.repository.NoteRepository;

import java.util.List;
import java.util.Optional;

public class NoteServiceIMPL implements NoteService {
    private final NoteRepository noteRepository;

    @Autowired
    public NoteServiceIMPL(NoteRepository noteRepository){
        this.noteRepository = noteRepository;
    }

    @Override
    public Note add(Note note){
        return noteRepository.save(note);
    }

    @Override
    public List<Note> getAll() {
        return noteRepository.findAll();
    }

    @Override
    public Optional<Note> get(Integer id) {
        return noteRepository.findById(id);
    }
}