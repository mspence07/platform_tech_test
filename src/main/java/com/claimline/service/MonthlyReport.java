package com.claimline.service;

import java.util.Map;

/** Approved spend for a month, totalled by category. Amounts are whole dollars. */
public record MonthlyReport(String month, Map<String, Long> totalsByCategory, long total) {}
