package club.jmint.crossing.client.config;

public class CrossingServer {
	public String name;
	public String ip;
	public String port;
	public boolean ssl_enabled;
	
	
	
	public CrossingServer(String name, String ip, String port, boolean ssl_enabled) {
		super();
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.ssl_enabled = ssl_enabled;
	}
	
	public String getName(){
		return name;
	}
	public void setName(){
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public boolean isSsl_enabled() {
		return ssl_enabled;
	}
	public void setSsl_enabled(boolean ssl_enabled) {
		this.ssl_enabled = ssl_enabled;
	}
	
	
}
