package com.matching.ezgg.global.exception;

public class PreferredChampionsException extends BaseException {
  private static final String MESSAGE = "선호 챔피언은 최대 3개까지 선택할 수 있습니다.";

  public PreferredChampionsException() { super(MESSAGE); }

  @Override
  public int getStatusCode() { return 500; }
}

