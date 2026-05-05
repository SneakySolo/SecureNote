package com.SneakySolo.SecureNote.Dto;

import lombok.Data;

@Data
public class NoteResponseDTO {
    Long id;
    String title;
    String content;
    String username;
}
