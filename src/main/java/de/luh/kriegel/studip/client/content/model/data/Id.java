package de.luh.kriegel.studip.client.content.model.data;

import java.math.BigInteger;

public class Id {

	public static final int LENGTH = 32;
	
	// 6e6f002856c3a0480a36bd1d97d67c70
	private String id_hex;
	
	// 294301540684354112428131192988899370874525954272
	private BigInteger id_bi;
	
	public Id(String hex) {
		assert hex != null;
		assert hex.length() == LENGTH;
		
		this.id_hex = hex;
		this.id_bi = new BigInteger(hex, LENGTH);
	}
	
	public Id(BigInteger bi) {
		assert bi != null;
		
		this.id_hex = bi.toString();
		this.id_bi = bi;
	}
	
	public String asHex() {
		return id_hex;
	}
	
	public BigInteger asBigInt() {
		return id_bi;
	}
	
	@Override
	public String toString() {
		return id_hex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id_hex == null) ? 0 : id_hex.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Id)) {
			return false;
		}
		Id other = (Id) obj;
		if (id_hex == null) {
			if (other.id_hex != null) {
				return false;
			}
		} else if (!id_hex.equals(other.id_hex)) {
			return false;
		}
		return true;
	}
	
}
