package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ItemDto {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDtoOut lastBooking;
    private BookingDtoOut nextBooking;
    private List<CommentDto> comments;
    private Integer requestId;

    public ItemDto(Integer id, String name, String description, Boolean available, Integer requestId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.available = available;
        this.requestId = requestId;
    }
}
