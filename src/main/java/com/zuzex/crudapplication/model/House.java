package com.zuzex.crudapplication.model;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "houses")
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address")
    private String address;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "house_residents",
            joinColumns = @JoinColumn(name = "house_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "resident_id", referencedColumnName = "id")
    )
    private List<User> residents = new ArrayList<>();
}
