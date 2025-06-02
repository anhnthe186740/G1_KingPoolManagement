package com.example.kingpool.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Entity
@Table(name = "persistent_logins")
@Getter
@Setter
public class PersistentLogin {

    @Id
    @Column(name = "series", length = 64)
    private String series;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "token", nullable = false, length = 64)
    private String token;

    @Column(name = "last_used", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUsed;
}
