package com.secondtrade.market.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Keys {
  private static final DateTimeFormatter ORDER_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private Keys() {}

  public static String draftKey() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  public static String orderNo(long seq) {
    String ts = LocalDateTime.now().format(ORDER_FMT);
    return "ORD-" + ts + "-" + String.format("%04d", (seq % 10000));
  }
}

