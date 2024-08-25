package com.iuh.fit.readhub.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "UserSetting")
public class UserSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settingId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Boolean receiveNotifications;
    private Boolean darkMode;
    private String preferredLanguage;



    @Override
    public String toString() {
        return "UserSetting{" +
                "settingId=" + settingId +
                ", user=" + user +
                ", receiveNotifications=" + receiveNotifications +
                ", darkMode=" + darkMode +
                ", preferredLanguage='" + preferredLanguage + '\'' +
                '}';
    }
}
