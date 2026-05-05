package com.SneakySolo.SecureNote.Service;

import com.SneakySolo.SecureNote.Dto.NoteRequestDTO;
import com.SneakySolo.SecureNote.Dto.NoteResponseDTO;
import com.SneakySolo.SecureNote.Entity.Note;
import com.SneakySolo.SecureNote.Entity.User;
import com.SneakySolo.SecureNote.Repository.NoteRepository;
import com.SneakySolo.SecureNote.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteResponseDTO createNote(NoteRequestDTO dto, String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Note note = new Note();
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setUser(user);
        noteRepository.save(note);

        NoteResponseDTO responseDTO = new NoteResponseDTO();
        responseDTO.setId(note.getId());
        responseDTO.setContent(note.getContent());
        responseDTO.setTitle(note.getTitle());
        responseDTO.setUsername(note.getUser().getUsername());
        return responseDTO;
    }

    public List<NoteResponseDTO> getNotes(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Note> note = noteRepository.findByUserId(user.getId());
        List<NoteResponseDTO> responseDTO = new ArrayList<>();

        for (Note temp: note) {
            NoteResponseDTO tempResponseDTO = new NoteResponseDTO();
            tempResponseDTO.setId(temp.getId());
            tempResponseDTO.setContent(temp.getContent());
            tempResponseDTO.setTitle(temp.getTitle());
            tempResponseDTO.setUsername(temp.getUser().getUsername());

            responseDTO.add(tempResponseDTO);
        }
        return responseDTO;
    }

    public void deleteNote(Long id) {
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
        }
        else {
            throw new RuntimeException("Note not found");
        }
    }
}
