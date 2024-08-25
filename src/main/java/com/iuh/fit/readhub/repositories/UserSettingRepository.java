package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
}