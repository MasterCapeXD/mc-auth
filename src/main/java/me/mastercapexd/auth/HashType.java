package me.mastercapexd.auth;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public enum HashType {

	MD5 {
		@Override
		public HashFunction getHashFunction() {
			return Hashing.md5();
		}
	}, SHA256 {
		@Override
		public HashFunction getHashFunction() {
			return Hashing.sha256();
		}
	};
	
	public abstract HashFunction getHashFunction();
}