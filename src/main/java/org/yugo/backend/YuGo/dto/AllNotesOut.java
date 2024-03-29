package org.yugo.backend.YuGo.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.yugo.backend.YuGo.mapper.NoteMapper;
import org.yugo.backend.YuGo.model.Note;

import java.util.List;

@Getter @Setter
public class AllNotesOut {
    private long totalCount;
    private List<NoteOut> results;

    public AllNotesOut(Page<Note> notes){
        this.results = notes.stream().map(NoteMapper::fromNotetoDTO)
                .toList();

        this.totalCount = notes.getTotalElements();
    }
}
