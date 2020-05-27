package me.mastercapexd.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailConfirmationEntry {

	private final String email, code;
}