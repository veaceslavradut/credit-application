package com.creditapp.bank.dto;

import java.util.List;

public record QuickLink(
    String label,
    String url,
    String icon
) {}