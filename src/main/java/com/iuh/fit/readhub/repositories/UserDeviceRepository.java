package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    @Query(value = "SELECT * FROM user_devices WHERE fcm_token = :fcmToken LIMIT 1", nativeQuery = true)
    Optional<UserDevice> findByFcmToken(String fcmToken);

    @Query(value = "SELECT * FROM user_devices WHERE user_id = :userId", nativeQuery = true)
    List<UserDevice> findByUserId(Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_devices WHERE fcm_token = :fcmToken", nativeQuery = true)
    void deleteByFcmToken(String fcmToken);
}
