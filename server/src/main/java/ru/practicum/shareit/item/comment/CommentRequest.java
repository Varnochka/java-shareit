package ru.practicum.shareit.item.comment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CommentRequest {
    private String text;
}
