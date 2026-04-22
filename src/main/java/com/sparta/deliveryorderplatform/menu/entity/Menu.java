package com.sparta.deliveryorderplatform.menu.entity;

import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
import com.sparta.deliveryorderplatform.menu.dto.MenuRequestDto;
import com.sparta.deliveryorderplatform.store.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "p_menu")
public class Menu extends BaseAuditEntity {

    @Id
    @Column(name = "menu_id", columnDefinition = "uuid")
    private UUID menuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_hidden")
    private Boolean isHidden;

    public Menu(MenuRequestDto requestDto, Store store) {
        this.menuId = UUID.randomUUID();
        this.name = requestDto.getName();
        this.price = requestDto.getPrice();
        this.description = requestDto.getDescription();

        this.store = store;
    }
}