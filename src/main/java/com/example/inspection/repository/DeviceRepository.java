package com.example.inspection.repository;

import com.example.inspection.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long>
{

}