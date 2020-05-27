package me.mastercapexd.auth.email;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailSettings {

	private final boolean enabled;
	private final String smtp, username, password, company;
	private final Map<EmailLetterType, LetterSettings> letterSettingsMap;
}