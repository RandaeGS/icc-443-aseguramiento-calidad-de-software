package com.randaegarcia.security;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Data
@Entity
@RevisionEntity(CustomRevisionListener.class)
@Table(name = "revinfo")
public class CustomRevisionEntity {

    @Id
    @GeneratedValue
    @RevisionNumber
    private int rev;

    @RevisionTimestamp
    private long revtstmp;

    @Column(name = "username")
    private String username;
}
