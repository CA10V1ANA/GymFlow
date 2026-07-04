package com.gymflow.pro.service;

import java.time.LocalDate;
import java.util.Map;

public interface ReportService {

    Map<String, Object> studentsReport();

    Map<String, Object> financialReport(LocalDate startDate, LocalDate endDate);

    Map<String, Object> attendanceReport(LocalDate startDate, LocalDate endDate);

    Map<String, Object> plansReport();

    Map<String, Object> productsReport();
}
