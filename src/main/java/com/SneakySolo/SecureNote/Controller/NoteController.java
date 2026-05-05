package com.SneakySolo.SecureNote.Controller;

import com.SneakySolo.SecureNote.Dto.NoteRequestDTO;
import com.SneakySolo.SecureNote.Dto.NoteResponseDTO;
import com.SneakySolo.SecureNote.Service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping("/api/notes")
    public ResponseEntity<NoteResponseDTO> createNote(@RequestBody NoteRequestDTO dto,
                                                      Principal principal) {

        NoteResponseDTO resultDTO = noteService.createNote(dto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(resultDTO);
    }

    @GetMapping("/api/notes")
    public ResponseEntity<List<NoteResponseDTO>> getAllNotes(Principal principal) {
        List<NoteResponseDTO> list = noteService.getNotes(principal.getName());
        return ResponseEntity.ok().body(list);
    }

    @DeleteMapping("/api/notes/{id}")
    public ResponseEntity<String> deleteNote (@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok().body("Note deleted");
    }
}
