package com.hzyi.jplab.core.viewer;

import lombok.Builder;
import lombok.Getter;

@Builder
public class Appearance {

  public static enum Color {
    red, blue, yellow, green, black, white, gray
  }

  public static enum Style {
    fill, stroke
  }

  @Getter private Color color;
  @Getter private Style style;

}