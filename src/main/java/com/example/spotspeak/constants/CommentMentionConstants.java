package com.example.spotspeak.constants;

import java.util.regex.Pattern;

public class CommentMentionConstants {
    public static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9._-]+)");
}
