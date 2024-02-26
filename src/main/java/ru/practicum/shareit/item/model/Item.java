package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO Sprint add-controllers.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Item {
    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private Integer ownerId;
}
