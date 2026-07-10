package com.sahil.hospital_appointment.repository;

import com.sahil.hospital_appointment.model.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule,Long> {

    List<DoctorSchedule> findByDoctorId(Long doctorId);

    List<DoctorSchedule> findByDoctorIdAndDayOfWeekAndActiveTrue(Long doctorId, DayOfWeek dayOfWeek);
}
