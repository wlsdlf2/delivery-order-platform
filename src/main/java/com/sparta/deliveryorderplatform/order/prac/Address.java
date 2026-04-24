//package com.sparta.deliveryorderplatform.order.prac;
//
//import com.sparta.deliveryorderplatform.global.entity.BaseAuditEntity;
//import com.sparta.deliveryorderplatform.user.entity.User;
//import jakarta.persistence.*;
//import lombok.*;
//import java.util.UUID;
//
//@Entity
//@Table(name = "p_address")
//@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class Address extends BaseAuditEntity {
//
//    @Id @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(name = "address_id")
//    private UUID id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @Column(length = 50)
//    private String alias;
//
//    @Column(nullable = false)
//    private String address;
//
//    @Column(name = "detail")
//    private String detail;
//
//    @Column(name = "zip_code", length = 10)
//    private String zipCode;
//
//    @Column(name = "is_default", nullable = false)
//    private Boolean isDefault = false;
//}