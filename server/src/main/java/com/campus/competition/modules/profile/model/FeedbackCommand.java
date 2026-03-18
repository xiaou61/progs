package com.campus.competition.modules.profile.model;

import java.util.List;

public record FeedbackCommand(
  Long userId,
  String content,
  List<String> imageUrls
) {
}
