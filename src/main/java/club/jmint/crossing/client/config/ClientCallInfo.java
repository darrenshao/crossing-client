package club.jmint.crossing.client.config;

/**
 * Contains interface info including: server name/interface class name/method name/
 * 	sign type/sign key/encrypt type/encrypt key/decrypt key/
 * @author shc
 *
 */
public class ClientCallInfo {
	public String server;
	public String clazz;
	public String method;
	public String signType;
	public String signKey;
	public String encryptType;
	public String encryptKey;
	public String decryptKey;
	
	public ClientCallInfo(String server, String clazz, String method, String signType, String signKey, 
			String encryptType, String encryptKey, String decryptKey) {
		super();
		this.server = server;
		this.clazz = clazz;
		this.method = method;
		this.signType = signType;
		this.signKey = signKey;
		this.encryptType = encryptType;
		this.encryptKey = encryptKey;
		this.decryptKey = decryptKey;
	}
	
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getSignType() {
		return signType;
	}
	public void setSignType(String signType) {
		this.signType = signType;
	}
	public String getSignKey() {
		return signKey;
	}
	public void setSignKey(String signKey) {
		this.signKey = signKey;
	}
	public String getEncryptType() {
		return encryptType;
	}
	public void setEncryptType(String encryptType) {
		this.encryptType = encryptType;
	}
	public String getEncryptKey() {
		return encryptKey;
	}
	public void setEncryptKey(String encryptKey) {
		this.encryptKey = encryptKey;
	}
	public String getDecryptKey() {
		return decryptKey;
	}
	public void setDecryptKey(String decryptKey) {
		this.decryptKey = decryptKey;
	}
	
	
	
}
