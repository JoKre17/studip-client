package de.luh.kriegel.studip.client.config;

public enum SubPaths {

	API("/api.php"),
	DEFAULT("/dispatch.php");
	
	
	private final String path;
	
	private SubPaths(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return this.path;
	}
	
	@Override
	public String toString() {
		return path;
	}
}
