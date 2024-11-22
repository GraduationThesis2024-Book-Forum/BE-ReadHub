package com.iuh.fit.readhub.repositories;

import com.iuh.fit.readhub.models.UserDevice;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT * FROM user_devices WHERE fcm_token = :fcmToken FOR UPDATE", nativeQuery = true)
    Optional<UserDevice> findByFcmToken(@Param("fcmToken") String fcmToken);

    @Query(value = "SELECT * FROM user_devices WHERE user_id = :userId", nativeQuery = true)
    List<UserDevice> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_devices WHERE fcm_token = :fcmToken", nativeQuery = true)
    void deleteByFcmToken(@Param("fcmToken") String fcmToken);
}