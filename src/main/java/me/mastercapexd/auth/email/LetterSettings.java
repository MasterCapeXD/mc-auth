package me.mastercapexd.auth.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LetterSettings {

	private final String subject, text;
}